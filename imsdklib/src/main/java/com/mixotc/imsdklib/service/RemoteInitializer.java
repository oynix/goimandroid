package com.mixotc.imsdklib.service;

import android.content.Context;
import android.content.Intent;

import com.mixotc.imsdklib.remotechat.RemoteAccountManager;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.remotechat.RemoteChatManager;
import com.mixotc.imsdklib.remotechat.RemoteContactManager;
import com.mixotc.imsdklib.remotechat.RemoteConversationManager;
import com.mixotc.imsdklib.remotechat.RemoteGroupManager;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.SharedPreferencesUtils;

import java.util.List;

import static com.mixotc.imsdklib.exception.ErrorType.ERROR_EXCEPTION_CODEINVALID;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_EMAIL;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_LOGIN_CODE;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_PHONE;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午7:08
 * Version  : v1.0.0
 * Describe : 远程初始化器
 */
public final class RemoteInitializer {

    private static final String TAG = RemoteInitializer.class.getSimpleName();

    private static final class LazyHolder {
        private static final RemoteInitializer INSTANCE = new RemoteInitializer();
    }

    public static RemoteInitializer getInstance() {
        return LazyHolder.INSTANCE;
    }

    // RemoteService启动时调用该方法, 初始化时尝试登录。
    public void init(final Context context) {
        Logger.e(TAG, "context == null ? " + (context == null));

        if (context == null) {
            return;
        }
        // 一共8个RemoteManager
        RemoteConnectionManager.getInstance().init(context);
        RemoteChatManager.getInstance().init(context);
        RemoteAccountManager.getInstance().init(context);
        RemoteContactManager.getInstance().init(context);
        RemoteGroupManager.getInstance().init(context);
        RemoteConversationManager.getInstance().init(context);

        Logger.d(TAG, "======尝试登录------");
        if (RemoteAccountManager.getInstance().isLoginPermit(context)) {
            String phone = SharedPreferencesUtils.getInstance(context).getString(KEY_LAST_PHONE, "");
            String email = SharedPreferencesUtils.getInstance(context).getString(KEY_LAST_EMAIL, "");
            String code = SharedPreferencesUtils.getInstance(context).getString(KEY_LAST_LOGIN_CODE, "");
            RemoteAccountManager.getInstance().login(phone, email, code, RemoteAccountManager.LOGIN_MODE_AUTO,
                    new RemoteCallBack.Stub() {

                        @Override
                        public void onSuccess(List result) {
                            Logger.d(TAG, "----------成功发送自动登录packet-------------");
                        }

                        @Override
                        public void onError(int errorCode, String reason) {
                            if (errorCode == ERROR_EXCEPTION_CODEINVALID || errorCode == ErrorType.ERROR_EXCEPTION_CODEERROR
                                    || errorCode == ErrorType.ERROR_EXCEPTION_OTHERLOGIN) {
                                RemoteChatManager.getInstance().onLoggedOut(false);
                                RemoteAccountManager.getInstance().onLoggedOut();

                                Intent intent = new Intent(RemoteChatManager.getInstance().getLogoutBroadcastAction());
                                context.sendOrderedBroadcast(intent, null);
                            } else {
                                RemoteConnectionManager.getInstance().reconnect();
                            }
                        }

                        @Override
                        public void onProgress(int progress, String message) {
                        }
                    });
        }
        Logger.d(TAG, "Service is initialized");
    }
}
