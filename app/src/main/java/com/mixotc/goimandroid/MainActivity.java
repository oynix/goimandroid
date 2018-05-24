package com.mixotc.goimandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mixotc.goimandroid.contact.ContactActivity;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午2:05
 * Version  : v1.0.0
 * Describe :
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View btnContact = findViewById(R.id.iv_contact);
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
            }
        });
    }
}
