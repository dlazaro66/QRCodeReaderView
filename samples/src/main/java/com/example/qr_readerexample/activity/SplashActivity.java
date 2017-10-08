package com.example.qr_readerexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    public static long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startTime=System.currentTimeMillis();   //获取开始时间

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
