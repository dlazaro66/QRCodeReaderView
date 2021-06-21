/*
 * Copyright (C) 2008 ZXing authors
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

import android.content.Context
import android.graphics.Point
import android.hardware.Camera.PreviewCallback
import android.view.SurfaceHolder
import com.dlazaro66.qrcodereaderview.SimpleLog.i
import com.dlazaro66.qrcodereaderview.SimpleLog.w
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.client.android.camera.open.OpenCamera
import com.google.zxing.client.android.camera.open.OpenCameraInterface
import java.io.IOException

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
@Suppress("DEPRECATION")
class CameraManager(context: Context) {
    private val configManager: CameraConfigurationManager = CameraConfigurationManager(context)
    private var openCamera: OpenCamera? = null
    private var autoFocusManager: AutoFocusManager? = null
    private var initialized = false
    private var previewing = false
    private var previewCallback: PreviewCallback? = null
    private var displayOrientation = 0
    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     */
    // PreviewCallback references are also removed from original ZXING authors work,
// since we're using our own interface.
// FramingRects references are also removed from original ZXING authors work,
// since We're using all view size while detecting QR-Codes.
    @set:Synchronized
    var previewCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA
    private var autofocusIntervalInMs: Long = AutoFocusManager.Companion.DEFAULT_AUTO_FOCUS_INTERVAL_MS
    fun setPreviewCallback(previewCallback: PreviewCallback?) {
        this.previewCallback = previewCallback
        if (isOpen) {
            openCamera?.camera?.setPreviewCallback(previewCallback)
        }
    }

    fun setDisplayOrientation(degrees: Int) {
        displayOrientation = degrees
        if (isOpen) {
            openCamera?.camera?.setDisplayOrientation(degrees)
        }
    }

    fun setAutofocusInterval(autofocusIntervalInMs: Long) {
        this.autofocusIntervalInMs = autofocusIntervalInMs
        autoFocusManager?.setAutofocusInterval(autofocusIntervalInMs)
    }

    fun forceAutoFocus() {
        autoFocusManager?.start()
    }

    val previewSize: Point?
        get() = configManager.cameraResolution

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @param height @throws IOException Indicates the camera driver failed to open.
     */
    @Synchronized
    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder?, width: Int, height: Int) {
        var theCamera = openCamera
        if (!isOpen) {
            theCamera = OpenCameraInterface.open(previewCameraId)
            if (theCamera?.camera == null) {
                throw IOException("Camera.open() failed to return object from driver")
            }
            openCamera = theCamera
        }
        theCamera?.camera?.setPreviewDisplay(holder)
        theCamera?.camera?.setPreviewCallback(previewCallback)
        theCamera?.camera?.setDisplayOrientation(displayOrientation)
        if (!initialized) {
            initialized = true
            configManager.initFromCameraParameters(theCamera, width, height)
        }
        val cameraObject = theCamera?.camera
        var parameters = cameraObject?.parameters
        val parametersFlattened = parameters?.flatten() // Save these, temporarily
        try {
            configManager.setDesiredCameraParameters(theCamera, false)
        } catch (re: RuntimeException) { // Driver failed
            w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters")
            i(TAG, "Resetting to saved camera params: $parametersFlattened")
            // Reset:
            if (parametersFlattened != null) {
                parameters = cameraObject?.parameters
                parameters?.unflatten(parametersFlattened)
                try {
                    cameraObject?.parameters = parameters
                    configManager.setDesiredCameraParameters(theCamera, true)
                } catch (re2: RuntimeException) { // Well, darn. Give up
                    w(TAG, "Camera rejected even safe-mode parameters! No configuration")
                }
            }
        }
        cameraObject?.setPreviewDisplay(holder)
    }

    /**
     * @param enabled if `true`, light should be turned on if currently off. And vice versa.
     */
    @Synchronized
    fun setTorchEnabled(enabled: Boolean) {
        val theCamera = openCamera
        if (theCamera != null && enabled != configManager.getTorchState(theCamera.camera)) {
            val wasAutoFocusManager = autoFocusManager != null
            if (wasAutoFocusManager) {
                autoFocusManager?.stop()
                autoFocusManager = null
            }
            configManager.setTorchEnabled(theCamera.camera, enabled)
            if (wasAutoFocusManager) {
                autoFocusManager = AutoFocusManager(theCamera.camera)
                autoFocusManager?.start()
            }
        }
    }

    @get:Synchronized
    val isOpen: Boolean
        get() = openCamera != null && openCamera?.camera != null

    /**
     * Closes the camera driver if still in use.
     */
    @Synchronized
    fun closeDriver() {
        if (isOpen) {
            openCamera?.camera?.release()
            openCamera = null
            // Make sure to clear these each time we close the camera, so that any scanning rect
// requested by intent is forgotten.
// framingRect = null;
// framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    @Synchronized
    fun startPreview() {
        val theCamera = openCamera
        if (theCamera != null && !previewing) {
            theCamera.camera.setPreviewCallback(previewCallback)
            theCamera.camera.startPreview()
            previewing = true
            autoFocusManager = AutoFocusManager(theCamera.camera)
            autoFocusManager?.setAutofocusInterval(autofocusIntervalInMs)
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    @Synchronized
    fun stopPreview() {
        autoFocusManager?.stop()
        autoFocusManager = null

        if (openCamera != null && previewing) {
            openCamera?.camera?.stopPreview()
            previewing = false
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    fun buildLuminanceSource(data: ByteArray?, width: Int, height: Int): PlanarYUVLuminanceSource {
        return PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
    }

    companion object {
        private val TAG = CameraManager::class.java.simpleName
    }

}