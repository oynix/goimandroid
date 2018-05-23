package com.mixotc.imsdklib.service;

import android.content.Context;

import com.mixotc.imsdklib.account.RemoteAccountManager;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午7:08
 * Version  : v1.0.0
 * Describe : 远程初始化器
 */
public final class RemoteInitializer {

    private static final class LazyHolder {
        private static final RemoteInitializer INSTANCE = new RemoteInitializer();
    }

    public static RemoteInitializer getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(Context context) {
        RemoteConnectionManager.getInstance().init(context);
        RemoteAccountManager.getInstance().init(context);
    }

}
