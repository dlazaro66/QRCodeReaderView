package com.example.qr_readerexample.activity;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qr_readerexample.R;

import java.util.ArrayList;
import java.util.List;

import db.DataBaseHelper;
import edu.njupt.liuq.navigationtabbar.library.ntb.NavigationTabBar;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;


public class MainActivity extends Activity {

    static DataBaseHelper myDb;

    String[] chartLabels = {"A0", "A1"};
    private static final String TAG = "MainActivity";
    public static long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        endTime = System.currentTimeMillis(); //获取结束时间
        Log.i(TAG, "程序运行时间： " + (endTime - SplashActivity.startTime) + "ms");
        myDb = new DataBaseHelper(this);
        initUI();

    }

    private void initUI() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }


            //实例化条目，即添加元素样式
            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view0 = null;
                switch (position) {
                    case 0:
                        container.removeAllViews();
                        view0 = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.content_real_data, null, false);
                        container.addView(view0);

                        drawLine();
                        drawChart();
                        break;
                    case 1:
                        container.removeAllViews();
                        view0 = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.content_history_data, null, false);
                        container.addView(view0);
                        break;
                    case 2:
                        container.removeAllViews();
                        view0 = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.content_qr, null, false);
                        container.addView(view0);
                        break;

                }


                return view0;
            }

        });


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("实时数据")
                        //.badgeTitle("NTB")角标
                        .build()

        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("历史数据")
                        //.badgeTitle("with")角标
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_seventh))
                        .title("扫描设备")
                        //.badgeTitle("state")角标
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {

                navigationTabBar.getModels().get(position).hideBadge();

            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }


    private void drawChart() {


        String[] chartUnits = {" ", "%"};
        int[] chartColors = new int[]{getResources().getColor(R.color.color_FE5E63), getResources().getColor(R.color.color_6CABFA)};

        int numColumns = chartLabels.length;
        //columnChartView.setZoomEnabled(false);

        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < numColumns; ++i) {
            values = new ArrayList<SubcolumnValue>();
            //int height = (int) (Math.random() * 50) + 5;
            float aver = myDb.aver(chartLabels[i]);
            values.add(new SubcolumnValue(aver, chartColors[i]).setLabel(aver +
                    chartUnits[i]));

            Column column = new Column(values);
            column.setHasLabels(true);
            // column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);
            axisValues.add(new AxisValue(i).setLabel(chartLabels[i]));
        }


        ColumnChartData data = new ColumnChartData(columns);

        // value.
        Axis axisx = new Axis(axisValues);
        axisx.setTextColor(getResources().getColor(R.color.color_323232));
        axisx.setTextSize(13);
        axisx.setHasLines(false);
        axisx.setHasSeparationLine(false);
        data.setAxisXBottom(axisx);
        data.setFillRatio(0.5f);
//        Axis axisY = new Axis().setHasLines(true);
//        data.setAxisYLeft(axisY);
        //data.setAxisXBottom(null);
        ColumnChartView columnChartView = (ColumnChartView) findViewById(R.id.chart);
        columnChartView.setInteractive(false);
        columnChartView.setColumnChartData(data);
        columnChartView.setViewportCalculationEnabled(false);
        final Viewport v = new Viewport(columnChartView.getMaximumViewport());
        v.bottom = 0;
        v.top = 65;
        v.left = -0.5f;
        v.right = numColumns - 1 + 0.5f;
        columnChartView.setMaximumViewport(v);
        columnChartView.setCurrentViewport(v);
    }


    private void drawLine() {
        String[] lineLabels = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        int[] chartColors = new int[]{getResources().getColor(R.color.color_FE5E63), getResources().getColor(R.color.color_6CABFA)};
        int maxNumberOfLines = 2;
        int numberOfPoints = lineLabels.length;
        ValueShape shape = ValueShape.CIRCLE;
        int[][] randomNumbersTab = new int[maxNumberOfLines][numberOfPoints];

        //设置数据的数值信息
        for (int i = 0; i < maxNumberOfLines; i++) {

            Cursor res = myDb.getValue(chartLabels[i], numberOfPoints);
            int j = 0;
            while (res.moveToNext()) {
                randomNumbersTab[i][j] = Integer.valueOf(res.getString(0));
                Log.i(TAG, "res.getCount():" + res.getCount() + "   numberOfPoints:" + numberOfPoints + " getString ()" + res.getString(0));
                j++;
            }
            //randomNumbersTab[i][j] = (float) Math.random() * 100f;

        }


        List<Line> lines = new ArrayList<Line>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < maxNumberOfLines; ++i) {
            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }
            Line line = new Line(values);
            line.setColor(chartColors[i]);
            line.setShape(shape);
            line.setPointRadius(3);
            line.setStrokeWidth(1);
            line.setCubic(false);
            line.setFilled(false);
            line.setHasLabels(false);
            line.setHasLabelsOnlyForSelected(true);
            line.setHasLines(true);
            line.setHasPoints(true);
            //line.setPointColor(R.color.transparent);
            //line.setHasGradientToTransparent(true);
//            if (pointsHaveDifferentColor){
//                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
//            }
            lines.add(line);

        }

        LineChartData data = new LineChartData(lines);
        for (int i = 0; i < chartLabels.length; i++) {

            Cursor res = myDb.getTime(chartLabels[i], numberOfPoints);
            int j = 0;
            while (res.moveToNext()) {

                String time = String.valueOf(res.getString(0));
                String a[] = time.split(" ");
                lineLabels[j] = a[1];
                Log.i(TAG, "lineLabels [" + j + "]:" + lineLabels[j]);
                j++;
            }
            //将时间数组的值赋值到x轴上
            for (int n = 0; n < lineLabels.length; n++) {
                axisValues.add(new AxisValue(n).setLabel(lineLabels[n]));
            }

        }

        Axis axisX = new Axis(axisValues).setMaxLabelChars(5);
        axisX.setTextColor(getResources().getColor(R.color.color_969696))
                .setTextSize(10).setLineColor(getResources().getColor(R.color.color_e6e6e6));
        data.setAxisXBottom(axisX);
        Axis axisY = new Axis().setHasLines(true).setHasSeparationLine(false).setMaxLabelChars(3);
        axisY.setTextColor(getResources().getColor(R.color.color_969696));
        axisY.setTextSize(10);
        data.setAxisYLeft(axisY);
        data.setBaseValue(Float.NEGATIVE_INFINITY);

        LineChartView lineChartView = (LineChartView) findViewById(R.id.lineView);
        //lineChartView.setZoomEnabled(false);
        lineChartView.setScrollEnabled(true);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setValueSelectionEnabled(true);//点击折线点可以显示label
        lineChartView.setLineChartData(data);
        // Reset viewport height range to (0,100)
        lineChartView.setViewportCalculationEnabled(false);
        //让布局能够水平滑动要设置setCurrentViewport比setMaximumViewport小
        final Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.bottom = 0;
        v.top = 105;
        v.left = 0;
        v.right = numberOfPoints - 1 + 0.5f;
        lineChartView.setMaximumViewport(v);
        v.left = 0;
        v.right = Math.min(6, numberOfPoints - 1 + 0.5f);
        lineChartView.setCurrentViewport(v);

    }

}
