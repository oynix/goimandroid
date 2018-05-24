package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.os.Environment;

import com.mixotc.imsdklib.account.RemoteAccountManager;

import java.io.File;

public class PathUtil {
    private static String pathPrefix;
    private static final String imagePathName = "/images/";
    private static final String voicePathName = "/voices/";
    private static final String filePathName = "/files/";
    private static final String videoPathName = "/videos/";
    private static final String thumbPathName = "/thumbs/";
    private static final String tempPathName = "/temp/";
    private static File storageDir = null;
    private static PathUtil instance = null;
    private File voicePath = null;
    private File imagePath = null;
    private File videoPath = null;
    private File filePath = null;
    private File thumbPath = null;
    private File tempPath = null;

    private PathUtil() {
    }

    public static PathUtil getInstance() {
        if (instance == null) {
            instance = new PathUtil();
        }
        return instance;
    }

    public void initDirs(long uid, Context context) {
        String str = context.getPackageName();
        // TODO: 2018/5/24 路径获取方式待修改
        pathPrefix = "/Android/data/" + str + "/";
        voicePath = generateVoicePath(uid, context);
        if (!voicePath.exists()) {
            voicePath.mkdirs();
        }
        imagePath = generateImagePath(uid, context);
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }
        videoPath = generateVideoPath(uid, context);
        if (!videoPath.exists()) {
            videoPath.mkdirs();
        }
        filePath = generateFiePath(uid, context);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        thumbPath = generateThumbPath(uid, context);
        if (!thumbPath.exists()) {
            thumbPath.mkdirs();
        }
        tempPath = generateTempPath(uid, context);
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
    }

    public File getImagePath(Context context) {
        if (imagePath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return imagePath;
    }

    public File getVoicePath(Context context) {
        if (voicePath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return voicePath;
    }

    public File getFilePath(Context context) {
        if (filePath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return filePath;
    }

    public File getVideoPath(Context context) {
        if (videoPath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return videoPath;
    }

    public File getThumbPath(Context context) {
        if (thumbPath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return thumbPath;
    }

    public File getTempPath(Context context) {
        if (tempPath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return tempPath;
    }

    private static File getStorageDir(Context context) {
        if (storageDir == null) {
            File localFile = Environment.getExternalStorageDirectory();
            if ((localFile.exists()) && (localFile.canWrite())) {
                return localFile;
            }
            storageDir = context.getFilesDir();
        }
        return storageDir;
    }

    private static File generateImagePath(long uid, Context context) {
        String str = pathPrefix + uid + imagePathName;
        return new File(getStorageDir(context), str);
    }

    private static File generateVoicePath(long uid, Context context) {
        String str = pathPrefix + uid + voicePathName;
        return new File(getStorageDir(context), str);
    }

    private static File generateFiePath(long uid, Context context) {
        String str = pathPrefix + uid + filePathName;
        return new File(getStorageDir(context), str);
    }

    private static File generateVideoPath(long uid, Context context) {
        String str = pathPrefix + uid + videoPathName;
        return new File(getStorageDir(context), str);
    }

    private static File generateThumbPath(long uid, Context context) {
        String str = pathPrefix + uid + thumbPathName;
        return new File(getStorageDir(context), str);
    }

    private static File generateTempPath(long uid, Context context) {
        String str = pathPrefix + uid + tempPathName;
        return new File(getStorageDir(context), str);
    }
}
