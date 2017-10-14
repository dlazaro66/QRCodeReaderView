package com.example.qr_readerexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;

import java.lang.ref.WeakReference;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import db.DataBaseHelper;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static com.example.qr_readerexample.R.id.qrdecoderview;


public class DecoderActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener {

    private LineChartView lineChart;


    String[] date = new  String[]{"0","1","2","3","4","5","6","7","8","9","0","1","2","3","4","5","6","7","8"} ;//X轴的标注

    int[] score = new int[]{0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,0};//图表的数据
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    private static String recvdate = null;
    private static final String TAG = "DecoderActivity";
    private Button btnChartA1,btnChartA0;
    private CheckBox cbQR;//打开二维码扫描界面
    private static final int PORT = 9999;
    public static boolean  isdatavisible ;
    private static TcpServer tcpServer = null;
    private final MyHandler myHandler = new MyHandler(this);
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    ExecutorService exec = Executors.newCachedThreadPool();

    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    private ViewGroup mainLayout;
    private TextView resultTextView;
    private QRCodeReaderView qrCodeReaderView;
    private PointsOverlayView pointsOverlayView;
    private ViewStub viewchart;
    private TextView tvRecvData;
    private static String sensordata;
    static DataBaseHelper myDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_decoder);

        mainLayout = (ViewGroup) findViewById(R.id.main_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            requestCameraPermission();
        }
        context = this;

        init();
        bindReceiver();

        myDb = new DataBaseHelper(this);

        //暂停TCP通信
        startTCP();

    }

    private void init() {
        viewchart = (ViewStub)findViewById(R.id.stub_import);
        btnChartA1 = (Button)findViewById(R.id.bt_chartA1);
        btnChartA0 = (Button)findViewById(R.id.bt_chartA0);
        cbQR = (CheckBox) findViewById(R.id.cb_qr);
        cbQR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                qrCodeReaderView.setVisibility(View.VISIBLE);
                pointsOverlayView.setVisibility(View.VISIBLE);}
                else {
                    qrCodeReaderView.setVisibility(View.INVISIBLE);
                    pointsOverlayView.setVisibility(View.INVISIBLE);}

            }
        });

        btnChartA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewchart.setVisibility(View.VISIBLE);
               Showgraphs("A1");

            }
        });
        btnChartA0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewchart.setVisibility(View.VISIBLE);
                Showgraphs("A0");

            }
        });
        tvRecvData = (TextView)findViewById(R.id.tv_RecvData);

    }

    private void startTCP() {
       Log.i(TAG,"ip address is :"+getHostIP()+"\n  PORT = 9999");
        tcpServer = new TcpServer(PORT);
        exec.execute(tcpServer);
    }

    private static void AddData(String data,String devicename) {
        String systime = getsystime();
        boolean isInserted = myDb.insertData(systime,data,devicename);
        if (isInserted)
            Log.i(TAG,"Data Inserted time :"+systime+", data :"+data +", devicename:"+devicename);
        else
            Log.i(TAG,"Data not inserted");
    }


    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction) {
                case "tcpServerReceiver":
                    String msg = intent.getStringExtra("tcpServerReceiver");

                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = msg;
                    myHandler.sendMessage(message);
                    break;
            }
        }
    }

    private  class  MyHandler extends android.os.Handler {
        private final WeakReference<DecoderActivity> mActivity;

        MyHandler(DecoderActivity activity) {
            mActivity = new WeakReference<DecoderActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DecoderActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        recvdate = msg.obj.toString();

                        String devidename = recvdate.substring(0, 2);
                        Log.i(TAG, "device name :" + devidename);
                        switch (devidename) {
                            case "A0":
                                sensordata = recvdate.substring(2, 3);
                                //向数据库添加数据
                                AddData(sensordata ,"A0");
                                tvRecvData.setText("设备A0的数据:"+sensordata);
                                Log.i(TAG, "A0 data is :" + sensordata);
                                break;
                            case "A1":
                                sensordata = recvdate.substring(2, 4).trim();
                                tvRecvData.setText("设备A1的数据:"+sensordata);
                                //向数据库添加数据
                                AddData(sensordata ,"A1");
                                Log.i(TAG, "A1 data is :" + sensordata +"; length is "+recvdate.length());

                                break;
                            default:
                                break;

                        }
                        break;
                }
            }
        }
    }


    private void bindReceiver() {
        IntentFilter intentFilter = new IntentFilter("tcpServerReceiver");
        registerReceiver(myBroadcastReceiver, intentFilter);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
        //关闭折线图
        if (lineChart==null){
        }else {
            lineChart.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mainLayout, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show();
            initQRCodeReaderView();
        } else {
            Snackbar.make(mainLayout, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(String devicename, PointF[] points) {
        resultTextView.setText(devicename);
        //扫描时，让图表设为VISIBLE
        viewchart.setVisibility(View.VISIBLE);
        switch (devicename){

            case "A0":
                Log.i(TAG,"QR扫描结果为A0");
                Showgraphs(devicename);
                break;
            case "A1":
                Showgraphs(devicename);
                Log.i(TAG,"QR扫描结果为A1");
                break;


        }
        //扫描时，不显示三个点
        pointsOverlayView.setPoints(points);
    }

    private void Showgraphs(String tablename) {
        //只显示20行
        Cursor res =  myDb.getValue(tablename,19);
        Log.i(TAG,"res.getCount():"+res.getCount());
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (res.moveToNext()){
            buffer.append("Value :"+res.getString(0)+"\n");
            score[i] = Integer.valueOf(res.getString(0));
            Log.i(TAG,"score["+i+"] : " +score[i]);
            i++;
        }

        res = myDb.getTime(tablename,19);
        if (res.getCount()==0){
            return;
        }
        int j = 0;
        while (res.moveToNext()){
            date[j] = res.getString(0);
            Log.i(TAG,"date ["+j+"]:"+date[j]);
            j++;
        }

        lineChart = (LineChartView)findViewById(R.id.line_chart);
        getAxisXLables();//获取x轴的的数据
        getAxisPoints();//获取坐标点
        initLineChart(tablename,"value");//初始化
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(mainLayout, "Camera access is required to display the camera preview.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(DecoderActivity.this, new String[]{
                            Manifest.permission.CAMERA
                    }, MY_PERMISSION_REQUEST_CAMERA);
                }
            }).show();
        } else {
            Snackbar.make(mainLayout, "Permission is not available. Requesting camera permission.",
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, MY_PERMISSION_REQUEST_CAMERA);
        }
    }

    public void initQRCodeReaderView() {
        View content = getLayoutInflater().inflate(R.layout.content_decoder, mainLayout, true);

        qrCodeReaderView = (QRCodeReaderView) content.findViewById(qrdecoderview);
        resultTextView = (TextView) content.findViewById(R.id.result_text_view);
        pointsOverlayView = (PointsOverlayView) content.findViewById(R.id.points_overlay_view);
        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onDestroy() {
        //应用销毁时，关闭TCP通讯
        tcpServer.closeSelf();
        super.onDestroy();
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i(TAG, "SocketException");
            e.printStackTrace();
        }
        Log.i(TAG,"IP ADDRESS :"+hostIp);
        return hostIp;

    }

    /**
     * 初始化LineChart的一些设置
     */
    private void initLineChart(String xname,String yname){

        Line line = new Line(mPointValues).setColor(Color.parseColor("#00868B"));  //黑色偏蓝
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.DIAMOND);//折线图上每个数据点的形状  这里是倾斜的正方形 （有三种 ：ValueShape.DIAMOND钻石(倾斜的正方形)  ValueShape.CIRCLE圆  ValueShape.SQUARE 方）
        line.setCubic(false);//曲线是否平滑
//	    line.setStrokeWidth(3);//线条的粗细，默认是3
        line.setFilled(true);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//		line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X轴下面坐标轴字体是斜的显示还是直的，true是斜的显示
	    //axisX.setTextColor(Color.WHITE);  //设置字体颜色
        axisX.setTextColor(Color.parseColor("#00868B"));//西门子色
	    //axisX.setName(xname);  //表格名称
        axisX.setTextSize(20);//设置字体大小
        axisX.setMaxLabelChars(5); //最多几个X轴坐标
        //axisX.setValues(mAxisXValues);  //填充X轴的坐标名称,即日期信息
        data.setAxisXBottom(axisX); //x 轴在底部（底部即x轴的底部）
//	    data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线



        Axis axisY = new Axis();  //Y轴
        //axisY.setName(yname);//y轴标注
        axisY.setTextColor(Color.parseColor("#00868B"));//西门子色
        axisY.setTextSize(20);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边(左边即y轴的左边)
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);  //缩放类型，水平
        lineChart.setMaxZoom((float) 4);//缩放比例（19个数，一页内只显示19/3 个值）
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 尼玛搞的老子好辛苦！！！见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         * 下面几句可以设置X轴数据的显示个数（x轴0-7个数据），当数据点个数小于（29）的时候，缩小到极致hellochart默认的是所有显示。当数据点个数大于（29）的时候，
         * 若不设置axisX.setMaxLabelChars(int count)这句话,则会自动适配X轴所能显示的尽量合适的数据个数。
         * 若设置axisX.setMaxLabelChars(int count)这句话,
         * 33个数据点测试，若 axisX.setMaxLabelChars(10);里面的10大于v.right= 7; 里面的7，则
         刚开始X轴显示7条数据，然后缩放的时候X轴的个数会保证大于7小于10
         若小于v.right= 7;中的7,反正我感觉是这两句都好像失效了的样子 - -!
         * 并且Y轴是根据数据的大小自动设置Y轴上限
         * 若这儿不设置 v.right= 7; 这句话，则图表刚开始就会尽可能的显示所有数据，交互性太差
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 0;
        lineChart.setCurrentViewport(v);
    }

    /**
     * X 轴的显示
     */
    private void getAxisXLables(){
        //data x轴显示日期
        //加载前，先清除之前的数值
        mAxisXValues.clear();
        for (int i = 0; i < date.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(date[i]));
        }
        Log.i(TAG,"date.length :"+date.length);
    }
    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints(){
        //先清除数据
        mPointValues.clear();
        for (int i = 0; i < score.length; i++) {
            mPointValues.add(new PointValue(i, score[i]));
        }
        Log.i(TAG,"score.length :"+score.length);
    }
    private  static  String getsystime(){
        SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }
}
