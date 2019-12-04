package demo.activity.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import demo.android.R;
import demo.widget.HistogramChartView;
import engine.android.util.AndroidUtil;
import engine.android.util.os.WindowUtil;

public class HistogramChartActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.setFullScreenMode(getWindow(), true);
        setContentView(R.layout.histogram_chart);
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.histogram_container);
        
        HistogramChartView view = new HistogramChartView(this);
        view.setUnit("温度/℃").setSize(100);
        layout.addView(view, new LayoutParams(75, LayoutParams.FILL_PARENT));
        
        view = new HistogramChartView(this);
        view.setUnit("电量/%").setSize(100).setLowerSizeMode(true).setAnimationMode(true);
        layout.addView(view, new LayoutParams(75, LayoutParams.FILL_PARENT));
        
        view = new HistogramChartView(this);
        view.setUnit("电量/%").setSize(80).setLowerSizeMode(true).setAnimationMode(true);
        layout.addView(view, new LayoutParams(75, LayoutParams.FILL_PARENT));
        
        view = new HistogramChartView(this);
        view.setUnit("电量/%").setSize(40).setLowerSizeMode(true).setAnimationMode(true);
        layout.addView(view, new LayoutParams(75, LayoutParams.FILL_PARENT));
        
        view = new HistogramChartView(this);
        view.setUnit("电量/%").setSize(2).setLowerSizeMode(true).setAnimationMode(true);
        layout.addView(view, new LayoutParams(75, LayoutParams.FILL_PARENT));
    }
}