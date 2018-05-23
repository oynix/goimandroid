package com.mixotc.imsdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:41
 * Version  : v1.0.0
 * Describe :
 */
public class AgentService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
