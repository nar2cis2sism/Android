package engine.android.game;

import android.os.Handler;

import engine.android.util.SyncLock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 游戏引擎
 * 
 * @author Daimon
 * @version 3.0
 * @since 5/14/2012
 */

public abstract class GameEngine {

    private final ReentrantLock opt = new ReentrantLock();  // 操作锁

    private GameDriver driver;                              // 驱动线程

    final SyncLock lock = new SyncLock();                   // 线程锁

    volatile boolean isRunning;                             // 线程是否运行

    volatile boolean isPause;                               // 线程是否暂停

    volatile boolean isStop = true;                         // 线程是否执行完毕

    private List<GameEngine> list;                          // 内置引擎

    /**
     * 驱动线程
     */

    private class GameDriver extends Thread {

        private final long delay;                           // 启动延迟时间

        private volatile long period;                       // 运行间隔时间

        public GameDriver(long delay, long period) {
            this.delay = delay;
            this.period = period;
        }

        /**
         * 重设运行间隔时间
         */

        public void resetPeriod(long period) {
            this.period = period;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            long delay = this.delay;
            while (delay > 0)
            {
                try {
                    Thread.sleep(delay);
                    delay = 0;
                } catch (InterruptedException e) {
                    if (!isRunning)
                    {
                        break;
                    }
                    else if (isPause)
                    {
                        long l = System.currentTimeMillis();
                        delay -= l - time;
                        time = l;
                        lock.lock();
                    }
                }
            }

            if (isRunning)
            {
                onStart();
                if (period <= 0)
                {
                    if (isPause)
                    {
                        lock.lock();
                    }

                    if (isRunning)
                    {
                        // 只执行一次
                        doEngine();
                    }
                }
                else
                {
                    while (isRunning)
                    {
                        if (isPause)
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
                                    delay = 0;
                                } catch (InterruptedException e) {
                                    long l = System.currentTimeMillis();
                                    delay -= l - time;
                                    time = l;
                                    continue;
                                }
                            }

                            long period = this.period;
                            time = System.currentTimeMillis();
                            doEngine();
                            time = (delay = System.currentTimeMillis()) - time;
                            if (time < period)
                            {
                                try {
                                    Thread.sleep(period - time);
                                } catch (InterruptedException e) {
                                    delay = period - time - (System.currentTimeMillis() - delay);
                                    continue;
                                }
                            }

                            delay = 0;
                        }
                    }
                }

                isRunning = false;
                isStop = true;
                onStop();
            }
            else
            {
                isStop = true;
            }
        }
    }

    /**
     * 管理引擎
     */

    public final void startManagingGameEngine(GameEngine engine) {
        if (engine == null)
        {
            throw new NullPointerException();
        }

        opt.lock();
        try {
            if (list == null)
            {
                list = new ArrayList<GameEngine>();
                list.add(engine);
            }
            else if (!list.contains(engine))
            {
                list.add(engine);
            }

            if (isRunning)
            {
                engine.start();
                if (isPause)
                {
                    engine.pause();
                }
            }
        } finally {
            opt.unlock();
        }
    }

    /**
     * 立即启动引擎（默认引擎启动入口，子类可重载）
     */

    public void start() {
        start(0);
    }

    /**
     * 启动引擎
     * 
     * @param delay 延迟时间
     */

    protected final void start(long delay) {
        start(delay, 0);
    }

    /**
     * 启动引擎
     * 
     * @param delay 延迟时间
     * @param period 循环周期
     */

    protected final void start(long delay, long period) {
        opt.lock();
        try {
            if (isRunning)
            {
                driver.resetPeriod(period);
                if (isPause)
                {
                    isPause = false;
                    lock.unlock();
                }
            }
            else if (isStop)
            {
                isRunning = true;
                isStop = false;
                (driver = new GameDriver(delay, period)).start();
            }
            else
            {
                return;
            }

            if (list != null && !list.isEmpty())
            {
                for (GameEngine engine : list)
                {
                    engine.start();
                }
            }
        } finally {
            opt.unlock();
        }
    }

    /**
     * 暂停引擎（线程还在运行中）
     */

    public final void pause() {
        opt.lock();
        try {
            if (isRunning && !isPause)
            {
                isPause = true;
                driver.interrupt();

                if (list != null && !list.isEmpty())
                {
                    for (GameEngine engine : list)
                    {
                        engine.pause();
                    }
                }
            }
        } finally {
            opt.unlock();
        }
    }

    /**
     * 关闭引擎（停止线程）
     */

    public final void stop() {
        opt.lock();
        try {
            if (isRunning)
            {
                isRunning = false;
                if (isPause)
                {
                    isPause = false;
                    lock.unlock();
                }
                else
                {
                    driver.interrupt();
                }

                driver = null;

                if (list != null && !list.isEmpty())
                {
                    for (GameEngine engine : list)
                    {
                        engine.stop();
                    }

                    list.clear();
                    list = null;
                }
            }
        } finally {
            opt.unlock();
        }
    }

    /**
     * 引擎是否正在运行
     */

    public final boolean isRunning() {
        return isRunning || !isStop;
    }

    /**
     * 引擎是否暂停
     */

    public final boolean isPause() {
        return isRunning && isPause;
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

        public void unregister() {
            if (!map.isEmpty())
            {
                for (Runnable r : map.values())
                {
                    handler.removeCallbacks(r);
                }

                map.clear();
            }
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
         * @return
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

    /**
     * 游戏动画管理器
     */

    public static class AnimationManager {

        private final Map<String, GameAnimation> map;           // 动画查询表<动画名称,动画实例>

        public AnimationManager() {
            map = new HashMap<String, GameAnimation>();
        }

        /**
         * 注册动画
         * 
         * @param name 动画名称
         * @param anim 动画实例
         */

        public void register(String name, GameAnimation anim) {
            unregister(name);
            map.put(name, anim);
        }

        /**
         * 注销动画
         * 
         * @param name 动画名称
         */

        public void unregister(String name) {
            if (map.containsKey(name))
            {
                map.remove(name).stop();
            }
        }

        /**
         * 注销所有动画
         */

        public void unregister() {
            if (!map.isEmpty())
            {
                for (GameAnimation anim : map.values())
                {
                    anim.stop();
                }

                map.clear();
            }
        }

        /**
         * 获取动画
         * 
         * @param name 动画名称
         * @return 如没注册返回Null
         */

        public GameAnimation getAnimation(String name)  {
            return map.get(name);
        }

        /**
         * 开启动画
         * 
         * @param name 动画名称
         */

        public void startAnimation(String name) {
            if (map.containsKey(name))
            {
                map.get(name).start();
            }
        }

        /**
         * 暂停动画
         * 
         * @param name 动画名称
         */

        public void pauseAnimation(String name) {
            if (map.containsKey(name))
            {
                map.get(name).pause();
            }
        }

        /**
         * 停止动画
         * 
         * @param name 动画名称
         */

        public void stopAnimation(String name) {
            if (map.containsKey(name))
            {
                map.get(name).stop();
            }
        }
    }
}