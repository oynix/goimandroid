package com.mixotc.imsdklib;

import android.content.Context;

import com.mixotc.imsdklib.chat.GOIMAccountManager;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMContactManager;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.service.RemoteService;

import java.util.Map;

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

    private Context mContext;
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
        mContext = context.getApplicationContext();
        mBindServiceHelper = new BindServiceHelper(context, service);
        mBindServiceHelper.bind();
    }

    /**
     * 接触与服务进程的绑定
     */
    public void unbindService() {
        mBindServiceHelper.unbind();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public RemoteServiceBinder getBinder() {
        if (mBindServiceHelper != null) {
            return mBindServiceHelper.getBinder();
        }
        return null;
    }

    /**
     * 获取登录验证码
     */
    public void sendLoginCode(String phone, String email, RemoteCallBack callBack) {
        GOIMAccountManager.getInstance().sendLoginCode(phone, email, callBack);
    }

    /**
     * 在登录页面进行手动登录操作
     */
    public void login(String phone, String email, String code, RemoteCallBack callBack) {
        GOIMAccountManager.getInstance().login(phone, email, code, callBack);
    }

    /**
     * 获取所有联系人
     */
    public Map<Long, GOIMContact> getContacts() {
        return GOIMContactManager.getInstance().getContactList();
    }
}
