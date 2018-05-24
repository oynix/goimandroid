package com.mixotc.imsdklib.remotechat;

import android.content.Context;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.mixotc.imsdklib.chat.GOIMChatOptions;
import com.mixotc.imsdklib.cloud.GOIMCloudFileManager;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.message.FileMessageBody;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMMessageUtils;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.ControlPacket;
import com.mixotc.imsdklib.packet.ControlReplyPacket;
import com.mixotc.imsdklib.packet.NotifyPacket;
import com.mixotc.imsdklib.packet.ReceivedMsgPacket;
import com.mixotc.imsdklib.utils.ImageUtils;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_EMAIL;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_LOGIN_CODE;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_LOGIN_USER;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_MSG_ID;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_PHONE;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_UPDATE_FRIEND_TIME;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_UPDATE_GROUP_TIME;

public class RemoteChatManager {
    private static final String TAG = RemoteChatManager.class.getSimpleName();
    private static final String NEW_MSG_BROADCAST = "imsdk.mixotc.newmsg.";
    private static final String LOGOUT_BROADCAST = "imsdk.mixotc.logout.";
    private static final String NEW_SYSTEM_MSG_BROADCAST = "imsdk.mixotc.newsystemmsg.";

    private static RemoteChatManager sInstance = new RemoteChatManager();
    private Context mContext;
    private RemoteNotifier mNotifier;
    private GOIMChatOptions mChatOptions;

    private RemoteChatManager() {
        mChatOptions = new GOIMChatOptions();
    }

    public static synchronized RemoteChatManager getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mContext = context;
        mNotifier = RemoteNotifier.getInstance(mContext);
    }

    public void onLoggedIn() {
        addPacketListeners();
    }

    // 登出时不清空数据库
    public void onLoggedOut(boolean clearDB) {
        Logger.e(TAG, "onLoggedOut: -- clear data -- remote");
        SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_PHONE, "");
        SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_EMAIL, "");
        SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_LOGIN_CODE, "");
        SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_LOGIN_USER, "");

        SharedPreferencesUtils.getInstance(mContext).putLong(KEY_UPDATE_FRIEND_TIME, 0);
        SharedPreferencesUtils.getInstance(mContext).putLong(KEY_UPDATE_GROUP_TIME, 0);

        removePacketListeners();

        RemoteGroupManager.getInstance().clear();
//        RemoteContactManager.getInstance().clear();
    }

    private void addPacketListeners() {
        RemoteConnectionManager.getInstance().addPacketListener(mNewMessageListener);
        RemoteConnectionManager.getInstance().addPacketListener(RemoteGroupManager.getInstance().getGroupNotifyListener());
        RemoteConnectionManager.getInstance().addPacketListener(RemoteContactManager.getInstance().getContactNotifyListener());
    }

    private void removePacketListeners() {
        RemoteConnectionManager.getInstance().removePacketListener(mNewMessageListener);
        RemoteConnectionManager.getInstance().removePacketListener(RemoteGroupManager.getInstance().getGroupNotifyListener());
        RemoteConnectionManager.getInstance().removePacketListener(RemoteContactManager.getInstance().getContactNotifyListener());
    }

    public void offlineMsgs(final long lastMid, final RemoteCallBack callBack) {
        RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
            @Override
            public void onReceivedPacket(BasePacket pkt) {
                if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                    ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                    if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.OFFLINEMSGS) {
                        RemoteConnectionManager.getInstance().removePacketListener(this);
                        final int ret = replyPacket.getResult();
                        String reason = replyPacket.getMessage();
                        Object data = replyPacket.getReplyData();
                        if (ret != 0) {
                            callbackOnError(callBack, ret, reason);
                            return;
                        }
                        if (data == null) {
                            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                            return;
                        }
                        JSONObject jsonData = (JSONObject) data;
                        int count = jsonData.optInt("count", -1);
                        JSONArray msgs = jsonData.optJSONArray("msgs");
                        if (msgs == null) {
                            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                            return;
                        }
                        long newLastMid = lastMid;
                        for (int j = 0; j < msgs.length(); j++) {
                            JSONObject msg = (JSONObject) msgs.opt(j);
                            if (msg == null) {
                                continue;
                            }
                            String type = msg.optString("type");
                            long mid = msg.optLong("id", -1);
                            if (mid > newLastMid) {
                                newLastMid = mid;
                            }
                            if (ReceivedMsgPacket.getMessageType(type) != GOIMMessage.Type.UNKNOWN) {
                                long gid = msg.optLong("gid", -1);
                                BasePacket packet = new BasePacket();
                                packet.setPacketType(gid == -1 ? BasePacket.PacketType.ANONYMOUS_CHAT_RECEIVE : BasePacket.PacketType.CHAT_RECEIVE);
                                packet.setPacketBody(msg.toString().getBytes());
                                RemoteConnectionManager.getInstance().onReceivedPacket(packet);
                            } else if (NotifyPacket.convertToNotifyPacketType(type) != NotifyPacket.NotifyPacketType.UNKNOWN) {
                                BasePacket packet = new BasePacket();
                                packet.setPacketType(BasePacket.PacketType.NOTIFY);
                                packet.setPacketBody(msg.toString().getBytes());
                                RemoteConnectionManager.getInstance().onReceivedPacket(packet);
                            }
                        }

                        if (count == msgs.length() && newLastMid > lastMid) {
                            offlineMsgs(newLastMid, callBack);
                        } else {
                            if (callBack != null) {
                                try {
                                    callBack.onSuccess(null);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });

        ControlPacket offlineMsgPacket = ControlPacket.createOfflineMsgPacket(lastMid);
        RemoteConnectionManager.getInstance().writeAndFlushPacket(offlineMsgPacket, callBack);
    }

    private void callbackOnSuccess(RemoteCallBack callBack, List result) {
        if (callBack != null) {
            try {
                callBack.onSuccess(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackOnProgress(RemoteCallBack callBack, int progress, String reason) {
        if (callBack != null) {
            try {
                callBack.onProgress(progress, reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackOnError(RemoteCallBack callBack, int code, String reason) {
        if (callBack != null) {
            try {
                callBack.onError(code, reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void notifyMessage(GOIMMessage message) {
        mNotifier.notifyChatMsg(message);
    }

    void notifySystemMessage() {
        mNotifier.notifySystemMsg();
    }

    void broadcastMessage(GOIMMessage message) {
        mNotifier.sendBroadcast(message);
    }

    public String getLogoutBroadcastAction() {
        return LOGOUT_BROADCAST + getBroadcastSuffix();
    }

    public String getNewMessageBroadcastAction() {
        return NEW_MSG_BROADCAST + getBroadcastSuffix();
    }

    public String getNewSystemMessageBroadcastAction() {
        return NEW_SYSTEM_MSG_BROADCAST + getBroadcastSuffix();
    }

    private String getBroadcastSuffix() {
        return "broadcast";
    }

    public void resetNotification() {
        if (mNotifier != null) {
            mNotifier.resetNotificationCount();
            mNotifier.cancelNotificaton();
        }
    }

    public void updateMessageBody(GOIMMessage message) {
        RemoteConversationManager.getInstance().updateMessageBody(message);
    }

    private void updateMessageState(GOIMMessage message) {
        RemoteConversationManager.getInstance().updateMessageStatus(message);
    }

    public GOIMChatOptions getChatOptions() {
        return mChatOptions;
    }

    public void setChatOptions(GOIMChatOptions chatOptions) {
        mChatOptions = chatOptions;
    }

    private void asyncFetchMessage(final GOIMMessage message) {
        Logger.e(TAG, "进入asyncFetchMessage " + message.getType().name() + ",hash code:" + message.hashCode());
        if (message.getType() == GOIMMessage.Type.IMAGE || message.getType() == GOIMMessage.Type.VIDEO || message.getType() == GOIMMessage.Type.VOICE) {
            //image和video先下thumb
            String remoteUrl = message.getType() == GOIMMessage.Type.IMAGE ? GOIMMessageUtils.getRemoteThumbUrl(message) : GOIMMessageUtils.getRemoteUrl(message);
//            String localFile = message.getType() == GOIMMessage.Type.VOICE ? GOIMMessageUtils.getLocalFilePath(message) : GOIMMessageUtils.getLocalThumbFilePath(message);
            String localFile = "";
            GOIMMessage.Type type = message.getType();
            if (type == GOIMMessage.Type.VOICE) {
                localFile = GOIMMessageUtils.getLocalFilePath(mContext, message);
            } else if (type == GOIMMessage.Type.IMAGE) {
                localFile = GOIMMessageUtils.getLocalThumbFilePath(mContext, message);
            } else if (type == GOIMMessage.Type.VIDEO) {
                localFile = GOIMMessageUtils.getLocalVideoPath(mContext, message);
            }
            if (TextUtils.isEmpty(localFile)) {
                return;
            }
            final FileMessageBody body = (FileMessageBody) message.getBody();
            message.setStatus(GOIMMessage.Status.INPROGRESS);
            updateMessageState(message);
            final String finalLocalFile = localFile;
            GOIMCloudFileManager.getInstance().downloadFile(remoteUrl, localFile, new RemoteCallBack.Stub() {
                @Override
                public void onSuccess(List result) {
                    if (message.getType() == GOIMMessage.Type.VOICE) {
                        Log.e(TAG, "异步接收消息 success" + message.getType().name());
                        body.setLocalUrl((String) result.get(0));
                        updateMessageBody(message);
                        body.mDownloaded = true;
                    }

                    if (message.getType() == GOIMMessage.Type.VIDEO) {
                        String imgPath = GOIMMessageUtils.getLocalThumbFilePath(mContext, message);
                        if (!TextUtils.isEmpty(finalLocalFile)) {
                            ImageUtils.saveVideoThumb(new File(finalLocalFile), imgPath, 160, 160, MediaStore.Video.Thumbnails.MINI_KIND);
                        }
                    }

                    message.setStatus(GOIMMessage.Status.SUCCESS);
                    updateMessageState(message);
                    callbackOnSuccess(body.mDownloadCallback, null);
                }

                @Override
                public void onError(int errorCode, String reason) {
                    message.setStatus(GOIMMessage.Status.FAIL);
                    Log.e(TAG, "异步接收消息 fail" + message.getType().name());
                    updateMessageState(message);
                    callbackOnError(body.mDownloadCallback, errorCode, reason);
                }

                @Override
                public void onProgress(int progress, String msg) {
                    message.setProgress(progress);
                    callbackOnProgress(body.mDownloadCallback, progress, msg);
                }
            });
        }
    }

    private PacketReceivedListener mNewMessageListener = new PacketReceivedListener() {
        @Override
        public void onReceivedPacket(BasePacket pkt) {
            if (pkt.getPacketType() == BasePacket.PacketType.CHAT_RECEIVE || pkt.getPacketType() == BasePacket.PacketType.ANONYMOUS_CHAT_RECEIVE) {
                Logger.e(TAG, "remote chat manager 处理packet");
                ReceivedMsgPacket receivedMsgPacket = new ReceivedMsgPacket(pkt);
                final GOIMMessage message = receivedMsgPacket.getMessage();
                if (message.getContact().getUid() == RemoteAccountManager.getInstance().getLoginUser().getUid()) {
                    return;
                }

                SharedPreferencesUtils.getInstance(mContext).putLong(KEY_LAST_MSG_ID, receivedMsgPacket.getMid());
                RemoteContactManager.getInstance().updateOrInsertIfNotExist(message.getContact());

                RemoteConversationManager.getInstance().receiveNewMessage(message, true);
                notifyMessage(message);

                // 2018/3/23 处理语音消息回调,download amr file immediately when receive the new message notification.
                if (receivedMsgPacket.getMessageType() == GOIMMessage.Type.IMAGE
                        || receivedMsgPacket.getMessageType() == GOIMMessage.Type.VOICE
                        || receivedMsgPacket.getMessageType() == GOIMMessage.Type.VIDEO) {
                    asyncFetchMessage(message);
                }
            }
        }
    };
}
