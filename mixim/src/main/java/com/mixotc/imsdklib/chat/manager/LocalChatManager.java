package com.mixotc.imsdklib.chat.manager;

import android.os.RemoteException;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.chat.GOIMChatOptions;
import com.mixotc.imsdklib.exception.GOIMException;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.message.GOIMMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalChatManager {
    private static final String TAG = "LocalChatManager";

    private static final class LazyHolder {
        private static final LocalChatManager INSTANCE = new LocalChatManager();
    }

    public ExecutorService mMsgCountThreadPool;

    private LocalChatManager() {
        mMsgCountThreadPool = Executors.newSingleThreadExecutor();
    }

    public static synchronized LocalChatManager getInstance() {
        return LazyHolder.INSTANCE;
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
//        LocalChatDBProxy.getInstance().f(message.getMsgId(), true);
    }

    public GOIMMessage getMessage(String msgId) {
        return LocalConversationManager.getInstance().getMessage(msgId);
    }

    public int getUnreadMsgCount() {
        return LocalConversationManager.getInstance().getUnreadMessageCount() + LocalChatDBProxy.getInstance().getUnreadSystemMsgs();
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
        return LocalChatDBProxy.getInstance().updateMessageBody(message);
    }

    private void updateMessageState(GOIMMessage message) {
        LocalChatDBProxy.getInstance().updateMsgStatus(message.getMsgId(), message.getStatus().ordinal());
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
