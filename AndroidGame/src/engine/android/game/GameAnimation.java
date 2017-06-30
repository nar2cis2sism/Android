package engine.android.game;

import engine.android.game.GameCanvas.GameResource;

/**
 * 游戏动画
 * 
 * @author Daimon
 * @version 3.0
 * @since 6/6/2012
 */

public abstract class GameAnimation extends GameEngine {

    /***** 动画重复次数 *****/

    public static final int INFINITE = -1;                  // 无限重复动画

    /***** 动画重复模式 *****/

    public static final int RESTART = 1;                    // 重头开始
    public static final int REVERSE = 2;                    // 反向开始

    private long startTime;                                 // 动画开始时间
    private long startOffset;                               // 重复动画开始时间间隔

    protected long duration;                                // 动画持续时间
    protected long interval;                                // 动画帧时间差

    private int repeatMode = RESTART;// 动画重复模式
    private int repeatCount;                                // 动画重复次数
    private int repeated;                                   // 动画重复计数

    protected boolean fillEnabled;                          // 是否改变动画状态
    protected boolean fillBefore = true;                    // 是否设置为动画之前状态
    protected boolean fillAfter;                            // 是否设置为动画之后状态

    private GameAnimationListener listener;// 游戏动画监听器
    private GameAnimationCallback callback;// 游戏动画回调接口

    /**
     * 播放动画
     */

    @Override
    public final void start() {
        if (interval == 0 && duration == 0)
        {
            throw new IllegalArgumentException("请设置动画间隔时间");
        }

        long period = getPeriod();
        if (period <= 0)
        {
            throw new RuntimeException("动画循环周期时间错误");
        }

        start(startTime, period);
    }

    /**
     * 添加回调函数
     */

    public GameAnimation addCallback(GameAnimationCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    protected final void onStart() {
        repeated = -1;
        if (listener != null)
        {
            listener.onAnimationStart(this);
        }

        onAnimationBefore();
    }

    @Override
    protected final void onStop() {
        onAnimationAfter();
        if (callback != null)
        {
            callback.doAnimation(this);
        }

        if (listener != null)
        {
            listener.onAnimationEnd(this);
        }
    }

    @Override
    protected final void doEngine() {
        boolean exit = onAnimation();
        if (callback != null)
        {
            callback.doAnimation(this);
        }

        if (exit)
        {
            repeated++;
            // 重复动画处理
            if (repeatCount == 0 || (repeatCount > 0 && repeated >= repeatCount))
            {
                // 动画重复完毕
                stop();
            }
            else
            {
                if (repeated > 0 && listener != null)
                {
                    listener.onAnimationRepeat(this);
                }

                if (startOffset > 0)
                {
                    try {
                        Thread.sleep(startOffset);
                    } catch (InterruptedException e) {}
                }
            }
        }
    }

    /**
     * 需要计算循环周期
     */

    protected abstract long getPeriod();

    /**
     * 动画开始前初始化（状态存储）
     */

    protected abstract void onAnimationBefore();

    /**
     * 动画实现
     * 
     * @return 单轮动画是否执行完毕
     */

    protected abstract boolean onAnimation();

    /**
     * 动画结束后资源回收（状态恢复）
     */

    protected abstract void onAnimationAfter();

    public long getStartTime() {
        return startTime;
    }

    public GameAnimation setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public GameAnimation setStartOffset(long startOffset) {
        this.startOffset = startOffset;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public GameAnimation setDuration(long duration) {
        this.duration = duration;
        if (isRunning())
        {
            start();
        }

        return this;
    }

    public long getInterval() {
        return interval;
    }

    public GameAnimation setInterval(long interval) {
        this.interval = interval;
        if (isRunning())
        {
            start();
        }

        return this;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public GameAnimation setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
        return this;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public GameAnimation setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
        return this;
    }

    public boolean isFillEnabled() {
        return fillEnabled;
    }

    public GameAnimation setFillEnabled(boolean fillEnabled) {
        this.fillEnabled = fillEnabled;
        return this;
    }

    public boolean isFillBefore() {
        return fillBefore;
    }

    public GameAnimation setFillBefore(boolean fillBefore) {
        this.fillBefore = fillBefore;
        return this;
    }

    public boolean isFillAfter() {
        return fillAfter;
    }

    public GameAnimation setFillAfter(boolean fillAfter) {
        this.fillAfter = fillAfter;
        return this;
    }

    public GameAnimationListener getListener() {
        return listener;
    }

    public GameAnimation setListener(GameAnimationListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 获取动画已重复次数
     */

    public int getRepeated() {
        return repeated;
    }

    /**
     * 判断是否反向
     */

    protected final boolean isReverse() {
        return repeatMode == REVERSE && repeated % 2 == 0;
    }

    /**
     * 判断是否超出范围
     */

    protected static final boolean isOutOfRange(float a, float b, float delta) {
        if (delta < 0)
        {
            return a + delta <= b;
        }
        else if (delta > 0)
        {
            return a + delta >= b;
        }
        else
        {
            return a != b;
        }
    }

    /**
     * 动画时间基数
     */
    
    protected static final long getBaseAnimationTime() {
        return GameResource.getGame().getRefreshTime();
    }

    /**
     * 动画回调接口
     */

    public static interface GameAnimationCallback {

        /**
         * 动画处理方法
         */

        public void doAnimation(GameAnimation animation);
    }

    /**
     * 游戏动画监听器
     */

    public static interface GameAnimationListener {

        /**
         * 动画开始之前回调
         */

        public void onAnimationStart(GameAnimation animation);

        /**
         * 动画结束之后回调
         */

        public void onAnimationEnd(GameAnimation animation);

        /**
         * 重复动画时回调
         */

        public void onAnimationRepeat(GameAnimation animation);
    }
}