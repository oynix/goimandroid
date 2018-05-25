package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.os.Environment;

import com.mixotc.imsdklib.remotechat.RemoteAccountManager;

import java.io.File;

public class UserFilePathManager {

    private static final String IMAGE_PATH_NAME = "/images/";
    private static final String VOICE_PATH_NAME = "/voices/";
    private static final String FILE_PATH_NAME = "/files/";
    private static final String VIDEO_PATH_NAME = "/videos/";
    private static final String THUMB_PATH_NAME = "/thumbs/";
    private static final String TEMP_PATH_NAME = "/temp/";

    private static UserFilePathManager sInstance = null;

    private String mPathPrefix;

    private File mStoragePath = null;
    private File mVoicePath = null;
    private File mImagePath = null;
    private File mVideoPath = null;
    private File mFilePath = null;
    private File mThumbPath = null;
    private File mTempPath = null;

    private UserFilePathManager() {
    }

    public static UserFilePathManager getInstance() {
        if (sInstance == null) {
            sInstance = new UserFilePathManager();
        }
        return sInstance;
    }

    public void initDirs(long uid, Context context) {
        mPathPrefix = "/Android/data/" + context.getPackageName() + "/";

        mVoicePath = generatePath(context, uid, VOICE_PATH_NAME);
        mImagePath = generatePath(context, uid, IMAGE_PATH_NAME);
        mVideoPath = generatePath(context, uid, VIDEO_PATH_NAME);
        mFilePath = generatePath(context, uid, FILE_PATH_NAME);
        mThumbPath = generatePath(context, uid, THUMB_PATH_NAME);
        mTempPath = generatePath(context, uid, TEMP_PATH_NAME);
    }

    private File generatePath(Context context, long uid, String pathName) {
        if (mStoragePath == null) {
            mStoragePath = Environment.getExternalStorageDirectory();
            if (!mStoragePath.exists() || !mStoragePath.canWrite()) {
                mStoragePath = context.getFilesDir();
            }
        }
        File file = new File(mStoragePath, mPathPrefix + uid + pathName);
        if (file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public File getImagePath(Context context) {
        if (mImagePath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return mImagePath;
    }

    public File getVoicePath(Context context) {
        if (mVoicePath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return mVoicePath;
    }

    public File getFilePath(Context context) {
        if (mFilePath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return mFilePath;
    }

    public File getVideoPath(Context context) {
        if (mVideoPath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return mVideoPath;
    }

    public File getThumbPath(Context context) {
        if (mThumbPath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return mThumbPath;
    }

    public File getTempPath(Context context) {
        if (mTempPath == null) {
            initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), context);
        }
        return mTempPath;
    }
}
