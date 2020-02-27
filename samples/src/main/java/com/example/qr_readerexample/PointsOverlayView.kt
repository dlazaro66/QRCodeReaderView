package com.example.qr_readerexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class PointsOverlayView : View {
    private var paint: Paint? = null
    private var mPoints: Array<PointF>? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint?.color = Color.YELLOW
        paint?.style = Paint.Style.FILL
    }

    fun setPoints(points: Array<PointF>) {
        this.mPoints = points
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        mPoints?.let {
            it.forEach { it1 ->
                paint?.let { it2 -> canvas.drawCircle(it1.x, it1.y, 10f, it2) }
            }
        }
    }
}