package engine.android.util.extra;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single safe thread to do one thing<br>
 * Control running state of thread and avoid concurrent mistake
 * 
 * @author Daimon
 * @since 11/14/2012
 */
public final class SafeThread {

    private SafeThreadImp thread;

    public synchronized void startThread(SafeRunnable r) {
        if (thread != null && thread.isRunning())
        {
            return;
        }

        (thread = new SafeThreadImp(r)).start();
    }

    public synchronized void stopThread() {
        if (thread != null)
        {
            thread.isRunning.set(false);
            thread = null;
        }
    }

    public synchronized void forceStartThread(SafeRunnable r) {
        if (thread != null)
        {
            thread.isRunning.set(false);
            thread.interrupt();
        }

        (thread = new SafeThreadImp(r)).start();
    }

    public synchronized void forceStopThread() {
        if (thread != null)
        {
            thread.isRunning.set(false);
            thread.interrupt();
            thread = null;
        }
    }

    private static class SafeThreadImp extends Thread {

        private final SafeRunnable runnable;
        
        public final AtomicBoolean isRunning = new AtomicBoolean(true);
        
        public SafeThreadImp(SafeRunnable runnable) {
            this.runnable = runnable;
        }
        
        @Override
        public void run() {
            runnable.run(isRunning);
        }
        
        public boolean isRunning() {
            return isRunning.get() && isAlive();
        }
    }

    public static abstract class SafeRunnable {
        
        public void run(AtomicBoolean isRunning) {
            try {
                while (isRunning.get())
                {
                    // TODO Do not block.
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}