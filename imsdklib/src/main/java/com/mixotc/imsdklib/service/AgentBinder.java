package com.mixotc.imsdklib.service;

import android.os.RemoteException;

import com.mixotc.imsdklib.RemoteServiceBinder;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午5:32
 * Version  : v1.0.0
 * Describe :
 */
public class AgentBinder extends RemoteServiceBinder.Stub {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public void sendCode(String phone, String email) throws RemoteException {

    }
}
