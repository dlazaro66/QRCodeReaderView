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
 */
@file:Suppress("DEPRECATION")

package com.google.zxing.client.android.camera.open

import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import com.dlazaro66.qrcodereaderview.SimpleLog.i
import com.dlazaro66.qrcodereaderview.SimpleLog.w

/**
 * Abstraction over the [Camera] API that helps open them and return their metadata.
 */
object OpenCameraInterface {
    private val TAG = OpenCameraInterface::class.java.name
    /** For [.open], means no preference for which camera to open.  */
    const val NO_REQUESTED_CAMERA = -1

    /**
     * Opens the requested camera with [Camera.open], if one exists.
     *
     * @param cameraId camera ID of the camera to use. A negative value
     * or [.NO_REQUESTED_CAMERA] means "no preference", in which case a rear-facing
     * camera is returned if possible or else any camera
     * @return handle to [OpenCamera] that was opened
     */
    fun open(cameraId: Int): OpenCamera? {
        val numCameras = Camera.getNumberOfCameras()
        if (numCameras == 0) {
            w(TAG, "No cameras!")
            return null
        }
        val explicitRequest = cameraId >= 0
        var selectedCameraInfo: CameraInfo? = null
        var index: Int
        if (explicitRequest) {
            index = cameraId
            selectedCameraInfo = CameraInfo()
            Camera.getCameraInfo(index, selectedCameraInfo)
        } else {
            index = 0
            while (index < numCameras) {
                val cameraInfo = CameraInfo()
                Camera.getCameraInfo(index, cameraInfo)
                val reportedFacing = CameraFacing.values()[cameraInfo.facing]
                if (reportedFacing == CameraFacing.BACK) {
                    selectedCameraInfo = cameraInfo
                    break
                }
                index++
            }
        }
        val camera: Camera?
        if (index < numCameras) {
            i(TAG, "Opening camera #$index")
            camera = Camera.open(index)
        } else {
            if (explicitRequest) {
                w(TAG, "Requested camera does not exist: $cameraId")
                camera = null
            } else {
                i(TAG, "No camera facing " + CameraFacing.BACK + "; returning camera #0")
                camera = Camera.open(0)
                selectedCameraInfo = CameraInfo()
                Camera.getCameraInfo(0, selectedCameraInfo)
            }
        }
        return if (camera == null) {
            null
        } else OpenCamera(index,
                camera,
                CameraFacing.values()[selectedCameraInfo!!.facing],
                selectedCameraInfo.orientation)
    }
}