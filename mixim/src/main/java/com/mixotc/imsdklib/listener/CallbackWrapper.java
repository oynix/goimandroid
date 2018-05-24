package com.mixotc.imsdklib.listener;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午6:23
 * Version  : v1.0.0
 * Describe : RemoteCallBack的包装类，将所有的回调切换到主线程执行，同时将onProgress变为可选的实现方法。
 */
public abstract class CallbackWrapper extends RemoteCallBack.Stub {
    
    private Handler mHandler = new Handler(Looper.getMainLooper());
    
    @Override
    public void onSuccess(final List result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onMainThreadSuccess(result);
            }
        });
    }

    @Override
    public void onError(final int errorCode, final String reason) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onMainThreadError(errorCode, reason);
            }
        });
    }

    @Override
    public void onProgress(final int progress, final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onMainThreadProgress(progress, message);
            }
        });
    }
    
    public abstract void onMainThreadSuccess(List result);
    public abstract void onMainThreadError(int errorCode, String reason);

    public void onMainThreadProgress(int progress, String message) {

    }
}
