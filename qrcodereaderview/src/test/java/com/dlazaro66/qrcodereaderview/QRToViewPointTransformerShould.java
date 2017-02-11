package com.dlazaro66.qrcodereaderview;

import android.graphics.Point;
import android.graphics.PointF;

import com.google.zxing.ResultPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class QRToViewPointTransformerShould {

  private QRToViewPointTransformer qrToViewPointTransformer;

  @Before
  public void setUp() throws Exception {
    qrToViewPointTransformer = new QRToViewPointTransformer();
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void transformPortraitNotMirrorQRPointToViewPoint() throws Exception {
    ResultPoint qrPoint = new ResultPoint(100, 50);
    boolean isMirrorPreview = false;
    Orientation orientation = Orientation.PORTRAIT;
    Point viewSize = new Point(100, 200);
    Point cameraPreviewSize = new Point(200, 100);

    PointF result = qrToViewPointTransformer.transform(qrPoint, isMirrorPreview, orientation,
        viewSize, cameraPreviewSize);

    assertEquals(result.x, 50, 0.0f);
    assertEquals(result.y, 100, 0.0f);
  }

  @Test
  public void transformPortraitMirrorQRPointToViewPoint() throws Exception {
    ResultPoint qrPoint = new ResultPoint(0, 0);
    boolean isMirrorPreview = true;
    Orientation orientation = Orientation.PORTRAIT;
    Point viewSize = new Point(100, 200);
    Point cameraPreviewSize = new Point(200, 100);

    PointF result = qrToViewPointTransformer.transform(qrPoint, isMirrorPreview, orientation,
        viewSize, cameraPreviewSize);

    assertEquals(result.x, 100, 0.0f);
    assertEquals(result.y, 200, 0.0f);
  }

  @Test
  public void transformLandscapeNotMirrorQRPointToViewPoint() throws Exception {
    ResultPoint qrPoint = new ResultPoint(100, 50);
    boolean isMirrorPreview = false;
    Orientation orientation = Orientation.LANDSCAPE;
    Point viewSize = new Point(200, 100);
    Point cameraPreviewSize = new Point(200, 100);

    PointF result = qrToViewPointTransformer.transform(qrPoint, isMirrorPreview, orientation,
        viewSize, cameraPreviewSize);

    assertEquals(result.x, 100, 0.0f);
    assertEquals(result.y, 50, 0.0f);
  }

  @Test
  public void transformLandscapeMirrorQRPointToViewPoint() throws Exception {
    ResultPoint qrPoint = new ResultPoint(0, 0);
    boolean isMirrorPreview = true;
    Orientation orientation = Orientation.LANDSCAPE;
    Point viewSize = new Point(200, 100);
    Point cameraPreviewSize = new Point(200, 100);

    PointF result = qrToViewPointTransformer.transform(qrPoint, isMirrorPreview, orientation,
        viewSize, cameraPreviewSize);

    assertEquals(result.x, 0, 0.0f);
    assertEquals(result.y, 100, 0.0f);
  }
}