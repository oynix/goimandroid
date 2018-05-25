package com.mixotc.imsdklib;

import android.app.Application;
import android.content.Context;

import com.mixotc.imsdklib.chat.manager.LocalAccountManager;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.manager.LocalContactManager;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.manager.LocalConversationManager;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.manager.LocalGroupManager;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.service.RemoteService;
import com.mixotc.imsdklib.utils.AppUtils;

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

    // 初始化
    /**
     * 主进程application创建时进行初始化，启动远程服务service.
     * 只需在主进程初始化一次即可。
     * <p>
     * <note>注意:️</note>
     * 必须在{@link Application#onCreate()}中调用该方法来完成服务service的创建初始化和绑定操作
     */
    public void initOnAppCreate(Context context, Class<? extends RemoteService> service) {
        if (AppUtils.getCurrentProcessName(context).equals(context.getPackageName())) {
            mContext = context.getApplicationContext();
            mBindServiceHelper = new BindServiceHelper(context, service);
            mBindServiceHelper.bind();
        }
    }

    // account
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
        LocalAccountManager.getInstance().sendLoginCode(phone, email, callBack);
    }

    /**
     * 在登录页面进行手动登录操作
     */
    public void login(String phone, String email, String code, RemoteCallBack callBack) {
        LocalAccountManager.getInstance().login(phone, email, code, callBack);
    }

    /**
     * 查看当前是否是登录状态
     */
    public boolean isLogin() {
        return LocalAccountManager.getInstance().isLoggedIn();
    }

    // contact
    // 应该提供以下的几种功能：
    // 1. 获取所有联系人
    // 2. 根据uid获取联系人，包括该联系人可能是非好友情况
    // 3. 根据uid判断一个用户是否是好友
    // 4.

    /**
     * 获取所有联系人
     */
    public Map<Long, GOIMContact> getContacts() {
        return LocalContactManager.getInstance().getContactList();
    }

    /**
     * 根据user id获取联系人，
     * @param uid
     * @return
     */
    public GOIMContact getContactById(long uid) {
        return LocalContactManager.getInstance().getContactByUid(uid);
    }

    // group

    /**
     * 获取所有群组
     */
    public Map<Long, GOIMGroup> getGroups() {
        return LocalGroupManager.getInstance().getGroupList();
    }

    // conversation

    /**
     * 获取所有对话
     */
    public Map<Long, GOIMConversation> getConversations() {
        return LocalConversationManager.getInstance().getAllConversations();
    }
}
