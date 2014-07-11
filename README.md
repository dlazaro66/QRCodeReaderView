QRCodeReaderView
================

#### Modification of ZXING Barcode Scanner project for easy Android QR-Code detection in portrait mode and AR purposes. ####

---
This project implements an Android view which show camera and notify when there's a QR code inside the preview.

Some Classes of camera controls and autofocus are taken and slightly modified from Barcode Scanner Android App.

The Library uses a "core.jar" file, which has been generated with ant tool cloning authors repository. It's recommended to generate core.jar again and integrate it into the project.

You can also use this for Augmented Reality purposes, as you get QR control points coordinates when decoding.

Original work and code can be found here: https://code.google.com/p/zxing/

---
From now, this project is licensed under the Apache License v2.0, the same as original project. It is not a copyleft license like the GPL; it is relatively generous about what you can do with the code.

---
How to use:

- Add library to your project.
- Check your App uses it as library (properties->Android->Library->Add...)
- Create an Activity which implements onQRCodeReadListener, and let implements required methods
- Make sure Activity orientation is PORTRAIT and give Camera permision in the manifest.xml
- Drag&Drop a "QRCodeReaderView" in the layout editor from "Custom & library views" like you actually do with a button for example

![Image](../master/readme_images/add_view.png?raw=true)

- In your onCreate method, you can find the view as usual, using findViewById() function.
- Set onQRCodeReadListener to the QRCodeReaderView.
- Start & Stop camera preview in onPause() and onResume() overriden methods.
- Use onQRCodeReadListener callbacks as you want.
- You can place averlay widgets or views over QRDecoderView
 
![Image](../master/readme_images/add_overlay.png?raw=true)

##### Example included in this repository: #####

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


![Image](../master/readme_images/app_example.png?raw=true)
