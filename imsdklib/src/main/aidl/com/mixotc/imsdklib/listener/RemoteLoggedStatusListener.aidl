// GOIMRemotePacketListener.aidl
package com.mixotc.imsdklib.listener;

// Declare any non-default types here with import statements

interface RemoteLoggedStatusListener {
    void onLoggedIn();
    void onLoggedOut();
}