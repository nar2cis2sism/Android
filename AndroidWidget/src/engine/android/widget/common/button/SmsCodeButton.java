package engine.android.widget.common.button;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * 验证码按钮
 *
 * @author Daimon
 * @since 6/6/2014
 */
public class SmsCodeButton extends Button {
    
    private SmsCodeTimer timer;

    public SmsCodeButton(Context context) {
        super(context);
    }

    public SmsCodeButton(Context context, AttributeSet attrs) {
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
    
    public void n秒后可重新获取验证码(int n) {
        n秒后可重新获取验证码(n, "已发送(%ds)");
    }
    
    /**
     * @param format 文本显示格式化
     */
    public void n秒后可重新获取验证码(int n, String format) {
        setEnabled(false);
        cancelTimer();
        timer = new SmsCodeTimer(n);
        timer.format = format;
        timer.start();
    }
    
    private class SmsCodeTimer extends CountDownTimer {
        
        private static final int ONE_SECOND_MILLIS = 1000;
        
        private final CharSequence text;
        
        public String format;

        public SmsCodeTimer(long seconds) {
            super(seconds * ONE_SECOND_MILLIS, ONE_SECOND_MILLIS);
            text = getText();
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