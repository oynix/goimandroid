package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Base64;

import com.mixotc.imsdklib.chat.GOIMContact;

import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_LOGIN_USER;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午6:47
 * Version  : v1.0.0
 * Describe :
 */
public final class SharedPreferencesUtils {

    private static final String SP_NAME = "com_mixotc_imsdklib.dat";
    private final SharedPreferences mSharedPreferences;

    private SharedPreferencesUtils(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    private volatile static SharedPreferencesUtils sInstance;

    public static SharedPreferencesUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreferencesUtils(context);
                }
            }
        }
        return sInstance;
    }

    public void putString(String id, String str) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(id, str).apply();
    }

    public String getString(String id, String value) {
        return mSharedPreferences.getString(id, value);
    }

    public void putLong(String id, long value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putLong(id, value).apply();
    }

    public long getLong(String key, long value) {
        return mSharedPreferences.getLong(key, value);
    }

    public GOIMContact getLastLoginUser() {
        GOIMContact lastLoginUser = null;
        String hash = mSharedPreferences.getString(KEY_LAST_LOGIN_USER, "");
        if (!TextUtils.isEmpty(hash)) {
            byte[] bytes = Base64.decode(hash.getBytes(), Base64.DEFAULT);
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);
            lastLoginUser = parcel.readParcelable(GOIMContact.class.getClassLoader());
            parcel.recycle();
        }
        return lastLoginUser;
    }

    public void setLastLoginUser(GOIMContact user) {
        if (user == null) {
            return;
        }
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(user, 0);
        byte[] bytes = parcel.marshall();
        String hash = Base64.encodeToString(bytes, Base64.DEFAULT);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(KEY_LAST_LOGIN_USER, hash);
        edit.apply();
        parcel.recycle();
    }

    /**
     * 根据传入的key值从SharedPreferences中获取value，并和传入的time对比大小，返回结果。
     */
    public boolean updateTime(String key, long time) {
        long lastUpdateTime = mSharedPreferences.getLong(key, -1);
        if (time > lastUpdateTime) {
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putLong(key, time);
            edit.apply();
            return true;
        }
        return false;
    }

}
