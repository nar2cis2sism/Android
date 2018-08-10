package engine.android.framework.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;

import engine.android.core.util.CalendarFormat;
import engine.android.framework.R;
import engine.android.widget.common.wheel.NumericWheelAdapter;
import engine.android.widget.common.wheel.OnWheelChangedListener;
import engine.android.widget.common.wheel.WheelView;

import java.util.Calendar;

/**
 * 日期选择对话框
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class DatePickerDialog extends AlertDialog implements OnWheelChangedListener, OnClickListener {
    
    private static final int MIN_YEAR = 1901;
    private static final int MAX_YEAR = 2070;
    
    private final WheelView year;
    private final WheelView month;
    private final WheelView day;

    private final OnDateSetListener listener;
    private final Calendar cal;
    
    private int date_year;
    private int date_month;
    private int date_day;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        void onDateSet(Calendar date);
    }
    
    public DatePickerDialog(Context context, OnDateSetListener listener) {
        this(context, listener, null);
    }

    /**
     * @param date 初始显示日期
     */
    public DatePickerDialog(Context context, OnDateSetListener listener, Calendar date) {
        super(context);
        this.listener = listener;

        setTitle(context.getString(R.string.date_picker_title));
        
        View view = LayoutInflater.from(context).inflate(R.layout.date_picker_dialog, null);
        year = (WheelView) view.findViewById(R.id.year);
        month = (WheelView) view.findViewById(R.id.month);
        day = (WheelView) view.findViewById(R.id.day);
        setView(view);
        
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), (OnClickListener) null);
        
        // Initialize the date. If the provided values designate an inconsistent
        // date the values are normalized before updating the spinners.
        if (date == null) date = Calendar.getInstance();
        CalendarFormat.formatAllDay(cal = date);
        date_year = date.get(Calendar.YEAR);
        date_month = date.get(Calendar.MONTH);
        date_day = date.get(Calendar.DATE);
        
        initYear();
        initMonth();
        initDay();
    }
    
    private void initYear() {
        year.setInterpolator(new AnticipateOvershootInterpolator());
        year.setAdapter(new NumericWheelAdapter(MIN_YEAR, MAX_YEAR));
        year.setCyclic(true);
        year.setCurrentItem(date_year - MIN_YEAR);
        year.addChangingListener(this);
    }
    
    private void initMonth() {
        month.setInterpolator(new AnticipateOvershootInterpolator());
        month.setAdapter(new NumericWheelAdapter(1, 12, "%02d月"));
        month.setCyclic(true);
        month.setCurrentItem(date_month);
        month.addChangingListener(this);
    }
    
    private void initDay() {
        day.setInterpolator(new AnticipateOvershootInterpolator());
        day.setCyclic(true);
        day.addChangingListener(this);
        updateDay();
    }
    
    private void updateDay() {
        int minimumDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        day.setAdapter(new NumericWheelAdapter(minimumDay, maximumDay, "%02d"));
        if (date_day > maximumDay) date_day = minimumDay;
        day.setCurrentItem(date_day - 1);
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == year)
        {
            date_year = MIN_YEAR + newValue;
            updateDate();
        }
        else if (wheel == month)
        {
            date_month = newValue;
            updateDate();
        }
        else if (wheel == day)
        {
            cal.set(Calendar.DAY_OF_MONTH, date_day = newValue + 1);
        }
    }
    
    private void updateDate() {
        cal.set(Calendar.YEAR, date_year);
        cal.set(Calendar.MONTH, date_month);
        cal.set(Calendar.DATE, 1);
        updateDay();
        cal.set(Calendar.DATE, date_day);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (listener != null)
        {
            listener.onDateSet(cal);
        }
    }
}