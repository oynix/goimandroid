// IGOIMConnectionListener.aidl
package com.mixotc.imsdklib.listener;

// Declare any non-default types here with import statements

interface RemoteCallBack {
    void onSuccess(in List result);
    void onError(int errorCode, String reason);
    void onProgress(int progress, String message);
}
