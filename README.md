QRCodeReaderView [![Download](https://api.bintray.com/packages/dlazaro66/maven/QRCodeReaderView/images/download.svg) ](https://bintray.com/dlazaro66/maven/QRCodeReaderView/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-QRCodeReaderView-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1891) [![Build Status](https://travis-ci.org/dlazaro66/QRCodeReaderView.svg?branch=master)](https://travis-ci.org/dlazaro66/QRCodeReaderView)
===

#### Modification of ZXING Barcode Scanner project for easy Android QR-Code detection and AR purposes. ####

This project implements an Android view which show camera and notify when there's a QR code inside the preview.

Some Classes of camera controls and autofocus are taken and slightly modified from Barcode Scanner Android App.

You can also use this for Augmented Reality purposes, as you get QR control points coordinates when decoding.

Usage
-----

- Add a "QRCodeReaderView" in the layout editor like you actually do with a button for example.
- In your onCreate method, you can find the view as usual, using findViewById() function.
- Create an Activity which implements `onQRCodeReadListener`, and let implements required methods or set a `onQRCodeReadListener` to the QRCodeReaderView object
- Make sure you have camera permissions in order to use the library. (https://developer.android.com/training/permissions/requesting.html)

```xml

 <com.dlazaro66.qrcodereaderview.QRCodeReaderView
        android:id="@+id/qrdecoderview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

```

- Start & Stop camera preview in onPause() and onResume() overriden methods.
- You can place widgets or views over QRDecoderView.
 
```java
public class DecoderActivity extends Activity implements OnQRCodeReadListener {

    private TextView resultTextView;
	private QRCodeReaderView qrCodeReaderView;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder);
        
        qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);

    	  // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrCodeReaderView.setTorchEnabled(true);

        // Use this function to set front camera preview
        qrCodeReaderView.setFrontCamera();

        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
	@Override
	public void onQRCodeRead(String text, PointF[] points) {
		resultTextView.setText(text);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		qrCodeReaderView.startCamera();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		qrCodeReaderView.stopCamera();
	}
}
```

Add it to your project
----------------------

Add QRCodeReaderView dependency to your build.gradle

```groovy

dependencies{
      compile 'com.dlazaro66.qrcodereaderview:qrcodereaderview:2.0.3'
}
```

Note: There is an issue with gradle 2.10, if you declare your dependency and it can't be found in jCenter repository (`could not find qrcodereaderview.jar Searched in the following locations:` or similar), try to declare the library dependency like this:

```groovy

dependencies{
      compile ('com.dlazaro66.qrcodereaderview:qrcodereaderview:2.0.3@aar'){
        transitive = true
      }
}
```
And in some cases, you need to clean your Gradle cache
`./gradlew build --refresh-dependencies`

Do you want to contribute?
--------------------------

Please send a PR or open an issue with your comments.
See [CONTRIBUTING file](CONTRIBUTING.md) for further information

Libraries used in this project
------------------------------

* [ZXING][1]

Screenshots
-----------

![Image](../master/readme_images/app-example.gif?raw=true)

Developed By
------------

* David Lázaro Esparcia  

<a href="https://twitter.com/_dlazaro">
  <img alt="Follow me on Twitter" src="../master/readme_images/logo-twitter.png?raw=true" />
</a>
<a href="https://es.linkedin.com/pub/david-lázaro-esparcia/49/4b3/342">
  <img alt="Add me to Linkedin" src="../master/readme_images/logo-linkedin.png?raw=true" />
</a>


Who's using it
--------------

*Does your app use QRCodeReaderView? If you want to be featured on this list drop me a line.*

* [Swapcard][2]

License
-------

    Copyright 2017 David Lázaro

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: https://github.com/zxing/zxing/
[2]: https://www.swapcard.com/
