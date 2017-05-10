package demo.activity.example;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import demo.android.R;
import demo.wheel.NumericWheelAdapter;
import demo.wheel.OnWheelChangedListener;
import demo.wheel.WheelView;
import engine.android.core.util.CalendarFormat;

import java.util.Calendar;

public class DateTimePickerActivity extends Activity implements OnWheelChangedListener, OnCheckedChangeListener {
    
    Calendar cal;
    
    TextView date;
    CheckBox allDay;
    WheelView month;
    WheelView day;
    WheelView hour;
    WheelView minute;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_time_picker);
        
        cal = Calendar.getInstance();
        CalendarFormat.formatPrecision(cal, Calendar.MINUTE);
        
        allDay = (CheckBox) findViewById(R.id.allDay);
        allDay.setOnCheckedChangeListener(this);
        initDate();
        initMonth();
        initDay();
        initHour();
        initMinute();
    }
    
    private void initDate()
    {
        date = (TextView) findViewById(R.id.date);
        updateDate();
    }
    
    private void updateDate()
    {
        date.setText(CalendarFormat.formatDateTimeBySetting(this, cal.getTimeInMillis(), 
                CalendarFormat.SHOW_WEEKDAY | CalendarFormat.ABBREV_WEEKDAY, !allDay.isChecked()));
    }
    
    private void initMonth()
    {
        month = getWheel(R.id.month);
        month.setAdapter(new NumericWheelAdapter(1, 12, "%02d月"));
        month.setCyclic(true);
        month.setCurrentItem(cal.get(Calendar.MONTH));
        month.addChangingListener(this);
    }
    
    private void initDay()
    {
        day = getWheel(R.id.day);
        day.setCyclic(true);
        updateDay();
        day.addChangingListener(this);
    }
    
    private void updateDay()
    {
        day.setAdapter(new NumericWheelAdapter(cal.getActualMinimum(Calendar.DAY_OF_MONTH), 
                cal.getActualMaximum(Calendar.DAY_OF_MONTH), "%02d"));
        day.setCurrentItem(cal.get(Calendar.DAY_OF_MONTH) - 1);
    }
    
    private void initHour()
    {
        hour = getWheel(R.id.hour);
        hour.setLabel("时");
        hour.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));
        hour.setCyclic(true);
        hour.setCurrentItem(cal.get(Calendar.HOUR_OF_DAY));
        hour.addChangingListener(this);
    }
    
    private void initMinute()
    {
        minute = getWheel(R.id.minute);
        minute.setLabel("分");
        minute.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
        minute.setCyclic(true);
        minute.setCurrentItem(cal.get(Calendar.MINUTE));
        minute.addChangingListener(this);
    }

    private WheelView getWheel(int resId)
    {
        WheelView wheel = (WheelView) findViewById(resId);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
        return wheel;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue)
    {
        if (wheel == month)
        {
            if (newValue == 0 && oldValue == 11)
            {
                cal.add(Calendar.MONTH, 1);
            }
            else if (newValue == 11 && oldValue == 0)
            {
                cal.add(Calendar.MONTH, -1);
            }
            else
            {
                cal.add(Calendar.MONTH, newValue - oldValue);
            }

//          cal.set(Calendar.MONTH, newValue);//我们不能直接设置月份，因为每月的天数会变
            updateDay();
        }
        else if (wheel == day)
        {
            cal.set(Calendar.DAY_OF_MONTH, newValue + 1);
        }
        else if (wheel == hour)
        {
            cal.set(Calendar.HOUR_OF_DAY, newValue);
        }
        else if (wheel == minute)
        {
            cal.set(Calendar.MINUTE, newValue);
        }
        
        updateDate();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (isChecked)
        {
            CalendarFormat.formatAllDay(cal);
        }
        else
        {
            Time time = new Time();
            time.setToNow();
            cal.set(Calendar.HOUR_OF_DAY, time.hour);
            cal.set(Calendar.MINUTE, time.minute);
            hour.setCurrentItem(time.hour);
            minute.setCurrentItem(time.minute);
        }
        
        int visibility = isChecked ? View.GONE : View.VISIBLE;
        hour.setVisibility(visibility);
        minute.setVisibility(visibility);
        updateDate();
    }
}