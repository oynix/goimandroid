package com.mixotc.imsdklib.utils;

import android.util.Log;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午2:18
 * Version  : v1.0.0
 * Describe : 日志打印辅助类
 */
public final class Logger {

    private static boolean sDebugMode = true;

    private Logger() {
    }

    public static void v(String tag, String msg) {
        if (!sDebugMode)
            return;
        Log.v(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (!sDebugMode)
            return;
        Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (!sDebugMode)
            return;
        Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (!sDebugMode)
            return;
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (!sDebugMode)
            return;
        Log.e(tag, msg);
    }
}
