package com.mixotc.imsdklib.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.utils.Logger;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:41
 * Version  : v1.0.0
 * Describe : 在非主进程运行的Service，用来链接主进程和服务进程
 */
public class RemoteService extends Service {

    private static final String TAG = RemoteService.class.getSimpleName();

    private RemoteBinder mBinder = new RemoteBinder();

    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "chat service created");
        RemoteInitializer.getInstance().init(this);
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        Logger.d(TAG, "on start command!");
        return START_STICKY;
    }

    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        RemoteConnectionManager.getInstance().disconnect();
        super.onDestroy();
    }

    public IBinder onBind(Intent paramIntent) {
        Logger.d(TAG, "onBind");
        return mBinder;
    }

    public boolean onUnbind(Intent paramIntent) {
        Logger.d(TAG, "onUnBind");
        return true;
    }
}
