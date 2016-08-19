/*
 * Copyright 2014 David Lázaro Esparcia.
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
package com.dlazaro66.qrcodereaderview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * QRCodeReaderView Class which uses ZXING lib and let you easily integrate a QR decoder view.
 * Take some classes and made some modifications in the original ZXING - Barcode Scanner project.
 *
 * @author David Lázaro
 */
public class QRCodeReaderView extends SurfaceView
    implements SurfaceHolder.Callback, Camera.PreviewCallback {

  public interface OnQRCodeReadListener {

    void onQRCodeRead(String text, PointF[] points);
  }

  private OnQRCodeReadListener mOnQRCodeReadListener;

  private static final String TAG = QRCodeReaderView.class.getName();

  private QRCodeReader mQRCodeReader;
  private int mPreviewWidth;
  private int mPreviewHeight;
  private CameraManager mCameraManager;
  private boolean mQrDecodingEnabled = true;
  private DecodeFrameTask decodeFrameTask;

  public QRCodeReaderView(Context context) {
    this(context, null);
  }

  public QRCodeReaderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    if (checkCameraHardware()) {
      mCameraManager = new CameraManager(getContext());
      mCameraManager.setPreviewCallback(this);

      getHolder().addCallback(this);
    } else {
      throw new RuntimeException("Error: Camera not found");
    }
  }

  /**
   * Set the callback to return decoding result
   *
   * @param onQRCodeReadListener the listener
   */
  public void setOnQRCodeReadListener(OnQRCodeReadListener onQRCodeReadListener) {
    mOnQRCodeReadListener = onQRCodeReadListener;
  }

  /**
   * Set QR decoding enabled/disabled.
   * default value is true
   *
   * @param qrDecodingEnabled decoding enabled/disabled.
   */
  public void setQRDecodingEnabled(boolean qrDecodingEnabled) {
    this.mQrDecodingEnabled = qrDecodingEnabled;
  }

  /**
   * Starts camera preview and decoding
   */
  public void startCamera() {
    mCameraManager.startPreview();
  }

  /**
   * Stop camera preview and decoding
   */
  public void stopCamera() {
    mCameraManager.stopPreview();
  }

  /**
   * Set Camera autofocus interval value
   * default value is 5000 ms.
   *
   * @param autofocusIntervalInMs autofocus interval value
   */
  public void setAutofocusInterval(long autofocusIntervalInMs) {
    if (mCameraManager != null) {
      mCameraManager.setAutofocusInterval(autofocusIntervalInMs);
    }
  }

  /**
   * Set Torch enabled/disabled.
   * default value is false
   *
   * @param enabled torch enabled/disabled.
   */
  public void setTorchEnabled(boolean enabled) {
    if (mCameraManager != null) {
      mCameraManager.setTorchEnabled(enabled);
    }
  }

  /**
   * Allows user to specify the camera ID, rather than determine
   * it automatically based on available cameras and their orientation.
   *
   * @param cameraId camera ID of the camera to use. A negative value means "no preference".
   */
  public void setPreviewCameraId(int cameraId) {
    mCameraManager.setPreviewCameraId(cameraId);
  }

  /**
   * Camera preview from device back camera
   */
  public void setBackCamera() {
    setPreviewCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
  }

  /**
   * Camera preview from device front camera
   */
  public void setFrontCamera() {
    setPreviewCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (decodeFrameTask != null) {
      decodeFrameTask.cancel(true);
      decodeFrameTask = null;
    }
  }

  /****************************************************
   * SurfaceHolder.Callback,Camera.PreviewCallback
   ****************************************************/

  @Override public void surfaceCreated(SurfaceHolder holder) {
    Log.d(TAG, "surfaceCreated");

    try {
      // Indicate camera, our View dimensions
      mCameraManager.openDriver(holder, this.getWidth(), this.getHeight());
    } catch (IOException e) {
      Log.w(TAG, "Can not openDriver: " + e.getMessage());
      mCameraManager.closeDriver();
    }

    try {
      mQRCodeReader = new QRCodeReader();
      mCameraManager.startPreview();
    } catch (Exception e) {
      Log.e(TAG, "Exception: " + e.getMessage());
      mCameraManager.closeDriver();
    }
  }

  @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.d(TAG, "surfaceChanged");

    if (holder.getSurface() == null) {
      Log.e(TAG, "Error: preview surface does not exist");
      return;
    }

    if (mCameraManager.getPreviewSize() == null) {
      Log.e(TAG, "Error: preview size does not exist");
      return;
    }

    mPreviewWidth = mCameraManager.getPreviewSize().x;
    mPreviewHeight = mCameraManager.getPreviewSize().y;

    mCameraManager.stopPreview();

    // Fix the camera sensor rotation
    mCameraManager.setPreviewCallback(this);
    mCameraManager.setDisplayOrientation(getCameraDisplayOrientation());

    mCameraManager.startPreview();
  }

  @Override public void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(TAG, "surfaceDestroyed");

    mCameraManager.setPreviewCallback(null);
    mCameraManager.stopPreview();
    mCameraManager.closeDriver();
  }

  // Called when camera take a frame
  @Override public void onPreviewFrame(byte[] data, Camera camera) {
    if (!mQrDecodingEnabled || (decodeFrameTask != null
        && decodeFrameTask.getStatus() == AsyncTask.Status.RUNNING)) {
      return;
    }

    decodeFrameTask = new DecodeFrameTask(this);
    decodeFrameTask.execute(data);
  }

  /** Check if this device has a camera */
  private boolean checkCameraHardware() {
    if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      // this device has a camera
      return true;
    } else if (getContext().getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
      // this device has a front camera
      return true;
    } else if (getContext().getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
      // this device has any camera
      return true;
    } else {
      // no camera on this device
      return false;
    }
  }

  /**
   * Fix for the camera Sensor on some devices (ex.: Nexus 5x)
   * http://developer.android.com/intl/pt-br/reference/android/hardware/Camera.html#setDisplayOrientation(int)
   */
  @SuppressWarnings("deprecation") private int getCameraDisplayOrientation() {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
      return 90;
    }

    Camera.CameraInfo info = new Camera.CameraInfo();
    android.hardware.Camera.getCameraInfo(mCameraManager.getPreviewCameraId(), info);
    WindowManager windowManager =
        (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    int rotation = windowManager.getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
    }

    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360;  // compensate the mirror
    } else {  // back-facing
      result = (info.orientation - degrees + 360) % 360;
    }
    return result;
  }

  private static class DecodeFrameTask extends AsyncTask<byte[], Void, Result> {

    private final WeakReference<QRCodeReaderView> viewRef;

    public DecodeFrameTask(QRCodeReaderView view) {
      viewRef = new WeakReference<>(view);
    }

    @Override protected Result doInBackground(byte[]... params) {
      final QRCodeReaderView view = viewRef.get();
      if (view == null) {
        return null;
      }

      final PlanarYUVLuminanceSource source =
          view.mCameraManager.buildLuminanceSource(params[0], view.mPreviewWidth,
              view.mPreviewHeight);

      final HybridBinarizer hybBin = new HybridBinarizer(source);
      final BinaryBitmap bitmap = new BinaryBitmap(hybBin);

      try {
        return view.mQRCodeReader.decode(bitmap);
      } catch (ChecksumException e) {
        Log.d(TAG, "ChecksumException", e);
      } catch (NotFoundException e) {
        Log.d(TAG, "No QR Code found");
      } catch (FormatException e) {
        Log.d(TAG, "FormatException", e);
      } finally {
        view.mQRCodeReader.reset();
      }

      return null;
    }

    @Override protected void onPostExecute(Result result) {
      super.onPostExecute(result);

      final QRCodeReaderView view = viewRef.get();

      // Notify we found a QRCode
      if (view != null && result != null && view.mOnQRCodeReadListener != null) {
        // Transform resultPoints to View coordinates
        final PointF[] transformedPoints =
            transformToViewCoordinates(view, result.getResultPoints());
        view.mOnQRCodeReadListener.onQRCodeRead(result.getText(), transformedPoints);
      }
    }

    /**
     * Transform result to surfaceView coordinates
     *
     * This method is needed because coordinates are given in landscape camera coordinates when
     * device is in portrait mode and different coordinates otherwise.
     *
     * @return a new PointF array with transformed points
     */
    private PointF[] transformToViewCoordinates(QRCodeReaderView view, ResultPoint[] resultPoints) {
      int orientation = view.getCameraDisplayOrientation();
      if (orientation == 90 || orientation == 270) {
        return transformToPortraitViewCoordinates(view, resultPoints);
      } else {
        return transformToLandscapeViewCoordinates(view, resultPoints);
      }
    }

    private PointF[] transformToLandscapeViewCoordinates(QRCodeReaderView view,
        ResultPoint[] resultPoints) {
      PointF[] transformedPoints = new PointF[resultPoints.length];
      int index = 0;
      float origX = view.mCameraManager.getPreviewSize().x;
      float origY = view.mCameraManager.getPreviewSize().y;
      float scaleX = view.getWidth() / origX;
      float scaleY = view.getHeight() / origY;

      for (ResultPoint point : resultPoints) {
        PointF transformedPoint = new PointF(view.getWidth() - point.getX() * scaleX,
            view.getHeight() - point.getY() * scaleY);
        if (view.mCameraManager.getPreviewCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
          transformedPoint.x = view.getWidth() - transformedPoint.x;
        }
        transformedPoints[index] = transformedPoint;
        index++;
      }

      return transformedPoints;
    }

    private PointF[] transformToPortraitViewCoordinates(QRCodeReaderView view,
        ResultPoint[] resultPoints) {
      PointF[] transformedPoints = new PointF[resultPoints.length];

      int index = 0;
      float previewX = view.mCameraManager.getPreviewSize().x;
      float previewY = view.mCameraManager.getPreviewSize().y;
      float scaleX = view.getWidth() / previewY;
      float scaleY = view.getHeight() / previewX;

      for (ResultPoint point : resultPoints) {
        PointF transformedPoint =
            new PointF((previewY - point.getY()) * scaleX, point.getX() * scaleY);
        if (view.mCameraManager.getPreviewCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
          transformedPoint.y = view.getHeight() - transformedPoint.y;
        }
        transformedPoints[index] = transformedPoint;
        index++;
      }
      return transformedPoints;
    }
  }
}
