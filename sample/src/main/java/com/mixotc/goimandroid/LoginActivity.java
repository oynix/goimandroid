package com.mixotc.goimandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.listener.CallbackWrapper;
import com.mixotc.imsdklib.utils.Logger;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etAccount = findViewById(R.id.et_account);
        final EditText etLoginCode = findViewById(R.id.et_login_code);
        Button btnSend = findViewById(R.id.btn_send);
        Button btnLogin = findViewById(R.id.btn_login);
        
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginCheck()) {
                    loginComp();
                    finish();
                }
                // use email to login
                String account = etAccount.getText().toString();
                AdminManager.getInstance().sendLoginCode("", account, new CallbackWrapper() {

                    @Override
                    public void onMainThreadSuccess(List result) {
                        Logger.d(TAG, "send code successfully!!!!" + Thread.currentThread());

                    }

                    @Override
                    public void onMainThreadError(int errorCode, String reason) {
                        Logger.d(TAG, "send code failed!!!!" + errorCode);

                    }
                });
            }
        });
        
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginCheck()) {
                    loginComp();
                    finish();
                }
                String code = etLoginCode.getText().toString();
                String account = etAccount.getText().toString();
                if (TextUtils.isEmpty(code) || TextUtils.isEmpty(account)) {
                    Logger.d(TAG, "输入有误，请检查!!!");
                    return;
                }
                AdminManager.getInstance().login("", account, code, new CallbackWrapper() {

                    @Override
                    public void onMainThreadSuccess(List result) {
                        Logger.d(TAG, "======login成功" + Thread.currentThread());
                        loginComp();

                    }

                    @Override
                    public void onMainThreadError(int errorCode, String reason) {
                        Logger.d(TAG, "======login失败" + errorCode);

                    }
                });
            }
        });
    }

    private boolean loginCheck() {
        return AdminManager.getInstance().isLogin();
    }

    private void loginComp() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

}
