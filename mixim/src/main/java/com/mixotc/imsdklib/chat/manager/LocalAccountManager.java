package com.mixotc.imsdklib.chat.manager;

import android.os.RemoteException;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteLoggedStatusListener;
import com.mixotc.imsdklib.utils.Logger;

/**
 * Created by junnikokuki on 2017/9/8.
 */

public class LocalAccountManager {

    private static final String TAG = LocalAccountManager.class.getSimpleName();

    private static final class LazyHolder {
        private static final LocalAccountManager INSTANCE = new LocalAccountManager();
    }

//    private Context mContext = null;

    private LocalAccountManager() {
//        mContext = AdminManager.getInstance().getApplicationContext();
    }

    public static LocalAccountManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 获取登录验证码
     */
    public void sendLoginCode(String phone, String email, RemoteCallBack callBack) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder == null)
            return;
        try {
            binder.sendCode(phone, email, callBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查是否登录状态
     */
    public boolean isLoggedIn() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder == null)
            return false;
        try {
            return binder.isLogin();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 进行登录操作,登录成功会通过RemoteLoggedStatusListener回调通知
     */
    public void login(final String phone, final String email, final String code, final RemoteCallBack callBack) {
        Logger.d(TAG, "manual login");
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder == null)
            return;
        try {
            binder.login(phone, email, code, callBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主动进行登出操作，成功会通过RemoteLoggedStatusListener回调通知
     */
    public void logout(final RemoteCallBack callBack) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder == null)
            return;
        try {
            binder.logout(callBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前的登录的用户
     */
    public GOIMContact getLoginUser() {
        GOIMContact loginUser = null;
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder == null)
            return null;
        try {
            loginUser = binder.getLoginUser();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return loginUser;
    }

    /**
     * 获取用户信息
     *
     * @param userId   ID
     * @param callBack 回调
     */
//    public void getUserInfo(long userId, final GOIMCallBack callBack) {
//        if (!doCheck(callBack)) {
//            return;
//        }
//        try {
//            GOIMConnectionManager.getInstance().addPacketListener(new PacketListener() {
//                @Override
//                public void processPacket(BasePacket pkt) {
//                    ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
//                    final int ret = replyPacket.getResult();
//                    String reason = replyPacket.getMessage();
//                    Object data = replyPacket.getReplyData();
//                    if (ret == 0) {
//                        if (data != null) {
//                            Gson gson = new Gson();
//                            Contacts contact = gson.fromJson(data.toString(), Contacts.class);
//                            callBack.onSuccess(contact);
//                        } else {
//                            callBack.onError(GOIMCallBack.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
//                        }
//                    } else {
//                        callBack.onError(ret, reason);
//                    }
//                    GOIMConnectionManager.getInstance().removePacketListener(this);
//                }
//            }, new PacketFilter() {
//                @Override
//                public boolean accept(BasePacket packet) {
//                    if (packet.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
//                        ControlReplyPacket replyPacket = new ControlReplyPacket(packet);
//                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.USERINFO) {
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//            });
//            GOIMConnectionManager.getInstance().userInfo(userId);
//        } catch (GOIMException e) {
//            e.printStackTrace();
//            callBack.onError(GOIMCallBack.ERROR_EXCEPTION_SERVER_CONNECTION, null);
//        }
//    }


    public RemoteLoggedStatusListener mLoggedStatusListener = new RemoteLoggedStatusListener.Stub() {
        @Override
        public void onLoggedIn() {
            Logger.d(TAG, "-------------------initialize Manager Data: -- local" + Thread.currentThread().getName());
            LocalContactManager.getInstance().initData();
            LocalGroupManager.getInstance().initData();
            LocalConversationManager.getInstance().initData();
            Logger.d(TAG, "-------------------after initialize Manager Data: -- local");
        }

        @Override
        public void onLoggedOut() {
            Logger.d(TAG, "-------------------clear Manager Data: -- local");
            LocalContactManager.getInstance().clear();
            LocalGroupManager.getInstance().clear();
            LocalConversationManager.getInstance().clear();
        }
    };
}
