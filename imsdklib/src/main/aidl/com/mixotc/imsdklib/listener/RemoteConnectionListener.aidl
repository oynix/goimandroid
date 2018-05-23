// IGOIMConnectionListener.aidl
package com.mixotc.imsdklib.listener;

// Declare any non-default types here with import statements

interface RemoteConnectionListener {
    void onConnected();
    void onDisconnected();
    void onError(String reason);
}
