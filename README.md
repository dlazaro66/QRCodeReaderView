QRCodeReaderView [![Download](https://api.bintray.com/packages/dlazaro66/maven/QRCodeReaderView/images/download.svg) ](https://bintray.com/dlazaro66/maven/QRCodeReaderView/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-QRCodeReaderView-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1891)
===

#### Modification of ZXING Barcode Scanner project for easy Android QR-Code detection in portrait mode and AR purposes. ####

This project implements an Android view which show camera and notify when there's a QR code inside the preview.

Some Classes of camera controls and autofocus are taken and slightly modified from Barcode Scanner Android App.

You can also use this for Augmented Reality purposes, as you get QR control points coordinates when decoding.

Usage
-----

How to use:

- Create an Activity which implements onQRCodeReadListener, and let implements required methods
- Make sure Activity orientation is PORTRAIT and give Camera permision in the manifest.xml
- Add a "QRCodeReaderView" in the layout editor like you actually do with a button for example

```xml

 <com.dlazaro66.qrcodereaderview.QRCodeReaderView
        android:id="@+id/qrdecoderview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

```


- In your onCreate method, you can find the view as usual, using findViewById() function.
- Set onQRCodeReadListener to the QRCodeReaderView.
- Start & Stop camera preview in onPause() and onResume() overriden methods.
- Use onQRCodeReadListener callbacks as you want.
- You can place widgets or views over QRDecoderView
 
```java
	public class DecoderActivity extends Activity implements OnQRCodeReadListener {

    private TextView myTextView;
	private QRCodeReaderView mydecoderview;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder);
        
        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setOnQRCodeReadListener(this);
        
        myTextView = (TextView) findViewById(R.id.exampleTextView);
    }

    
    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
	@Override
	public void onQRCodeRead(String text, PointF[] points) {
		myTextView.setText(text);
	}

	
	// Called when your device have no camera
	@Override
	public void cameraNotFound() {
		
	}

	// Called when there's no QR codes in the camera preview image
	@Override
	public void QRCodeNotFoundOnCamImage() {
		
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		mydecoderview.getCameraManager().startPreview();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mydecoderview.getCameraManager().stopPreview();
	}
}
```


Add it to your project
----------------------


Add QRCodeReaderView dependency to your build.gradle

```groovy

dependencies{
    compile 'com.dlazaro66.qrcodereaderview:qrcodereaderview:1.0.0'
}

```

Do you want to contribute?
--------------------------

Please send a PR or open an issue with your comments!

Libraries used in this project
------------------------------

* [ZXING] [1]

Screenshots
-----------

![Image](../master/readme_images/app_example.png?raw=true)


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

* [Swapcard][6]

Contributors
------------
* [David Lázaro Esparcia][2]
* [Samuel Guirado Navarro][3]
* [Daniel Comas Fernández][4]
* [Kirill Boyarshinov][5]

License
-------

    Copyright 2013 David Lázaro

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
[2]: https://github.com/dlazaro66
[3]: https://github.com/saguinav
[4]: https://github.com/danicomas
[5]: https://github.com/kboyarshinov
[6]: https://www.swapcard.com/




















