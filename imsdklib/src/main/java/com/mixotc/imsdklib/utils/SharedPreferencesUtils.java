package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.content.SharedPreferences;

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

    public void putLong(String id, long value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putLong(id, value).apply();
    }

}
