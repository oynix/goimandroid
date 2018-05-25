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

    /**
     * 判断当前进程是否在前台运行
     */
    public static boolean isAppRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List runningTasks = activityManager.getRunningTasks(1);
        boolean running = context.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo) runningTasks.get(0)).baseActivity.getPackageName());
        Logger.d("utils", "app running in foregroud：" + (running));
        return running;
    }

    /**
     * 获取当前进程名字，主进程是包名，其他进程视情况
     */
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

}
