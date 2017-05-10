package demo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import engine.android.util.AndroidUtil;
import engine.android.util.Util;

public class BatteryActivity extends Activity {
    
    TextView tv_batteryStatus;
    TextView tv_batteryLevel;
    TextView tv_batteryHealth;
    TextView tv_batteryTemperature;
    TextView tv_batteryVoltage;
    TextView tv_batteryTechnology;
    
    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryStatus(intent);
            updateBatteryLevel(intent);
            updateBatteryHealth(intent);
            updateBatteryTemperature(intent);
            updateBatteryVoltage(intent);
            updateBatteryTechnology(intent);
        }
        
        private void updateBatteryStatus(Intent intent)
        {
            //电池状态
            String text = "电池状态: ";
            int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            switch (batteryStatus) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                text += "正在充电" + getBatteryPlugged(intent);
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                text += "正在放电";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                text += "未在充电";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                text += "已充满" + getBatteryPlugged(intent);
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                text += "状态未知";
            }
            
            tv_batteryStatus.setText(text);
        }
        
        private String getBatteryPlugged(Intent intent)
        {
            //电源类型
            String text = "";
            int batteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            switch (batteryPlugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                text = "(AC)";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                text = "(USB)";
                break;
            }
            
            return text;
        }
        
        private void updateBatteryLevel(Intent intent)
        {
            //电池电量
            String text = "电池电量: ";
            int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            tv_batteryLevel.setText(text + batteryLevel * 100 / batteryScale + "%");
        }
        
        private void updateBatteryHealth(Intent intent)
        {
            //电池健康
            String text = "电池健康: ";
            int batteryHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 
                    BatteryManager.BATTERY_HEALTH_UNKNOWN);
            switch (batteryHealth) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                text += "良好";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                text += "过热";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                text += "损坏";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                text += "过压";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                text += "未知故障";
                break;
            case 7/*BatteryManager.BATTERY_HEALTH_COLD*/:
                text += "过冷";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                text += "未知";
            }
            
            tv_batteryHealth.setText(text);
        }
        
        private void updateBatteryTemperature(Intent intent)
        {
            //电池温度
            String text = "电池温度: ";
            int batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            tv_batteryTemperature.setText(text + batteryTemperature / 10f + "℃");
        }
        
        private void updateBatteryVoltage(Intent intent)
        {
            //电池电压
            String text = "电池电压: ";
            int batteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            tv_batteryVoltage.setText(text + batteryVoltage + "mv");
        }
        
        private void updateBatteryTechnology(Intent intent)
        {
            //电池类型
            String text = "电池类型: ";
            String batteryTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            tv_batteryTechnology.setText(text + Util.getString(batteryTechnology, ""));
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidUtil.setFullScreenMode(this, true);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = 5;
        params.rightMargin = 5;
        
        layout.addView(tv_batteryStatus         = createTextView(), params);
        layout.addView(tv_batteryLevel          = createTextView(), params);
        layout.addView(tv_batteryHealth         = createTextView(), params);
        layout.addView(tv_batteryTemperature    = createTextView(), params);
        layout.addView(tv_batteryVoltage        = createTextView(), params);
        layout.addView(tv_batteryTechnology     = createTextView(), params);
        
        setContentView(layout);
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    
    @Override
    protected void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }
    
    private TextView createTextView()
    {
        TextView tv = new TextView(this);
        
        tv.setTextSize(18);
        tv.setTextColor(Color.WHITE);
        
        return tv;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}