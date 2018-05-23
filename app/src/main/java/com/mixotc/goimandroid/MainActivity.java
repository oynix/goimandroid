package com.mixotc.goimandroid;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.utils.Logger;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String mEmail = "xiaoyu@mixotc.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void requestCode(View v) {
        AdminManager.getInstance().sendLoginCode("", mEmail, new RemoteCallBack.Stub() {
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

    public void login(View v) {
        EditText ed = (EditText) findViewById(R.id.et_login_code);
        Editable code = ed.getText();
        AdminManager.getInstance().login("", mEmail, code.toString(), new RemoteCallBack.Stub() {
            @Override
            public void onSuccess(List result) throws RemoteException {
                Logger.d(TAG, "login成功");
            }

            @Override
            public void onError(int errorCode, String reason) throws RemoteException {
                Logger.d(TAG, "login 失败" + errorCode);
            }

            @Override
            public void onProgress(int progress, String message) throws RemoteException {

            }
        });
    }
}
