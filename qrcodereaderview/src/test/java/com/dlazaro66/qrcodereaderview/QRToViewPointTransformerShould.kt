package com.dlazaro66.qrcodereaderview

import android.graphics.Point
import com.google.zxing.ResultPoint
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QRToViewPointTransformerShould {
    private var qrToViewPointTransformer: QRToViewPointTransformer? = null
    @Before
    @Throws(Exception::class)
    fun setUp() {
        qrToViewPointTransformer = QRToViewPointTransformer()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
    }

    @Test
    @Throws(Exception::class)
    fun transformPortraitNotMirrorQRPointToViewPoint() {
        val qrPoint = ResultPoint(100f, 50f)
        val isMirrorPreview = false
        val orientation = Orientation.PORTRAIT
        val viewSize = Point(100, 200)
        val cameraPreviewSize = Point(200, 100)
        val result = qrToViewPointTransformer!!.transform(qrPoint, isMirrorPreview, orientation,
                viewSize, cameraPreviewSize)!!
        Assert.assertEquals(result.x, 50f, 0.0f)
        Assert.assertEquals(result.y, 100f, 0.0f)
    }

    @Test
    @Throws(Exception::class)
    fun transformPortraitMirrorQRPointToViewPoint() {
        val qrPoint = ResultPoint(0f, 0f)
        val isMirrorPreview = true
        val orientation = Orientation.PORTRAIT
        val viewSize = Point(100, 200)
        val cameraPreviewSize = Point(200, 100)
        val result = qrToViewPointTransformer!!.transform(qrPoint, isMirrorPreview, orientation,
                viewSize, cameraPreviewSize)!!
        Assert.assertEquals(result.x, 100f, 0.0f)
        Assert.assertEquals(result.y, 200f, 0.0f)
    }

    @Test
    @Throws(Exception::class)
    fun transformLandscapeNotMirrorQRPointToViewPoint() {
        val qrPoint = ResultPoint(100f, 50f)
        val isMirrorPreview = false
        val orientation = Orientation.LANDSCAPE
        val viewSize = Point(200, 100)
        val cameraPreviewSize = Point(200, 100)
        val result = qrToViewPointTransformer!!.transform(qrPoint, isMirrorPreview, orientation,
                viewSize, cameraPreviewSize)!!
        Assert.assertEquals(result.x, 100f, 0.0f)
        Assert.assertEquals(result.y, 50f, 0.0f)
    }

    @Test
    @Throws(Exception::class)
    fun transformLandscapeMirrorQRPointToViewPoint() {
        val qrPoint = ResultPoint(0f, 0f)
        val isMirrorPreview = true
        val orientation = Orientation.LANDSCAPE
        val viewSize = Point(200, 100)
        val cameraPreviewSize = Point(200, 100)
        val result = qrToViewPointTransformer!!.transform(qrPoint, isMirrorPreview, orientation,
                viewSize, cameraPreviewSize)!!
        Assert.assertEquals(result.x, 0f, 0.0f)
        Assert.assertEquals(result.y, 100f, 0.0f)
    }
}