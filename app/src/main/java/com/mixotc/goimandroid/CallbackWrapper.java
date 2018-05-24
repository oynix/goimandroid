package com.mixotc.goimandroid;

import com.mixotc.imsdklib.listener.RemoteCallBack;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午5:31
 * Version  : v1.0.0
 * Describe :
 */
public class CallbackWrapper extends RemoteCallBack.Stub {
    @Override
    public void onSuccess(List result) {
        
    }

    @Override
    public void onError(int errorCode, String reason) {

    }

    @Override
    public void onProgress(int progress, String message) {

    }
}
