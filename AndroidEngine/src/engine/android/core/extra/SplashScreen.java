package engine.android.core.extra;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 闪屏机制
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class SplashScreen {

    public interface SplashCallback {

        void onSplashDisplayed();

        void onSplashFinished();
    }

    public interface SplashLoading {

        void loadInBackground();
    }
    
    private class SplashTask extends AsyncTask<Void, Void, Void> implements Runnable {
        
        @Override
        protected void onPreExecute() {
            callback.onSplashDisplayed();
            if (showTime == 0)
            {
                finish();
            }
            else if (showTime > 0)
            {
                handler.postDelayed(this, showTime);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            loading.loadInBackground();
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            loadingOver.set(true);
            if (splashFinished.get()) over();
        }

        @Override
        public void run() {
            finish();
        }
    }

    private final SplashCallback callback;                          // 闪屏回调

    private final SplashLoading loading;                            // 闪屏加载
    
    private final Handler handler = new Handler();                  // 计时器
    
    private final SplashTask task = new SplashTask();

    private long showTime;                                          // 闪屏显示时间

    private AtomicBoolean splashFinished = new AtomicBoolean();     // 闪屏是否结束
    
    private AtomicBoolean loadingOver = new AtomicBoolean();        // 是否加载完毕

    public SplashScreen(SplashCallback callback, SplashLoading loading) {
        this.callback = callback;
        this.loading = loading;
    }
    
    /**
     * 启动闪屏（界面展示同时后台初始化资源）
     */
    public synchronized void start() {
        if (task.getStatus() == Status.PENDING)
        {
            task.execute();
        }
    }

    /**
     * 结束闪屏界面
     */
    public void finish() {
        if (splashFinished.compareAndSet(false, true))
        {
            handler.removeCallbacksAndMessages(null);
            over();
        }
    }

    /**
     * 取消闪屏初始化并退出
     */
    public void cancel() {
        task.cancel(true);
        finish();
    }

    /**
     * 设置闪屏显示时间
     * 
     * @param duration 如小于0则一直显示
     */
    public void setDuration(long duration) {
        showTime = duration;
    }
    
    void over() {
        if (loadingOver.getAndSet(false) && !task.isCancelled())
        {
            callback.onSplashFinished();
        }
    }
}