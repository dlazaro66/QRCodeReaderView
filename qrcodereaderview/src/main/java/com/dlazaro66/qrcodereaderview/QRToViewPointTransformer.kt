package com.dlazaro66.qrcodereaderview

import android.graphics.Point
import android.graphics.PointF
import com.google.zxing.ResultPoint

class QRToViewPointTransformer {
    fun transform(qrPoints: Array<ResultPoint>, isMirrorPreview: Boolean,
                  orientation: Orientation,
                  viewSize: Point?, cameraPreviewSize: Point?): Array<PointF?> {
        val transformedPoints = arrayOfNulls<PointF>(qrPoints.size)
        for ((index, qrPoint) in qrPoints.withIndex()) {
            val transformedPoint = viewSize?.let {
                cameraPreviewSize?.let { it1 ->
                    transform(qrPoint, isMirrorPreview, orientation, it,
                            it1)
                }
            }
            transformedPoints[index] = transformedPoint
        }
        return transformedPoints
    }

    fun transform(qrPoint: ResultPoint, isMirrorPreview: Boolean, orientation: Orientation,
                  viewSize: Point, cameraPreviewSize: Point): PointF? {
        val previewX = cameraPreviewSize.x.toFloat()
        val previewY = cameraPreviewSize.y.toFloat()
        var transformedPoint: PointF? = null
        val scaleX: Float
        val scaleY: Float
        if (orientation == Orientation.PORTRAIT) {
            scaleX = viewSize.x / previewY
            scaleY = viewSize.y / previewX
            transformedPoint = PointF((previewY - qrPoint.y) * scaleX, qrPoint.x * scaleY)
            if (isMirrorPreview) {
                transformedPoint.y = viewSize.y - transformedPoint.y
            }
        } else if (orientation == Orientation.LANDSCAPE) {
            scaleX = viewSize.x / previewX
            scaleY = viewSize.y / previewY
            transformedPoint = PointF(viewSize.x - qrPoint.x * scaleX,
                    viewSize.y - qrPoint.y * scaleY)
            if (isMirrorPreview) {
                transformedPoint.x = viewSize.x - transformedPoint.x
            }
        }
        return transformedPoint
    }
}