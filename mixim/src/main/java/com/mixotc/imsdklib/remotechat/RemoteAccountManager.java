package com.mixotc.imsdklib.remotechat;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.mixotc.imsdklib.BuildConfig;
import com.mixotc.imsdklib.RemoteConfig;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteCallbackAdapter;
import com.mixotc.imsdklib.listener.RemoteConnectionListener;
import com.mixotc.imsdklib.listener.RemoteLoggedStatusListener;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.LoginPacket;
import com.mixotc.imsdklib.packet.LogoutPacket;
import com.mixotc.imsdklib.packet.ReplyPacket;
import com.mixotc.imsdklib.packet.SendCodePacket;
import com.mixotc.imsdklib.utils.DeviceUtil;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.UserFilePathManager;
import com.mixotc.imsdklib.utils.SharedPreferencesIds;
import com.mixotc.imsdklib.utils.SharedPreferencesUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_EMAIL;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_LOGIN_CODE;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_MSG_ID;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_PHONE;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LOGIN_CODE_VALID_TIME;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_UPDATE_FRIEND_TIME;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_UPDATE_GROUP_TIME;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午6:38
 * Version  : v1.0.0
 * Describe :
 */
public class RemoteAccountManager {

    private static final String TAG = RemoteAccountManager.class.getSimpleName();

    private static final class LazyHolder {
        private static final RemoteAccountManager INSTANCE = new RemoteAccountManager();
    }

    /** 登录模式：手动登录，即指从登录页面登录 */
    public static final int LOGIN_MODE_MANUAL = 0;
    /** 登录模式：自动登录，即指打开应用后自动尝试登录 */
    public static final int LOGIN_MODE_AUTO = 1;
    /** 登录模式：重新登录，即指使用过程中断线重连成功后登录 */
    public static final int LOGIN_MODE_RECONNECT = 2;

    private RemoteAccountManager() {
    }

    public static RemoteAccountManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Context mContext;
    private final List<RemoteLoggedStatusListener> mLoggedListeners = new ArrayList<>();

    public void init(Context context) {
        mContext = context;
    }

    /**
     * 获取登录验证码.
     * 先进行链接服务器尝试建立channel，若成功则发送请求验证码的packet，并监听服务器reply packet。
     *
     * @param phone    手机号
     * @param email    邮箱
     * @param callBack 获取操作回调
     */
    public void sendLoginCode(final String phone, final String email, final RemoteCallBack callBack) {
        final PacketReceivedListener packetReceivedListener = new PacketReceivedListener() {
            @Override
            public void onReceivedPacket(BasePacket packet) {
                if (packet.getPacketType() == BasePacket.PacketType.SENDCODE_REPLY) {
                    RemoteConnectionManager.getInstance().removePacketListener(this);
                    ReplyPacket replyPacket = new ReplyPacket(packet);
                    int ret = replyPacket.getResult();
                    String reason = replyPacket.getMessage();
                    JSONObject data = replyPacket.getData();
                    long validTime = data.optLong("valid_time", -1);
                    if (ret == 0 || ret == ErrorType.ERROR_EXCEPTION_CODESENT) {
                        SharedPreferencesUtils.getInstance(mContext).putLong(SharedPreferencesIds.KEY_LOGIN_CODE_VALID_TIME, System.currentTimeMillis() + validTime * 1000);
                        callbackOnSuccess(callBack, null);
                    } else {
                        callbackOnError(callBack, ret, reason);
                    }
                }
            }
        };
        RemoteConnectionManager.getInstance().addConnectionListener(new RemoteConnectionListener.Stub() {
            @Override
            public void onConnected() {
                RemoteConnectionManager.getInstance().removeConnectionListener(this);
                RemoteConnectionManager.getInstance().addPacketListener(packetReceivedListener);
                SendCodePacket pkt = new SendCodePacket(phone, email);
                RemoteConnectionManager.getInstance().writeAndFlushPacket(pkt, callBack, false);
            }

            @Override
            public void onDisconnected() {
                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNABLE_CONNECT_TO_SERVER, null);
                RemoteConnectionManager.getInstance().removeConnectionListener(this);
            }

            @Override
            public void onError(String reason) {
                RemoteConnectionManager.getInstance().removeConnectionListener(this);
                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNABLE_CONNECT_TO_SERVER, null);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connect = RemoteConnectionManager.getInstance().connect();
                if (!connect) {
                    callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNKNOWN_SERVER_HOST, null);
                }
            }
        }).start();
    }

    /**
     * 进行登录操作 1 初始化RemoteService时调用即自动登录；2 手动登录时调用；3 断线重连时调用
     */
    public void login(final String phone, final String email, final String code, final int mode, final RemoteCallBack callBack) {
        Logger.d(TAG, "-------------------------------由此开始登录操作-------------------------");
        if (TextUtils.isEmpty(phone) && TextUtils.isEmpty(email)) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_EMPTY_PHONE_NUMBER_OR_EMAIL, null);
            return;
        } else if (TextUtils.isEmpty(code)) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_EMPTY_CODE, null);
            return;
        }

        final PacketReceivedListener loginListener = new PacketReceivedListener() {
            @Override
            public void onReceivedPacket(BasePacket pkt) {
                if (pkt.getPacketType() != BasePacket.PacketType.LOGIN_REPLY) {
                    return;
                }
                RemoteConnectionManager.getInstance().removePacketListener(this);
                ReplyPacket replyPacket = new ReplyPacket(pkt);
                final int ret = replyPacket.getResult();
                String reason = replyPacket.getMessage();
                if (ret != 0) {
                    callbackOnError(callBack, ret, reason);
                    return;
                }
                JSONObject data = replyPacket.getData();
                GOIMContact lastLoginUser = GOIMContact.createLoginUser(data, phone, email);
                if (lastLoginUser.getUid() < 0) {
                    callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                    return;
                }
                Logger.d(TAG, "current user name:" + lastLoginUser.getNick() + "---------成功登录，开始初始化----------");

                final boolean updateFriend = SharedPreferencesUtils.getInstance(mContext).updateTime(KEY_UPDATE_FRIEND_TIME, data.optLong("friend", -1));
                final boolean updateGroup = SharedPreferencesUtils.getInstance(mContext).updateTime(KEY_UPDATE_GROUP_TIME, data.optLong("group", -1));
                SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_PHONE, phone);
                SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_EMAIL, email);
                SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_LOGIN_CODE, code);
                SharedPreferencesUtils.getInstance(mContext).setLastLoginUser(lastLoginUser);

                boolean dbExist = RemoteDBManager.initDB(mContext, lastLoginUser.getUid());
                UserFilePathManager.getInstance().initDirs(lastLoginUser.getUid(), mContext);

                // 登录成功，增加remote manager到remote connection的监听
                RemoteChatManager.getInstance().onLoggedIn();

                // 2018/4/26
                // 新策略按两种情况处理：
                // 1。登录成功后本地没有该用户的IM数据库，即新账号登录：先初始化Local，再请求groups from server，完成后进入app，同时请求offline messages
                // 2。登录成功后本地存在该用户的IM数据库，即老账号登录：初始化Local后直接进入app，然后请求offline message，
                // 根据offline message更新数据库数据，保证本地数据库数据不会错乱(无法处理从该设备退出后，在其他设备操作过后又回到该设备)，
                // 完成后再按需请求groups from server，同步服务器端数据库

                Logger.i(TAG, "before onLoggedIn()");
                // when called this method, local manager will load data to memory from database by binder.
                onLoggedIn();
                Logger.i(TAG, "after onLoggedIn()");
                final long lastMid = SharedPreferencesUtils.getInstance(mContext).getLong(KEY_LAST_MSG_ID, 0);
                if (!dbExist) {
                    Logger.d(TAG, "database not exist, request groups from server");
                    // now, local manager has initialized but there is no any data in memory
                    // after getGroupsFromServer's callback invoked, remote will call local's
                    // listeners to initialize the data in memory.
                    RemoteGroupManager.getInstance().getGroupsFromServer(new RemoteCallbackAdapter() {
                        @Override
                        public void onSuccess(List result) {
                            callbackOnSuccess(callBack, result);
                            RemoteChatManager.getInstance().offlineMsgs(lastMid, null);
                        }

                        @Override
                        public void onError(int errorCode, String reason) {
                            callbackOnError(callBack, errorCode, reason);
                        }

                    });
                } else {
                    Logger.d(TAG, "database already exists, enter application immediately");
                    callbackOnSuccess(callBack, null);
                    Logger.d(TAG, "database already exists, request offline messages from server");
                    // there is a problem: the message in server is updated in real time, that mean the
                    // group id of message is the updated, but the data in database is not before sync
                    // from server, so when client receive a message with updated group id, there may
                    // appear two conversation with different group id but of the same user. oh shit
                    // FIXME: 2018/5/25 just like description above
                    RemoteChatManager.getInstance().offlineMsgs(lastMid, new RemoteCallbackAdapter() {

                        @Override
                        public void onSuccess(List result) {
                            requestGroups();
                        }

                        @Override
                        public void onError(int errorCode, String reason) {
                            requestGroups();
                        }

                        private void requestGroups() {
                            if (updateFriend || updateGroup) {
                                RemoteGroupManager.getInstance().getGroupsFromServer(null);
                            }
                        }
                    });
                }

                RemoteContactManager.getInstance().clientService(null);
                Logger.d(TAG, "-------------------------------登录操作到此完成-------------------------");
            }
        };

        RemoteConnectionListener connectionListener = new RemoteConnectionListener.Stub() {
            @Override
            public void onConnected() {
                RemoteConnectionManager.getInstance().addPacketListener(loginListener);
                LoginPacket pkt = new LoginPacket(phone, email, code, RemoteConfig.COUNTRY, Build.MODEL,
                        BuildConfig.VERSION_CODE, 1, mode, DeviceUtil.getIMEI(mContext));
                RemoteConnectionManager.getInstance().writeAndFlushPacket(pkt, callBack, false);
                onConnectComplete();
            }

            @Override
            public void onDisconnected() {
                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNABLE_CONNECT_TO_SERVER, null);
                onConnectComplete();
            }

            @Override
            public void onError(String reason) {
                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNABLE_CONNECT_TO_SERVER, null);
                onConnectComplete();
            }

            private void onConnectComplete() {
                RemoteConnectionManager.getInstance().removeConnectionListener(this);
            }
        };

        RemoteConnectionManager.getInstance().addConnectionListener(connectionListener);
        RemoteConnectionManager.getInstance().disconnect();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connect = RemoteConnectionManager.getInstance().connect();
                if (!connect) {
                    callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNKNOWN_SERVER_HOST, null);
                }
            }
        }).start();
    }

    /** 获取当前登录的user */
    public GOIMContact getLoginUser() {
        return SharedPreferencesUtils.getInstance(mContext).getLastLoginUser();
    }

    /** 是否允许登录:phone或email不为空，并且code未过期 */
    public boolean isLoginPermit(Context context) {
        String phone = SharedPreferencesUtils.getInstance(context).getString(KEY_LAST_PHONE, "");
        String email = SharedPreferencesUtils.getInstance(context).getString(KEY_LAST_EMAIL, "");
        String code = SharedPreferencesUtils.getInstance(context).getString(KEY_LAST_LOGIN_CODE, "");
        long validTime = SharedPreferencesUtils.getInstance(context).getLong(KEY_LOGIN_CODE_VALID_TIME, 0);

        return (!TextUtils.isEmpty(phone) || !TextUtils.isEmpty(email))
                && (!TextUtils.isEmpty(code) && validTime > System.currentTimeMillis());
    }

    public void logout(final RemoteCallBack callBack) {
        RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
            @Override
            public void onReceivedPacket(BasePacket pkt) {
                if (pkt.getPacketType() == BasePacket.PacketType.LOGOUT_REPLY) {
                    RemoteConnectionManager.getInstance().removePacketListener(this);
                    // 移除remote connection的监听器
                    RemoteChatManager.getInstance().onLoggedOut(false);
                    callbackOnSuccess(callBack, null);
                    onLoggedOut();
                    Logger.d(TAG, "log out --- disconnect!!!!!!!!");
                    RemoteConnectionManager.getInstance().disconnect();
                }
            }
        });
        LogoutPacket pkt = new LogoutPacket();
        RemoteConnectionManager.getInstance().writeAndFlushPacket(pkt, callBack);
    }

    /** 添加登录状态监听，目前只有binder会调用 */
    public void addLogStatusListener(RemoteLoggedStatusListener l) {
        if (l == null) {
            return;
        }
        if (!mLoggedListeners.contains(l)) {
            mLoggedListeners.add(l);
        }
    }

    /** 移除Local的监听 */
    public void removeLogStatusListener() {
        mLoggedListeners.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void callbackOnSuccess(RemoteCallBack callBack, List result) {
        if (callBack != null) {
            try {
                callBack.onSuccess(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackOnError(RemoteCallBack callBack, int code, String reason) {
        if (callBack != null) {
            try {
                callBack.onError(code, reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void onLoggedIn() {
        for (RemoteLoggedStatusListener listener : mLoggedListeners) {
            try {
                listener.onLoggedIn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onLoggedOut() {
        Logger.d(TAG, "onLoggedOut: -- clear data -- local");
        for (RemoteLoggedStatusListener listener : mLoggedListeners) {
            try {
                listener.onLoggedOut();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
