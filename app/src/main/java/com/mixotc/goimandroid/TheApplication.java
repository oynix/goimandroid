package com.mixotc.goimandroid;

import android.app.Application;

import com.mixotc.imsdklib.AdminManager;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:05
 * Version  : v1.0.0
 * Describe :
 */
public class TheApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AdminManager.getInstance().initOnAppCreate(this, LocalService.class);
    }
}
