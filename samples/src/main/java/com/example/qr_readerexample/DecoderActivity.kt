package com.example.qr_readerexample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener
import com.google.android.material.snackbar.Snackbar

class DecoderActivity : AppCompatActivity(), OnRequestPermissionsResultCallback, OnQRCodeReadListener {
    private var mainLayout: ViewGroup? = null
    private var resultTextView: TextView? = null
    private var qrCodeReaderView: QRCodeReaderView? = null
    private var flashlightCheckBox: CheckBox? = null
    private var enableDecodingCheckBox: CheckBox? = null
    private var pointsOverlayView: PointsOverlayView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decoder)
        mainLayout = findViewById<View>(R.id.main_layout) as ViewGroup
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView()
        } else {
            requestCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        qrCodeReaderView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        qrCodeReaderView?.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return
        }
        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mainLayout?.let { Snackbar.make(it, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show() }
            initQRCodeReaderView()
        } else {
            mainLayout?.let {
                Snackbar.make(it, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
                        .show()
            }
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    override fun onQRCodeRead(text: String?, points: Array<PointF?>?) {
        resultTextView?.text = text
        pointsOverlayView?.setPoints(points)
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            mainLayout?.let {
                Snackbar.make(it, "Camera access is required to display the camera preview.",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK") {
                    ActivityCompat.requestPermissions(this@DecoderActivity, arrayOf(
                            Manifest.permission.CAMERA
                    ), MY_PERMISSION_REQUEST_CAMERA)
                }.show()
            }
        } else {
            mainLayout?.let {
                Snackbar.make(it, "Permission is not available. Requesting camera permission.",
                        Snackbar.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.CAMERA
            ), MY_PERMISSION_REQUEST_CAMERA)
        }
    }

    private fun initQRCodeReaderView() {
        val content: View = layoutInflater.inflate(R.layout.content_decoder, mainLayout, true)
        qrCodeReaderView = content.findViewById(R.id.qrdecoderview)
        resultTextView = content.findViewById(R.id.result_text_view)
        flashlightCheckBox = content.findViewById(R.id.flashlight_checkbox)
        enableDecodingCheckBox = content.findViewById(R.id.enable_decoding_checkbox)
        pointsOverlayView = content.findViewById(R.id.points_overlay_view)
        qrCodeReaderView?.setAutofocusInterval(2000L)
        qrCodeReaderView?.setOnQRCodeReadListener(this)
        qrCodeReaderView?.setBackCamera()
        flashlightCheckBox?.setOnCheckedChangeListener { compoundButton, isChecked -> qrCodeReaderView?.setTorchEnabled(isChecked) }
        enableDecodingCheckBox?.setOnCheckedChangeListener { compoundButton, isChecked -> qrCodeReaderView?.setQRDecodingEnabled(isChecked) }
        qrCodeReaderView?.startCamera()
    }

    companion object {
        private const val MY_PERMISSION_REQUEST_CAMERA = 0
    }
}