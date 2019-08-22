package engine.android.util.extra;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同步锁
 * 
 * @author Daimon
 * @since 5/14/2012
 */
public class SyncLock {

    private final AtomicBoolean isLock = new AtomicBoolean();     // 是否锁定

    /**
     * 锁住线程（直到解锁）
     */
    public void lock() {
        lock(0);
    }

    /**
     * 锁定线程
     * 
     * @param time 锁定时间
     */
    public void lock(long time) {
        if (isLock.compareAndSet(false, true))
        {
            try {
                synchronized (this) {
                    if (time <= 0)
                    {
                        wait();
                    }
                    else
                    {
                        wait(time);
                    }
                }
            } catch (InterruptedException e) {
                // Unlocked.
            } finally {
                isLock.set(false);
            }
        }
    }

    /**
     * 解锁
     */
    public void unlock() {
        if (isLock.compareAndSet(true, false))
        {
            synchronized (this) {
                notifyAll();
            }
        }
    }
}