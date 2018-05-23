package com.mixotc.goimandroid;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.utils.Logger;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onButtonClick(View v) {
        String email = "xiaoyu@mixotc.com";
        AdminManager.getInstance().sendLoginCode("", email, new RemoteCallBack.Stub() {
            @Override
            public void onSuccess(List result) throws RemoteException {
                Logger.d(TAG, "发送验证码成功");
            }

            @Override
            public void onError(int errorCode, String reason) throws RemoteException {
                Logger.d(TAG, "发送验证码失败" + errorCode);
            }

            @Override
            public void onProgress(int progress, String message) throws RemoteException {

            }
        });
    }
}
