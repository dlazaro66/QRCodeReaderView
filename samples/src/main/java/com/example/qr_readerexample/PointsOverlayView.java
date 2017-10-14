package com.example.qr_readerexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class PointsOverlayView extends View {

  PointF[] points;
  private Paint paint;

  public PointsOverlayView(Context context) {
    super(context);
    init();
  }

  public PointsOverlayView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PointsOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    paint = new Paint();
    paint.setColor(Color.YELLOW);
    paint.setStyle(Paint.Style.FILL);
  }

  public void setPoints(PointF[] points) {
    this.points = points;
    invalidate();
  }

  @Override public void draw(Canvas canvas) {
    super.draw(canvas);
    if (points != null) {
      for (PointF pointF : points) {
        //画圆，指二维码识别时，对应的三个点的半径大小
        canvas.drawCircle(pointF.x, pointF.y, 10, paint);
      }
    }
  }
}
