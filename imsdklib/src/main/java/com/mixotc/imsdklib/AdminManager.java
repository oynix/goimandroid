package com.mixotc.imsdklib;

import android.content.Context;
import android.os.RemoteException;

import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.service.RemoteService;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 上午11:09
 * Version  : v1.0.0
 * Describe :
 * <p>
 * 该类是单例，是整个IM SDK的管理者，所有的操作均需要通过该类提供的方法调用。
 */
public final class AdminManager {

    private static final String TAG = AdminManager.class.getSimpleName();

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
    public void initOnAppCreate(Context context, Class<? extends RemoteService> service) {
        mBindServiceHelper = new BindServiceHelper(context, service);
        mBindServiceHelper.bind();
    }

    /**
     * 获取登录验证码
     */
    public void sendLoginCode(String phone, String email, RemoteCallBack callBack) {
        RemoteServiceBinder binder = mBindServiceHelper.getBinder();
        if (binder == null)
            return;
        try {
            binder.sendCode(phone, email, callBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在登录页面进行手动登录操作
     */
    public void login(String phone, String email, String code, RemoteCallBack callBack) {
        RemoteServiceBinder binder = mBindServiceHelper.getBinder();
        if (binder == null)
            return;
        try {
            binder.login(phone, email, code, callBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
