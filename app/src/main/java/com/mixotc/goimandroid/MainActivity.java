package com.mixotc.goimandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mixotc.imsdklib.connection.RemoteConnectionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RemoteConnectionManager.getInstance().init(this);
        RemoteConnectionManager.getInstance().connect();
    }
}
