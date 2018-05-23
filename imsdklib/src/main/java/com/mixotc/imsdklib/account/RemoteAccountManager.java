package com.mixotc.imsdklib.account;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.mixotc.imsdklib.BuildConfig;
import com.mixotc.imsdklib.RemoteConfig;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.error.ErrorType;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteConnectionListener;
import com.mixotc.imsdklib.listener.RemoteLoggedStatusListener;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.LoginPacket;
import com.mixotc.imsdklib.packet.ReplyPacket;
import com.mixotc.imsdklib.packet.SendCodePacket;
import com.mixotc.imsdklib.utils.DeviceUtil;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.PathUtil;
import com.mixotc.imsdklib.utils.SharedPreferencesIds;
import com.mixotc.imsdklib.utils.SharedPreferencesUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_EMAIL;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_LOGIN_CODE;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_MSG_ID;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_PHONE;
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
                        SharedPreferencesUtils.getInstance(mContext).putLong(SharedPreferencesIds.KEY_LOGIN_CODE_VALID_TIME, validTime);
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
                Logger.d(TAG, "成功登录，开始初始化");

                long friendLastUpdate = data.optLong("friend", -1);
                long groupLastUpdate = data.optLong("group", -1);
                final boolean updateFriend = SharedPreferencesUtils.getInstance(mContext).updateTime(KEY_UPDATE_FRIEND_TIME, friendLastUpdate);
                final boolean updateGroup = SharedPreferencesUtils.getInstance(mContext).updateTime(KEY_UPDATE_GROUP_TIME, groupLastUpdate);
                SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_PHONE, phone);
                SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_EMAIL, email);
                SharedPreferencesUtils.getInstance(mContext).putString(KEY_LAST_LOGIN_CODE, code);
                SharedPreferencesUtils.getInstance(mContext).setLastLoginUser(lastLoginUser);

//                boolean dbExist = RemoteDBManager.initDB(mContext, lastLoginUser.getUid());
                boolean dbExist = false;
                PathUtil.getInstance().initDirs(RemoteAccountManager.getInstance().getLoginUser().getUid(), mContext);

//                RemoteChatManager.getInstance().onLoggedIn();

                // 更改记录：
                // 2018／3／22
                // 这两个值只能表明数据是否更新，无法判定被删除好友和群组人员数量的变化
                // 所以暂定每次登录都要重新请求数据
                // 2018/04/19
                // 服务器数据库表中的update_time的值为row数据最后更新的时间，前提是该row存在，若被删除则无法获取。
                // 被好友删除或者被群组踢出以及群组解散的情况，也就是contact／group减少的情况，对应的通知会当作离线消息存储，
                // 当再次登陆请求离线消息时，也可以及时处理，所以不用每次登录都重新请求group数据

                // 需要复现离线消息发生的过程，同步数据改变和解析消息的过程
                // 如果数据库存在，则先请求离线消息，后进行是否请求更新group的判断，若需要则请求。
                // 如果数据库不存在，则先直接请求group，之后再请求离线消息
                // 如果离线期间被别人删除好友，目前会发生删除动作前的离线消息服务器不推送

                // 2018/4/26
                // 新策略按两种情况处理：
                // 1。登录成功后本地没有该用户的IM数据库，即新账号登录：先初始化Local，再请求groups from server，完成后进入app，同时请求offline messages
                // 2。登录成功后本地存在该用户的IM数据库，即老账号登录：初始化Local后直接进入app，然后请求offline message，
                // 根据offline message更新数据库数据，保证本地数据库数据不会错乱(无法处理从该设备退出后，在其他设备操作过后又回到该设备)，
                // 完成后再按需请求groups from server，同步服务器端数据库
                Logger.d(TAG, "before onLoggedIn()");
                // 通知Local登录成功,Local自动初始化数据到内存
                onLoggedIn();
                Logger.d(TAG, "after onLoggedIn()");
                final long lastMid = SharedPreferencesUtils.getInstance(mContext).getLong(KEY_LAST_MSG_ID, -1);
//                final long lastMid = 0;
                if (!dbExist) {
                    Logger.d(TAG, "database not exist, request groups from server");
//                    try {
//                        // 此时Local是从一个空的数据库读取的数据，
//                        // groups from server完成后通知Local，Local会重新执行初始化从数据库读取数据
//                        RemoteGroupManager.getInstance().getGroupsFromServer(new RemoteCallBack.Stub() {
//                            @Override
//                            public void onSuccess(List result) {
//                                try {
//                                    callBack.onSuccess(result);
//                                    RemoteChatManager.getInstance().offlineMsgs(lastMid,null);
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onError(int errorCode, String reason) {
//                                try {
//                                    callBack.onError(errorCode, reason);
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onProgress(int progress, String message) {
//                            }
//                        });
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    Logger.d(TAG, "database already exist, enter application immediately");
                    try {
                        callBack.onSuccess(null);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Logger.d(TAG, "database already exist, request offline messages from server");
//                    RemoteChatManager.getInstance().offlineMsgs(lastMid, new RemoteCallBack.Stub() {
//
//                        @Override
//                        public void onSuccess(List result) {
//                            requestGroups();
//                        }
//
//                        @Override
//                        public void onError(int errorCode, String reason) {
//                            requestGroups();
//                        }
//
//                        @Override
//                        public void onProgress(int progress, String message) {
//                        }
//
//                        private void requestGroups() {
//                            // 按需更新groups contacts
//                            if (updateFriend || updateGroup) {
//                                try {
//                                    RemoteGroupManager.getInstance().getGroupsFromServer(null);
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });
                }

//                RemoteContactManager.getInstance().clientService(null);
                Logger.d(TAG, "-------------------------------登录操作到此完成-------------------------");
            }
        };

        Logger.d(TAG, "log in disconnect!!!!!!!!");
        RemoteConnectionManager.getInstance().disconnect();
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

    public GOIMContact getLoginUser() {
        return SharedPreferencesUtils.getInstance(mContext).getLastLoginUser();
    }

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
