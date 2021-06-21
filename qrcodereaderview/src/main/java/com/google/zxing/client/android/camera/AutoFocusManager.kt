/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -- Class modifications
 *
 * Copyright 2016 David Lázaro Esparcia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.client.android.camera

import android.annotation.SuppressLint
import android.hardware.Camera
import android.hardware.Camera.AutoFocusCallback
import android.os.AsyncTask
import com.dlazaro66.qrcodereaderview.SimpleLog.i
import com.dlazaro66.qrcodereaderview.SimpleLog.w
import java.util.*
import java.util.concurrent.RejectedExecutionException

@Suppress("DEPRECATION")
internal class AutoFocusManager(private val camera: Camera?) : AutoFocusCallback {
    private var autofocusIntervalMs = DEFAULT_AUTO_FOCUS_INTERVAL_MS

    companion object {
        private val TAG = AutoFocusManager::class.java.simpleName
        const val DEFAULT_AUTO_FOCUS_INTERVAL_MS = 5000L
        private var FOCUS_MODES_CALLING_AF: MutableCollection<String>? = null

        init {
            FOCUS_MODES_CALLING_AF = ArrayList(2)
            FOCUS_MODES_CALLING_AF?.add(Camera.Parameters.FOCUS_MODE_AUTO)
            FOCUS_MODES_CALLING_AF?.add(Camera.Parameters.FOCUS_MODE_MACRO)
        }
    }

    private var stopped = false
    private var focusing = false
    private val useAutoFocus: Boolean
    private var outstandingTask: AsyncTask<*, *, *>? = null
    @Synchronized
    override fun onAutoFocus(success: Boolean, theCamera: Camera) {
        focusing = false
        autoFocusAgainLater()
    }

    fun setAutofocusInterval(autofocusIntervalMs: Long) {
        require(autofocusIntervalMs > 0) { "AutoFocusInterval must be greater than 0." }
        this.autofocusIntervalMs = autofocusIntervalMs
    }

    @SuppressLint("NewApi")
    @Synchronized
    private fun autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            val newTask = AutoFocusTask()
            try {
                newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                outstandingTask = newTask
            } catch (ree: RejectedExecutionException) {
                w(TAG, "Could not request auto focus", ree)
            }
        }
    }

    @Synchronized
    fun start() {
        if (useAutoFocus) {
            outstandingTask = null
            if (!stopped && !focusing) {
                try {
                    camera?.autoFocus(this)
                    focusing = true
                } catch (re: RuntimeException) { // Have heard RuntimeException reported in Android 4.0.x+; continue?
                    w(TAG, "Unexpected exception while focusing", re)
                    // Try again later to keep cycle going
                    autoFocusAgainLater()
                }
            }
        }
    }

    @Synchronized
    private fun cancelOutstandingTask() {
        outstandingTask?.let {
            if (it.status != AsyncTask.Status.FINISHED) {
                it.cancel(true)
            }
            outstandingTask = null
        }
    }

    @Synchronized
    fun stop() {
        stopped = true
        if (useAutoFocus) {
            cancelOutstandingTask()
            // Doesn't hurt to call this even if not focusing
            try {
                camera!!.cancelAutoFocus()
            } catch (re: RuntimeException) { // Have heard RuntimeException reported in Android 4.0.x+; continue?
                w(TAG, "Unexpected exception while cancelling focusing", re)
            }
        }
    }

    private inner class AutoFocusTask : AsyncTask<Any?, Any?, Any?>() {
        override fun doInBackground(vararg p0: Any?): Any? {
            try {
                Thread.sleep(autofocusIntervalMs)
            } catch (e: InterruptedException) { // continue
            }
            start()
            return null
        }
    }

    init {
        val currentFocusMode = camera?.parameters?.focusMode
        useAutoFocus = FOCUS_MODES_CALLING_AF?.contains(currentFocusMode)?: false
        i(TAG, "Current focus mode '"
                + currentFocusMode
                + "'; use auto focus? "
                + useAutoFocus)
        start()
    }
}