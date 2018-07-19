package com.project.ui.util;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.project.R;

public class CountDownLabel extends CountDownTimer {
    
    private static final int ONE_SECOND_MILLIS = 1000;
    private static final int ONE_MINUTE_MILLIS = 60 * ONE_SECOND_MILLIS;
    
    private final Context context;
    private final TextView label;
    
    private CharSequence text;

    public CountDownLabel(int minutes, TextView label) {
        super(minutes * ONE_MINUTE_MILLIS, ONE_SECOND_MILLIS);
        this.context = (this.label = label).getContext();
    }
    
    /**
     * 启动/停止开关
     */
    
    public void start(boolean start) {
        if (start)
        {
            text = label.getText();
            label.setEnabled(false);
            start();
        }
        else
        {
            cancel();
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        label.setText(context.getResources().getString(
                R.string.count_down_label, millisUntilFinished / ONE_SECOND_MILLIS));
    }

    @Override
    public void onFinish() {
        label.setText(text);
        label.setEnabled(true);
    }
}