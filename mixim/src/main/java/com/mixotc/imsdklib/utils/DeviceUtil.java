package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.text.TextUtils;

import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_IMEI;

/**
 * Author   : xiaoyu
 * Date     : 2018/4/13 下午4:05
 * Version  : v1.0.0
 * Describe :
 */
public class DeviceUtil {

    public static String getIMEI(Context context) {
        if (context == null) {
            return "";
        }
        // get imei of phone will fail for permission, so just mock temporary
        String deviceId = "1213812138";
//        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        if (manager != null) {
//            try {
//                deviceId = manager.getDeviceId();
//            } catch (Exception e) {
//                e.printStackTrace();
//                deviceId = "";
//            }
//        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = SharedPreferencesUtils.getInstance(context).getString(KEY_IMEI, "");
        } else {
            SharedPreferencesUtils.getInstance(context).putString(KEY_IMEI, deviceId);
        }
        return deviceId;
    }

}
