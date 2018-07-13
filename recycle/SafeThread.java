package engine.android.util;

/**
 * Single safe thread to do one thing<br>
 * Control running state of thread and avoid concurrent mistake
 * 
 * @author Daimon
 * @version 4.0
 * @since 11/14/2012
 */

public final class SafeThread {

    private SafeThreadImp thread;

    public synchronized void startThread(SafeRunnable r) {
        if (thread != null)
        {
            if (thread.sr.isRunning && thread.isAlive())
            {
                return;
            }
        }

        thread = new SafeThreadImp(r);
        thread.sr.setRunning(true);
        thread.start();
    }

    public synchronized void stopThread() {
        if (thread != null)
        {
            thread.sr.setRunning(false);
            thread = null;
        }
    }

    public synchronized void forceStartThread(SafeRunnable r) {
        if (thread != null)
        {
            thread.sr.setRunning(false);
            thread.interrupt();
        }

        thread = new SafeThreadImp(r);
        thread.sr.setRunning(true);
        thread.start();
    }

    public synchronized void forceStopThread() {
        if (thread != null)
        {
            thread.sr.setRunning(false);
            thread.interrupt();
            thread = null;
        }
    }

    public boolean isAlive() {
        return thread.isAlive();
    }

    private static final class SafeThreadImp extends Thread {

        SafeRunnable sr;

        public SafeThreadImp(SafeRunnable sr) {
            super(sr);
            this.sr = sr;
        }
    }

    public static abstract class SafeRunnable implements Runnable {

        boolean isRunning;

        void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        protected final boolean isRunning() {
            return isRunning;
        }

        /**
         * E.G.
        
        @Override
        public void run() {
            try {
                while (isRunning())
                {
                    ...
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
         */
    }
}