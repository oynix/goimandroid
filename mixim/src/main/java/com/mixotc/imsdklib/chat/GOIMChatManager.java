package com.mixotc.imsdklib.chat;

import android.os.RemoteException;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.exception.GOIMException;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.utils.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GOIMChatManager {
    private static final String TAG = "GOIMChatManager";

    private static GOIMChatManager sInstance;

    public ExecutorService mMsgCountThreadPool;

    private GOIMChatManager() {
        mMsgCountThreadPool = Executors.newSingleThreadExecutor();
    }

    public static synchronized GOIMChatManager getInstance() {
        if (sInstance == null) {
            synchronized (GOIMChatManager.class) {
                if (sInstance == null) {
                    sInstance = new GOIMChatManager();
                }
            }
        }
        return sInstance;
    }

    // 登录成功后回调，初始化Local数据
    public void initManagerData() {
        Logger.e(TAG, "~~~~~~~~~~~~~~~~~~~initialize Manager Data: -- local");
        GOIMContactManager.getInstance().initData();
        GOIMGroupManager.getInstance().initData();
        GOIMConversationManager.getInstance().initData();
        Logger.e(TAG, "~~~~~~~~~~~~~~~~~~~after initialize Manager Data: -- local");
    }

    public void clearManagerData() {
        Logger.e(TAG, "~~~~~~~~~~~~~~~~~~~clear Manager Data: -- local");
        GOIMContactManager.getInstance().clear();
        GOIMGroupManager.getInstance().clear();
        GOIMConversationManager.getInstance().clear();
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

    private void callbackOnSuccess(RemoteCallBack callBack, List result) {
        if (callBack != null) {
            try {
                callBack.onSuccess(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    public String getLogoutBroadcastAction() {
//        if (AdminManager.getInstance().getBinder() != null) {
//            try {
//                return AdminManager.getInstance().getBinder().getLogoutBroadcastAction();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
        return "";
    }

//    public String getNewMessageBroadcastAction() {
//        if (AdminManager.getInstance().getBinder() != null) {
//            try {
//                return AdminManager.getInstance().getBinder().getNewMessageBroadcastAction();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//        return "";
//    }

//    public String getNewSystemMessageBroadcastAction() {
//        if (AdminManager.getInstance().getBinder() != null) {
//            try {
//                return AdminManager.getInstance().getBinder().getNewSystemMessageBroadcastAction();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//        return "";
//    }

    public void ackMessageRead(long userId, String msgId) throws GOIMException {
//        if (!chatOptions.getRequireAck()) {
//            GOIMLog.d(TAG, "chat option reqire ack set to false. skip send out ask msg read");
//            return;
//        }
//        GOIMConnectionManager.getInstance().ackMessageRead(userId, msgId);
    }

    public void setMessageListened(GOIMMessage message) {
//        message.setListened(true);
//        GOIMChatDBProxy.getInstance().f(message.getMsgId(), true);
    }

    public GOIMMessage getMessage(String msgId) {
        return GOIMConversationManager.getInstance().getMessage(msgId);
    }

    public int getUnreadMsgCount() {
        return GOIMConversationManager.getInstance().getUnreadMessageCount() + GOIMChatDBProxy.getInstance().getUnreadSystemMsgs();
    }

    public void activityResumed() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.resetNotification();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean updateMessageBody(GOIMMessage message) {
        return GOIMChatDBProxy.getInstance().updateMessageBody(message);
    }

    private void updateMessageState(GOIMMessage message) {
        GOIMChatDBProxy.getInstance().updateMsgStatus(message.getMsgId(), message.getStatus().ordinal());
    }

    public GOIMChatOptions getChatOptions() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.getChatOptions();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new GOIMChatOptions();
    }

    public void setChatOptions(GOIMChatOptions chatOptions) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.setChatOptions(chatOptions);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void asyncFetchMessage(final GOIMMessage message) {
//        if (message.getStatus() == GOIMMessage.Status.INPROGRESS) {
//            return;
//        }
//        if (message.getType() == GOIMMessage.Type.IMAGE || message.getType() == GOIMMessage.Type.VIDEO || message.getType() == GOIMMessage.Type.VOICE) {
//            //image和video先下thumb
//            String remoteUrl = message.getType() == GOIMMessage.Type.IMAGE ? GOIMMessageUtils.getRemoteThumbUrl(message) : GOIMMessageUtils.getRemoteUrl(message);
////            String localFile = message.getType() == GOIMMessage.Type.VOICE ? GOIMMessageUtils.getLocalFilePath(message) : GOIMMessageUtils.getLocalThumbFilePath(message);
//            String localFile = "";
//            GOIMMessage.Type type = message.getType();
//            if (type == GOIMMessage.Type.VOICE) {
//                localFile = GOIMMessageUtils.getLocalFilePath(message);
//            } else if (type == GOIMMessage.Type.IMAGE) {
//                localFile = GOIMMessageUtils.getLocalThumbFilePath(message);
//            } else if (type == GOIMMessage.Type.VIDEO) {
//                localFile = GOIMMessageUtils.getLocalVideoPath(message);
//            }
//            if (TextUtils.isEmpty(localFile)) {
//                return;
//            }
//            final FileMessageBody body = (FileMessageBody) message.getBody();
//            message.setStatus(GOIMMessage.Status.INPROGRESS);
//            final String finalLocalFile = localFile;
//            GOIMCloudFileManager.getInstance().downloadFile(remoteUrl, localFile, new GOIMCallBack() {
//                @Override
//                public void onSuccess(Object result) {
//                    if (message.getType() == GOIMMessage.Type.VOICE) {
//                        body.setLocalUrl((String) result);
//                        updateMessageBody(message);
//                        body.mDownloaded = true;
//                    }
//                    if (message.getType() == GOIMMessage.Type.VIDEO) {
//                        String imgPath = GOIMMessageUtils.getLocalThumbFilePath(message);
//                        if (!TextUtils.isEmpty(finalLocalFile)) {
//                            ImageUtils.saveVideoThumb(new File(finalLocalFile), imgPath, 160, 160, MediaStore.Video.Thumbnails.MINI_KIND);
//                        }
//                    }
//
//                    message.setStatus(GOIMMessage.Status.SUCCESS);
//                    updateMessageState(message);
//                    if (body.mDownloadCallback != null) {
//                        body.mDownloadCallback.onSuccess(null);
//                    }
//                }
//
//                @Override
//                public void onError(int errorCode, String reason) {
//                    message.setStatus(GOIMMessage.Status.FAIL);
//                    updateMessageState(message);
//                    if (body.mDownloadCallback != null) {
//                        body.mDownloadCallback.onError(errorCode, reason);
//                    }
//                }
//
//                @Override
//                public void onProgress(int progress, String msg) {
//                    message.setProgress(progress);
//                    if (body.mDownloadCallback != null) {
//                        body.mDownloadCallback.onProgress(progress, msg);
//                    }
//                }
//            });
//        }
    }
}
