/*
 * Copyright (C) 2010 ZXing authors
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
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import com.dlazaro66.qrcodereaderview.SimpleLog.i
import com.dlazaro66.qrcodereaderview.SimpleLog.w
import com.google.zxing.client.android.camera.open.CameraFacing
import com.google.zxing.client.android.camera.open.OpenCamera
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
@Suppress("DEPRECATION")
internal class CameraConfigurationManager(private val context: Context) {
    var screenResolution: Point? = null
        private set
    var cameraResolution: Point? = null
        private set
    private var bestPreviewSize: Point? = null
    private var previewSizeOnScreen: Point? = null
    private var cwRotationFromDisplayToCamera = 0
    private var cwNeededRotation = 0
    fun initFromCameraParameters(camera: OpenCamera?, width: Int, height: Int) {
        val parameters = camera?.camera?.parameters
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val displayRotation = display.rotation
        val cwRotationFromNaturalToDisplay: Int
        cwRotationFromNaturalToDisplay = when (displayRotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else ->  // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    (360 + displayRotation) % 360
                } else {
                    throw IllegalArgumentException("Bad rotation: $displayRotation")
                }
        }
        i(TAG, "Display at: $cwRotationFromNaturalToDisplay")
        var cwRotationFromNaturalToCamera = camera?.orientation ?: 0
        i(TAG, "Camera at: $cwRotationFromNaturalToCamera")
        // Still not 100% sure about this. But acts like we need to flip this:
        if (camera?.facing == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360
            i(TAG, "Front camera overriden to: $cwRotationFromNaturalToCamera")
        }
        cwRotationFromDisplayToCamera = (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360
        i(TAG, "Final display orientation: $cwRotationFromDisplayToCamera")
        cwNeededRotation = if (camera?.facing == CameraFacing.FRONT) {
            i(TAG, "Compensating rotation for front camera")
            (360 - cwRotationFromDisplayToCamera) % 360
        } else {
            cwRotationFromDisplayToCamera
        }
        i(TAG, "Clockwise rotation from display to camera: $cwNeededRotation")
        screenResolution = Point(width, height)
        i(TAG, "Screen resolution in current orientation: " + screenResolution)
        cameraResolution = findBestPreviewSizeValue(parameters, screenResolution!!)
        i(TAG, "Camera resolution: $cameraResolution")
        bestPreviewSize = findBestPreviewSizeValue(parameters, screenResolution!!)
        i(TAG, "Best available preview size: $bestPreviewSize")
        val isScreenPortrait = screenResolution!!.x < screenResolution!!.y
        val isPreviewSizePortrait = bestPreviewSize!!.x < bestPreviewSize!!.y
        previewSizeOnScreen = if (isScreenPortrait == isPreviewSizePortrait) {
            bestPreviewSize
        } else {
            Point(bestPreviewSize!!.y, bestPreviewSize!!.x)
        }
        i(TAG, "Preview size on screen: $previewSizeOnScreen")
    }

    fun setDesiredCameraParameters(camera: OpenCamera?, safeMode: Boolean) {
        val theCamera = camera!!.camera
        val parameters = theCamera.parameters
        if (parameters == null) {
            w(TAG,
                    "Device error: no camera parameters are available. Proceeding without configuration.")
            return
        }
        i(TAG, "Initial camera parameters: " + parameters.flatten())
        if (safeMode) {
            w(TAG, "In camera config safe mode -- most settings will not be honored")
        }
        // Maybe selected auto-focus but not available, so fall through here:
        var focusMode: String? = null
        if (!safeMode) {
            val supportedFocusModes = parameters.supportedFocusModes
            focusMode = findSettableValue("focus mode",
                    supportedFocusModes,
                    Camera.Parameters.FOCUS_MODE_AUTO)
        }
        if (focusMode != null) {
            parameters.focusMode = focusMode
        }
        parameters.setPreviewSize(bestPreviewSize?.x ?: 0, bestPreviewSize?.y ?: 0)
        theCamera.parameters = parameters
        theCamera.setDisplayOrientation(cwRotationFromDisplayToCamera)
        val afterParameters = theCamera.parameters
        val afterSize = afterParameters.previewSize
        if (afterSize != null && (bestPreviewSize?.x != afterSize.width
                        || bestPreviewSize!!.y != afterSize.height)) {
            w(TAG,
                    "Camera said it supported preview size "
                            + bestPreviewSize!!.x
                            + 'x'
                            + bestPreviewSize!!.y
                            + ", but after setting it, preview size is "
                            + afterSize.width
                            + 'x'
                            + afterSize.height)
            bestPreviewSize?.x = afterSize.width
            bestPreviewSize?.y = afterSize.height
        }
    }

    // All references to Torch are removed from here, methods, variables...
    fun findBestPreviewSizeValue(parameters: Camera.Parameters?, screenResolution: Point): Point {
        val rawSupportedSizes = parameters?.supportedPreviewSizes
        if (rawSupportedSizes == null) {
            w(TAG, "Device returned no supported preview sizes; using default")
            val defaultSize = parameters?.previewSize
            return Point(defaultSize?.width ?: 0, defaultSize?.height ?: 0)
        }
        // Sort by size, descending
        val supportedPreviewSizes: List<Camera.Size> = ArrayList(rawSupportedSizes)
        Collections.sort(supportedPreviewSizes, Comparator { a, b ->
            val aPixels = a.height * a.width
            val bPixels = b.height * b.width
            if (bPixels < aPixels) {
                return@Comparator -1
            }
            if (bPixels > aPixels) {
                1
            } else 0
        })
        if (Log.isLoggable(TAG, Log.INFO)) {
            val previewSizesString = StringBuilder()
            for (supportedPreviewSize in supportedPreviewSizes) {
                previewSizesString.append(supportedPreviewSize.width)
                        .append('x')
                        .append(supportedPreviewSize.height)
                        .append(' ')
            }
            i(TAG, "Supported preview sizes: $previewSizesString")
        }
        var bestSize: Point? = null
        val screenAspectRatio = screenResolution.x.toFloat() / screenResolution.y.toFloat()
        var diff = Float.POSITIVE_INFINITY
        for (supportedPreviewSize in supportedPreviewSizes) {
            val realWidth = supportedPreviewSize.width
            val realHeight = supportedPreviewSize.height
            val pixels = realWidth * realHeight
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue
            }
            // This code is modified since We're using portrait mode
            val isCandidateLandscape = realWidth > realHeight
            val maybeFlippedWidth = if (isCandidateLandscape) realHeight else realWidth
            val maybeFlippedHeight = if (isCandidateLandscape) realWidth else realHeight
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                val exactPoint = Point(realWidth, realHeight)
                i(TAG, "Found preview size exactly matching screen size: $exactPoint")
                return exactPoint
            }
            val aspectRatio = maybeFlippedWidth.toFloat() / maybeFlippedHeight.toFloat()
            val newDiff = abs(aspectRatio - screenAspectRatio)
            if (newDiff < diff) {
                bestSize = Point(realWidth, realHeight)
                diff = newDiff
            }
        }
        if (bestSize == null) {
            val defaultSize = parameters.previewSize
            bestSize = Point(defaultSize.width, defaultSize.height)
            i(TAG, "No suitable preview sizes, using default: $bestSize")
        }
        i(TAG, "Found best approximate preview size: $bestSize")
        return bestSize
    }

    fun getTorchState(camera: Camera?): Boolean {
        camera?.let {
            val parameters = it.parameters
            if (parameters != null) {
                val flashMode = it.parameters.flashMode
                return flashMode != null && (Camera.Parameters.FLASH_MODE_ON == flashMode || Camera.Parameters.FLASH_MODE_TORCH == flashMode)
            }
        }

        return false
    }

    fun setTorchEnabled(camera: Camera?, enabled: Boolean) {
        val parameters = camera?.parameters
        setTorchEnabled(parameters, enabled, false)
        camera?.parameters = parameters
    }

    fun setTorchEnabled(parameters: Camera.Parameters?, enabled: Boolean, safeMode: Boolean) {
        parameters?.let {
            setTorchEnabled(it, enabled)
            if (!safeMode) {
                setBestExposure(it, enabled)
            }
        }
    }

    companion object {
        private const val TAG = "CameraConfiguration"
        // This is bigger than the size of a small screen, which is still supported. The routine
// below will still select the default (presumably 320x240) size for these. This prevents
// accidental selection of very low resolution on some devices.
        private const val MIN_PREVIEW_PIXELS = 470 * 320 // normal screen
        private const val MAX_PREVIEW_PIXELS = 1280 * 720
        private const val MAX_EXPOSURE_COMPENSATION = 1.5f
        private const val MIN_EXPOSURE_COMPENSATION = 0.0f
        private fun findSettableValue(name: String,
                                      supportedValues: Collection<String>?,
                                      vararg desiredValues: String): String? {
            i(TAG, "Requesting " + name + " value from among: " + Arrays.toString(desiredValues))
            i(TAG, "Supported $name values: $supportedValues")
            if (supportedValues != null) {
                for (desiredValue in desiredValues) {
                    if (supportedValues.contains(desiredValue)) {
                        i(TAG, "Can set $name to: $desiredValue")
                        return desiredValue
                    }
                }
            }
            i(TAG, "No supported values match")
            return null
        }

        fun setTorchEnabled(parameters: Camera.Parameters,
                            enabled: Boolean) {
            val supportedFlashModes = parameters.supportedFlashModes
            val flashMode: String?
            flashMode = if (enabled) {
                findSettableValue("flash mode",
                        supportedFlashModes,
                        Camera.Parameters.FLASH_MODE_TORCH,
                        Camera.Parameters.FLASH_MODE_ON)
            } else {
                findSettableValue("flash mode",
                        supportedFlashModes,
                        Camera.Parameters.FLASH_MODE_OFF)
            }
            if (flashMode != null) {
                if (flashMode == parameters.flashMode) {
                    i(TAG, "Flash mode already set to $flashMode")
                } else {
                    i(TAG, "Setting flash mode to $flashMode")
                    parameters.flashMode = flashMode
                }
            }
        }

        fun setBestExposure(parameters: Camera.Parameters,
                            lightOn: Boolean) {
            val minExposure = parameters.minExposureCompensation
            val maxExposure = parameters.maxExposureCompensation
            val step = parameters.exposureCompensationStep
            if ((minExposure != 0 || maxExposure != 0) && step > 0.0f) { // Set low when light is on
                val targetCompensation = if (lightOn) MIN_EXPOSURE_COMPENSATION else MAX_EXPOSURE_COMPENSATION
                var compensationSteps = (targetCompensation / step).roundToInt()
                val actualCompensation = step * compensationSteps
                // Clamp value:
                compensationSteps = compensationSteps.coerceAtMost(maxExposure).coerceAtLeast(minExposure)
                if (parameters.exposureCompensation == compensationSteps) {
                    i(TAG, "Exposure compensation already set to " + compensationSteps + " / "
                            + actualCompensation)
                } else {
                    i(TAG,
                            "Setting exposure compensation to $compensationSteps / $actualCompensation")
                    parameters.exposureCompensation = compensationSteps
                }
            } else {
                i(TAG, "Camera does not support exposure compensation")
            }
        }
    }

}