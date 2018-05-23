package com.mixotc.imsdklib.account;

import android.content.Context;
import android.os.RemoteException;

import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.error.ErrorType;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteConnectionListener;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.ReplyPacket;
import com.mixotc.imsdklib.packet.SendCodePacket;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.SharedPreferencesIds;
import com.mixotc.imsdklib.utils.SharedPreferencesUtils;

import org.json.JSONObject;

import java.util.List;

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

    private RemoteAccountManager() {
    }

    public static RemoteAccountManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Context mContext;

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
                        SharedPreferencesUtils.getInstance(mContext).putLong(SharedPreferencesIds.LOGIN_CODE_VALID_TIME, validTime);
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
                Logger.d(TAG, "on connected");
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
}
