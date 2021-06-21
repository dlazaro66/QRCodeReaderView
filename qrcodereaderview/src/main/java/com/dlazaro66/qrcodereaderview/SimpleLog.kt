package com.dlazaro66.qrcodereaderview

import android.util.Log

object SimpleLog {
    private var loggingEnabled = false
    fun setLoggingEnabled(enabled: Boolean) {
        loggingEnabled = enabled
    }

    fun d(tag: String?, text: String?) {
        if (loggingEnabled) {
            Log.d(tag, text ?: "")
        }
    }

    fun w(tag: String?, text: String?) {
        if (loggingEnabled) {
            Log.w(tag, text ?: "")
        }
    }

    @JvmStatic
    fun w(tag: String?, text: String?, e: Throwable?) {
        if (loggingEnabled) {
            Log.w(tag, text, e)
        }
    }

    fun e(tag: String?, text: String?) {
        if (loggingEnabled) {
            Log.e(tag, text ?: "")
        }
    }

    fun d(tag: String?, text: String?, e: Throwable?) {
        if (loggingEnabled) {
            Log.d(tag, text, e)
        }
    }

    @JvmStatic
    fun i(tag: String?, text: String?) {
        if (loggingEnabled) {
            Log.i(tag, text ?: "")
        }
    }
}