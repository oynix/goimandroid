package com.mixotc.imsdklib.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 上午11:00
 * Version  : v1.0.0
 * Describe :
 */
public final class AppUtils {

    private AppUtils() {}

    public static boolean isAppRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List runningTasks = activityManager.getRunningTasks(1);
        boolean running = context.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo) runningTasks.get(0)).baseActivity.getPackageName());
        Logger.d("utils", "app running in foregroud：" + (running));
        return running;
    }

}
