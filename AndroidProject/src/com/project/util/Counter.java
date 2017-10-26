package com.project.util;

/**
 * 计数器（不支持多线程）
 * 
 * @author Daimon
 */

public class Counter {
    
    private long duration = 700;        // 在此时间内才计数
    
    private int count;                  // 次数
    
    private long lastTime;              // 上次计数时间
    
    private final int maxCount;         // 计数多少次执行操作
    
    public Counter(int count) {
        maxCount = count;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    /**
     * 计数
     * @return 是否计数完成
     */
    
    public boolean count() {
        if (lastTime == 0)
        {
            lastTime = System.currentTimeMillis();
            count++;
        }
        else
        {
            long time = System.currentTimeMillis();
            if (time - lastTime > duration)
            {
                count = 0;
            }
            else
            {
                count++;
            }
            
            lastTime = time;
        }
        
        return count >= maxCount;
    }
    
    public void reset() {
        count = 0;
        lastTime = 0;
    }
}