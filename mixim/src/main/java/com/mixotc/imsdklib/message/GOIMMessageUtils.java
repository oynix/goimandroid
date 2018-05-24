package com.mixotc.imsdklib.message;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import com.mixotc.imsdklib.RemoteConfig;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.utils.FileUtils;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.PathUtil;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GOIMMessageUtils {
    private static final String TAG = GOIMMessageUtils.class.getSimpleName();

    private GOIMMessageUtils() {
    }

    public static int checkMessageError(GOIMMessage message) {
        if (message.getType() == GOIMMessage.Type.FILE) {
            String localUrl = ((FileMessageBody) message.getBody()).getLocalUrl();
            File localFile = new File(localUrl);
            if (!localFile.exists()) {
                Logger.e(TAG, "file doesn't exists:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_NOT_EXIST;
            }
            if (localFile.length() == 0L) {
                Logger.e(TAG, "file size is 0:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_SIZE_ZERO;
            }
        } else if (message.getType() == GOIMMessage.Type.IMAGE) {
            String localUrl = ((ImageMessageBody) message.getBody()).getLocalUrl();
            File localFile = new File(localUrl);
            if (!localFile.exists()) {
                Logger.e(TAG, "image doesn't exists:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_NOT_EXIST;
            }
            if (localFile.length() == 0L) {
                Logger.e(TAG, "image size is 0:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_SIZE_ZERO;
            }
        } else if (message.getType() == GOIMMessage.Type.VOICE) {
            String localUrl = ((VoiceMessageBody) message.getBody()).getLocalUrl();
            File localFile = new File(localUrl);
            if (!localFile.exists()) {
                Logger.e(TAG, "voice file doesn't exists:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_NOT_EXIST;
            }
            if (localFile.length() == 0L) {
                Logger.e(TAG, "voice file size is 0:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_SIZE_ZERO;
            }
        } else if (message.getType() == GOIMMessage.Type.VIDEO) {
            String localUrl = ((VideoMessageBody) message.getBody()).getLocalUrl();
            File localUrlFile = new File(localUrl);
            if (!localUrlFile.exists()) {
                Logger.e(TAG, "video file doesn't exists:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_NOT_EXIST;
            }
            if (localUrlFile.length() == 0L) {
                Logger.e(TAG, "video file size is 0:" + localUrl);
                return ErrorType.ERROR_EXCEPTION_FILE_SIZE_ZERO;
            }
        }
        return 0;
    }

    public static void asyncCallback(final RemoteCallBack callBack, final int errorCode, final String reason) {
        if (callBack == null) {
            return;
        }
        new Thread() {
            public void run() {
                try {
                    callBack.onError(errorCode, reason);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String getUniqueMessageId() {
        return Long.toHexString(System.currentTimeMillis());
    }

    public static String getRemoteUrl(GOIMMessage message) {
        if (message.getDirect() == GOIMMessage.Direct.RECEIVE && message.getBody() instanceof FileMessageBody) {
            FileMessageBody body = (FileMessageBody) message.getBody();
            return RemoteConfig.FILE_DOWNLOAD_URL + body.getRemoteId();
        }
        return null;
    }

    public static String getRemoteThumbUrl(GOIMMessage message) {
        if (message.getDirect() == GOIMMessage.Direct.RECEIVE && (message.getBody() instanceof ImageMessageBody || message.getBody() instanceof VideoMessageBody)) {
            FileMessageBody body = (FileMessageBody) message.getBody();
            return RemoteConfig.FILE_DOWNLOAD_URL + body.getRemoteId() + RemoteConfig.PARAM_OF_THUMB;
        }
        return null;
    }

    public static String getLocalFilePath(Context context, GOIMMessage message) {
        if (message.getBody() instanceof FileMessageBody) {
            FileMessageBody body = (FileMessageBody) message.getBody();
            if (message.getDirect() == GOIMMessage.Direct.RECEIVE) {
                if (body.getLocalUrl() == null || body.getLocalUrl().equals("") || body.getLocalUrl().equals("null")) {
                    File parent;
                    String mime = body.getMime();
                    if (mime.startsWith("image/")) {
                        parent = PathUtil.getInstance().getImagePath(context);
                    } else if (mime.startsWith("audio/")) {
                        parent = PathUtil.getInstance().getVoicePath(context);
                    } else if (mime.startsWith("video/")) {
                        parent = PathUtil.getInstance().getVideoPath(context);
                    } else {
                        parent = PathUtil.getInstance().getFilePath(context);
                    }
                    File file = new File(parent, body.getRemoteId() + "." + FileUtils.getFileExtension(body.getMime()));
                    return file.getAbsolutePath();
                }
            }
            return body.getLocalUrl();
        }
        return null;
    }

    public static String getLocalThumbFilePath(Context context, GOIMMessage message) {
        if (message.getBody() instanceof ImageMessageBody || message.getBody() instanceof VideoMessageBody) {
            String localUrl = getLocalFilePath(context, message);
            if (!TextUtils.isEmpty(localUrl) && !localUrl.equals("null")) {
                File file = new File(PathUtil.getInstance().getThumbPath(context), MD5(localUrl) + ".jpg");
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String getLocalVideoPath(Context context, GOIMMessage message) {
        if (message.getBody() instanceof ImageMessageBody || message.getBody() instanceof VideoMessageBody) {
            String localUrl = getLocalFilePath(context, message);
            if (!TextUtils.isEmpty(localUrl) && !localUrl.equals("null")) {
                File file = new File(PathUtil.getInstance().getVideoPath(context), MD5(localUrl) + ".mp4");
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String MD5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
