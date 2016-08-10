package com.example.qr_readerexample;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;

public class DecoderActivity extends Activity implements OnQRCodeReadListener {

  private TextView resultTextView;
  private QRCodeReaderView qrCodeReaderView;
  private CheckBox flashlightCheckBox;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decoder);

    qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
    resultTextView = (TextView) findViewById(R.id.result_text_view);
    flashlightCheckBox = (CheckBox) findViewById(R.id.flashlight_checkbox);

    qrCodeReaderView.setOnQRCodeReadListener(this);
    qrCodeReaderView.setAutofocusInterval(2000L);
    qrCodeReaderView.setQRDecodingEnabled(true);
    flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        qrCodeReaderView.setTorchEnabled(isChecked);
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();

    qrCodeReaderView.startCamera();
  }

  @Override protected void onPause() {
    super.onPause();

    qrCodeReaderView.stopCamera();
  }

  @Override public void onQRCodeRead(String text, PointF[] points) {
    resultTextView.setText(text);
  }
}
