package engine.android.util.ui;

import android.view.View;

/**
 * 快速点击事件处理工具<p>
 * 只能在主线程使用
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public final class FastClickUtil {
    
    public static long threshold = 1000;                // 在此时间内不响应
    
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
    
    /**
     * 快速点击事件计数器
     */
    public static class FastClickCounter {
        
        private long threshold = 300;       // 在此时间内才计数
        
        private long countAfterTime = 1500; // 计数完成1.5秒后才能重新开始计数
        
        private int count;                  // 已点击次数
        
        private long lastTime;              // 上次计数时间
        
        private final int maxCount;         // 计数多少次执行操作
        
        /**
         * @param count 计数值
         */
        public FastClickCounter(int count) {
            maxCount = count;
        }
        
        /**
         * 设置点击时间阈值
         */
        public void setThreshold(long threshold) {
            this.threshold = threshold;
        }
        
        public void setCountAfterTime(long countAfterTime) {
            this.countAfterTime = countAfterTime;
        }
        
        /**
         * 计数
         * 
         * @return True:计数完成，可以执行操作
         */
        public boolean count() {
            long time = System.currentTimeMillis();
            if (lastTime == 0)
            {
                lastTime = time;
            }
            else if (lastTime > time)
            {
                return false;
            }
            else
            {
                if (time - lastTime > threshold)
                {
                    count = 0;
                }
                
                lastTime = time;
            }
            
            if (++count >= maxCount)
            {
                count = 0;
                lastTime += countAfterTime;
                return true;
            }
            
            return false;
        }
    }
}