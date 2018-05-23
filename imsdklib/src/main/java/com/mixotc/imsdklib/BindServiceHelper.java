package com.mixotc.imsdklib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.mixotc.imsdklib.service.RemoteService;
import com.mixotc.imsdklib.utils.Logger;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午1:34
 * Version  : v1.0.0
 * Describe : 绑定远程服务到主进程的辅助类
 */
public class BindServiceHelper {

    private static final String TAG = BindServiceHelper.class.getSimpleName();

    private Context mContext;
    private Class<? extends RemoteService> mServiceClass;
    private RemoteServiceBinder mServiceBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceBinder = RemoteServiceBinder.Stub.asInterface(service);
            Logger.d(TAG, "on service connect:" + (mServiceBinder == null));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    BindServiceHelper(Context context, Class<? extends RemoteService> service) {
        mContext = context;
        mServiceClass = service;
    }

    /**
     * 将远程服务Service绑定到主进程
     */
    public void bind() {
        if (mServiceBinder == null) {
            Intent intent = new Intent(mContext, mServiceClass);
            // onCreate onStartCommand
            mContext.startService(intent);

            Intent intent2 = new Intent(mContext, mServiceClass);
            // onCreate onBind
            mContext.bindService(intent2, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    /**
     * 将远程服务Service与主进程解除绑定
     */
    public void unbind() {
        if (mServiceBinder != null) {
            // TODO: 2018/5/23 移除所有listener ，通过Binder提供的借口方法
            mContext.unbindService(mServiceConnection);
            Intent intent = new Intent(mContext, mServiceClass);
            mContext.stopService(intent);
        }
    }

    public RemoteServiceBinder getBinder() {
        return mServiceBinder;
    }
}
