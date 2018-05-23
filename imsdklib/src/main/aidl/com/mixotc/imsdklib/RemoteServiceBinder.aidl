// RemoteServiceBinder.aidl
package com.mixotc.imsdklib;

// Declare any non-default types here with import statements
import com.mixotc.imsdklib.listener.RemoteCallBack;

interface RemoteServiceBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void sendCode(String phone, String email, RemoteCallBack callback);
    void login(String phone, String email, String code, RemoteCallBack callback);
}
