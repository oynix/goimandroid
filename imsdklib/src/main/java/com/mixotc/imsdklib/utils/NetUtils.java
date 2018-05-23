package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午5:09
 * Version  : v1.0.0
 * Describe :
 */
public final class NetUtils {

    private NetUtils() {}

    public static boolean isNetConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager == null) {
                return false;
            }
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
