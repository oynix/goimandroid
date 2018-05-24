package com.mixotc.imsdklib;

import android.app.Application;
import android.content.Context;

import com.mixotc.imsdklib.chat.GOIMAccountManager;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMContactManager;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMConversationManager;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.GOIMGroupManager;
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

    public Context getApplicationContext() {
        return mContext;
    }

    public RemoteServiceBinder getBinder() {
        if (mBindServiceHelper != null) {
            return mBindServiceHelper.getBinder();
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////以下是给调用者提供的方法////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 主进程application创建时进行初始化，启动远程服务service
     * <p>
     * <note>注意:️</note>
     * 必须在{@link Application#onCreate()}中调用该方法来完成服务service的创建初始化和绑定操作
     */
    public void initOnAppCreate(Context context, Class<? extends RemoteService> service) {
        mContext = context.getApplicationContext();
        mBindServiceHelper = new BindServiceHelper(context, service);
        mBindServiceHelper.bind();
    }

    /**
     * 解除与服务进程的绑定
     */
    public void unbindService() {
        mBindServiceHelper.unbind();
    }

    // account
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
     * 查看当前是否是登录状态
     */
    public boolean isLogin() {
        return GOIMAccountManager.getInstance().isLoggedIn();
    }

    // contact
    /**
     * 获取所有联系人
     */
    public Map<Long, GOIMContact> getContacts() {
        return GOIMContactManager.getInstance().getContactList();
    }

    // group

    /**
     * 获取所有群组
     */
    public Map<Long, GOIMGroup> getGroups() {
        return GOIMGroupManager.getInstance().getGroupList();
    }

    // conversation

    /**
     * 获取所有对话
     */
    public Map<Long, GOIMConversation> getConversations() {
        return GOIMConversationManager.getInstance().getAllConversations();
    }
}
