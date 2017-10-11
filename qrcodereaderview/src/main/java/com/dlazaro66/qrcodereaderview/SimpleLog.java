package com.dlazaro66.qrcodereaderview;

import android.util.Log;

/**
 * Created on 11.10.2017.
 */

public class SimpleLog {

    private boolean loggingEnabled = false;

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public void d(String tag, String text) {
        if (loggingEnabled) {
            Log.d(tag, text);
        }
    }

    public void w(String tag, String text) {
        if (loggingEnabled) {
            Log.w(tag, text);
        }
    }

    public void w(String tag, String text, Throwable e) {
        if (loggingEnabled) {
            Log.w(tag, text, e);
        }
    }

    public void e(String tag, String text) {
        if (loggingEnabled) {
            Log.e(tag, text);
        }
    }

    public void d(String tag, String text, Throwable e) {
        if (loggingEnabled) {
            Log.d(tag, text, e);
        }
    }

    public void i(String tag, String text) {
        if (loggingEnabled) {
            Log.i(tag, text);
        }
    }
}
