package com.example.qr_readerexample;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;

public class DecoderActivity extends Activity implements OnQRCodeReadListener {

  private TextView myTextView;
  private QRCodeReaderView mydecoderview;
  private ImageView line_image;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decoder);

    mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
    mydecoderview.setOnQRCodeReadListener(this);
    mydecoderview.setAutofocusInterval(1000L);

    myTextView = (TextView) findViewById(R.id.exampleTextView);

    line_image = (ImageView) findViewById(R.id.red_line_image);

    TranslateAnimation mAnimation =
        new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.5f);
    mAnimation.setDuration(1000);
    mAnimation.setRepeatCount(-1);
    mAnimation.setRepeatMode(Animation.REVERSE);
    mAnimation.setInterpolator(new LinearInterpolator());
    line_image.setAnimation(mAnimation);
  }

  @Override protected void onResume() {
    super.onResume();

    mydecoderview.startCamera();
  }

  @Override protected void onPause() {
    super.onPause();

    mydecoderview.stopCamera();
  }

  // Called when a QR is decoded
  // "text" : the text encoded in QR
  // "points" : points where QR control points are placed
  @Override public void onQRCodeRead(String text, PointF[] points) {
    myTextView.setText(text);
  }
}
