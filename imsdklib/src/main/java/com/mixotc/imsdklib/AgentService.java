package com.mixotc.imsdklib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:41
 * Version  : v1.0.0
 * Describe : 在非主进程运行的Service，用来链接主进程和服务进程
 */
public class AgentService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
