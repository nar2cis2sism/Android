package engine.android.widget.common.button;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * 倒计时按钮
 *
 * @author Daimon
 * @since 6/6/2014
 */
public class CountDownButton extends Button {
    
    private CountDownTimer timer;

    public CountDownButton(Context context) {
        super(context);
    }

    public CountDownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        cancelTimer();
        super.onDetachedFromWindow();
    }
    
    private void cancelTimer() {
        if (timer != null) timer.cancel();
    }
    
    /**
     * 开始倒计时（期间不可操作）
     * 
     * @param time 倒计时时间，以秒为单位
     * @param format 文本显示格式化
     */
    public void start(int time, String format) {
        setEnabled(false);
        cancelTimer();
        timer = new SmsCodeTimer(time, format).start();
    }
    
    private class SmsCodeTimer extends CountDownTimer {
        
        private static final int ONE_SECOND_MILLIS = 1000;
        
        private final CharSequence text;
        
        private final String format;

        public SmsCodeTimer(long seconds, String format) {
            super(seconds * ONE_SECOND_MILLIS, ONE_SECOND_MILLIS);
            text = getText();
            this.format = format;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setText(String.format(format, millisUntilFinished / ONE_SECOND_MILLIS));
        }

        @Override
        public void onFinish() {
            setText(text);
            setEnabled(true);
        }
    }
}