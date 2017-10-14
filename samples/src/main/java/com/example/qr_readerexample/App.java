package com.example.qr_readerexample;

import android.app.Application;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

/**
 * 文件描述：
 * Created by Administrator on 2017/10/3.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Don't do this! This is just so cold launches take some time
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(3));
    }
}