package com.mixotc.goimandroid;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.mixotc.imsdklib.AdminManager;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:05
 * Version  : v1.0.0
 * Describe :
 */
public class TheApplication extends Application {

    private static final Handler MAIN_LOOPER_HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        AdminManager.getInstance().initOnAppCreate(this, LocalService.class);
    }

    /**
     * 向主线程提交一个任务。
     */
    public static void postOnUiThread(Runnable runnable) {
        postRunnableByHandler(MAIN_LOOPER_HANDLER, runnable, 0);
    }

    /**
     * 向主线程提交一个延时任务。
     */
    public static void postOnUiThread(Runnable runnable, long delay) {
        postRunnableByHandler(MAIN_LOOPER_HANDLER, runnable, delay);
    }

    private static void postRunnableByHandler(Handler handler, Runnable runnable, long delay) {
        if (delay <= 0) {
            handler.post(runnable);
        } else {
            handler.postDelayed(runnable, delay);
        }
    }
}
