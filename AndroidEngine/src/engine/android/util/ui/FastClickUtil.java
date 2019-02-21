package engine.android.util.ui;

import android.view.View;

/**
 * 快速点击事件处理工具<p>
 * 只能在主线程使用
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class FastClickUtil {
    
    public static long threshold = 800;       // 在此时间内不响应
    
    private static long lastClickTime;
    
    public static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime > threshold)
        {
            lastClickTime = time;
            return false;
        }
        else
        {
            return true;
        }
    }
    
    public static void click(final View view) {
        view.setClickable(false);
        view.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                view.setClickable(true);
            }
        }, threshold);
    }
}