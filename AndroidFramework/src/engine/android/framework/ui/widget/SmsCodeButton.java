package engine.android.framework.ui.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.Button;

import engine.android.framework.R;

/**
 * “获取验证码”的按钮
 *
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class SmsCodeButton extends Button {
    
    private SmsCodeTimer timer;

    public SmsCodeButton(Context context) {
        super(context);
        init(context);
    }

    public SmsCodeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        setBackgroundResource(R.drawable.btn_yellow);
        setText(R.string.get_sms_code);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        cancelTimer();
        super.onDetachedFromWindow();
    }
    
    private void cancelTimer() {
        if (timer != null) timer.cancel();
    }
    
    public void n秒后可重新获取验证码(int n) {
        setEnabled(false);
        cancelTimer();
        timer = new SmsCodeTimer(n);
        timer.start();
    }
    
    private class SmsCodeTimer extends CountDownTimer {
        
        private static final int ONE_SECOND_MILLIS = 1000;

        private CharSequence text;

        public SmsCodeTimer(long seconds) {
            super(seconds * ONE_SECOND_MILLIS, ONE_SECOND_MILLIS);
            text = getText();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setText(getResources().getString(R.string.get_sms_code_timer, millisUntilFinished / ONE_SECOND_MILLIS));
        }

        @Override
        public void onFinish() {
            setText(text);
            setEnabled(true);
        }
    }
}