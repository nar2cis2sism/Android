package demo.activity.example;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import demo.widget.XYChartView;


public class XYChartActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        XYChartView v = new XYChartView(this);
        v.setBackgroundColor(Color.WHITE);
        
        v.setXUnit("月");
        v.setCoordsX(new String[]{"1月", "2月", "3月", "4月", "5月", "6月", 
                "7月", "8月", "9月", "10月", "11月", "12月"});
        v.setYUnit("kWh");
        v.setCoordsY(8);
        
        float[] coordsX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        v.addLine("电冰箱", coordsX, new float[]{20, 84, 56, 70, 130, 125, 10, 40, 60, 45, 77, 195});
        v.addLine("电脑", coordsX, new float[]{10, 150, 20, 210, 15, 42, 67, 94, 12, 99, 110, 66});
        
        setContentView(v);
    }
}