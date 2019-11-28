package engine.android.game;

import engine.android.util.extra.SyncLock;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 游戏引擎
 * 
 * @author Daimon
 * @since 5/14/2012
 */
public abstract class GameEngine {
    
    private static final int STATUS_STOP    = 0;
    private static final int STATUS_START   = 1;
    private static final int STATUS_PAUSE   = 2;

    private final SyncLock lock = new SyncLock();                   // 线程锁
    
    private GameDriver driver;                                      // 驱动线程
    
    private volatile long period;                                   // 线程运行周期
    
    private volatile int status;                                    // 线程运行状态

    private final CopyOnWriteArrayList<GameEngine> chirdren         // 内置引擎
    = new CopyOnWriteArrayList<GameEngine>();
    
    /**
     * 设置引擎循环周期
     * 
     * @param period <=0表示一次性使用
     */
    public void setPeriod(long period) {
        this.period = period;
    }
    
    /**
     * 引擎是否正在运行
     */
    public boolean isRunning() {
        return status == STATUS_START;
    }
    
    /**
     * 引擎是否暂停
     */
    public boolean isPaused() {
        return status == STATUS_PAUSE;
    }
    
    private boolean isStopped() {
        return status == STATUS_STOP;
    }
    
    private void changeStatus(int status) {
        this.status = status;
    }
    
    /**
     * 启动引擎
     */
    public void start() {
        if (isStopped())
        {
            changeStatus(STATUS_START);
            (driver = new GameDriver()).start();
        }
        else if (isPaused())
        {
            changeStatus(STATUS_START);
            lock.unlock();
        }
        
        for (GameEngine engine : chirdren)
        {
            engine.start();
        }
    }
    
    /**
     * 暂停引擎
     */
    public void pause() {
        if (isRunning())
        {
            changeStatus(STATUS_PAUSE);
            driver.interrupt();
        }
        
        for (GameEngine engine : chirdren)
        {
            engine.pause();
        }
    }
    
    /**
     * 关闭引擎
     */
    public void stop() {
        if (isRunning())
        {
            changeStatus(STATUS_STOP);
            driver.interrupt();
        }
        else if (isPaused())
        {
            changeStatus(STATUS_STOP);
            lock.unlock();
        }
        
        for (GameEngine engine : chirdren)
        {
            engine.stop();
        }
    }
    
    /**
     * 管理关联引擎
     */
    public void startManagingEngine(GameEngine engine) {
        if (chirdren.addIfAbsent(engine) && isRunning())
        {
            engine.start();
        }
    }
    
    /**
     * 清空关联引擎
     */
    public void clearManagingEngine() {
        chirdren.clear();
    }

    /**
     * 驱动线程
     */
    private class GameDriver extends Thread {
        
        @Override
        public void run() {
            onStart();
            if (period <= 0)
            {
                // 只执行一次
                if (isPaused())
                {
                    lock.lock();
                }
                
                if (isRunning())
                {
                    doEngine();
                }
            }
            else
            {
                long time, delay = 0;
                while (!isStopped())
                {
                    if (isPaused())
                    {
                        lock.lock();
                    }
                    else
                    {
                        if (delay > 0)
                        {
                            time = System.currentTimeMillis();
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                delay -= System.currentTimeMillis() - time;
                                continue;
                            }
                        }
                        
                        time = System.currentTimeMillis();
                        doEngine();
                        time = System.currentTimeMillis() - time;
                        delay = period - time;
                    }
                }
            }

            changeStatus(STATUS_STOP);
            onStop();
        }
    }

    /**
     * 引擎启动（由系统调用）
     */
    protected void onStart() {}

    /**
     * 引擎实现（由子类实现）
     */
    protected abstract void doEngine();

    /**
     * 引擎关闭（由系统调用）
     */
    protected void onStop() {}

    /**
     * 游戏事件处理器
     */
    public static class GameHandler {

        private final Handler handler;                          // 事件处理器

        private final Map<String, Runnable> map;                // 接口查询表<接口名称,接口实例>

        public GameHandler() {
            handler = new Handler();
            map = new HashMap<String, Runnable>();
        }

        /**
         * 注册接口
         * 
         * @param name 接口名称
         * @param r 接口实例
         */
        public void register(String name, Runnable r) {
            unregister(name);
            map.put(name, r);
        }

        /**
         * 注销接口
         * 
         * @param name 接口名称
         */
        public void unregister(String name) {
            if (map.containsKey(name))
            {
                handler.removeCallbacks(map.remove(name));
            }
        }

        /**
         * 注销所有接口
         */
        public void unregisterAll() {
            handler.removeCallbacksAndMessages(null);
            map.clear();
        }

        /**
         * 获取接口
         * 
         * @param name 接口名称
         * @return 如没注册返回Null
         */
        public Runnable getRunnable(String name) {
            return map.get(name);
        }

        /**
         * @see Handler#post(Runnable)
         * @param name 接口名称
         */
        public final void post(String name) {
            if (map.containsKey(name))
            {
                handler.post(map.get(name));
            }
        }

        /**
         * @see Handler#postDelayed(Runnable, long)
         * @param name 接口名称
         * @param delayMillis
         */
        public final void postDelayed(String name, long delayMillis) {
            if (map.containsKey(name))
            {
                handler.postDelayed(map.get(name), delayMillis);
            }
        }

        /**
         * @see Handler#removeCallbacks(Runnable)
         * @param name 接口名称
         */
        public final void removeCallbacks(String name) {
            if (map.containsKey(name))
            {
                handler.removeCallbacks(map.get(name));
            }
        }

        public final Handler getHandler() {
            return handler;
        }
    }
}