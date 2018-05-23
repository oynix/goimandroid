package com.mixotc.imsdklib.service;

import android.os.RemoteException;

import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.account.RemoteAccountManager;
import com.mixotc.imsdklib.listener.RemoteCallBack;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午5:32
 * Version  : v1.0.0
 * Describe : 这个类就像是一个中间人，一个中介，接收主进程的调用后，直接转发至服务进程的方法实现。
 */
public class RemoteBinder extends RemoteServiceBinder.Stub {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public void sendCode(String phone, String email, RemoteCallBack callBack) throws RemoteException {
        RemoteAccountManager.getInstance().sendLoginCode(phone, email, callBack);
    }
}
