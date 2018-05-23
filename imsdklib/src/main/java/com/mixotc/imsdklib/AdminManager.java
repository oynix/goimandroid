package com.mixotc.imsdklib;

import android.content.Context;

import com.mixotc.imsdklib.service.AgentService;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:09
 * Version  : v1.0.0
 * Describe :
 * <p>
 * 该类是单例，是整个IM SDK的管理者，所有的操作均需要通过该类提供的方法调用。
 */
public final class AdminManager {

    private BindServiceHelper mBindServiceHelper;

    private static class LazyHolder {
        private static final AdminManager INSTANCE = new AdminManager();
    }

    private AdminManager() {
    }

    public static AdminManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 主进程application创建时进行初始化，启动远程服务service
     */
    public void initOnAppCreate(Context context, Class<? extends AgentService> service) {
        mBindServiceHelper = new BindServiceHelper(context, service);
        mBindServiceHelper.bind();
    }

}
