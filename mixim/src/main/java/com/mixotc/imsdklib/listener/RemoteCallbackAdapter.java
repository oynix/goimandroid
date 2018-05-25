package com.mixotc.imsdklib.listener;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/25 下午2:51
 * Version  : v1.0.0
 * Describe : 将必须实现的三个接口方法变成了可选继承的方法。
 */
public class RemoteCallbackAdapter extends RemoteCallBack.Stub {
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
