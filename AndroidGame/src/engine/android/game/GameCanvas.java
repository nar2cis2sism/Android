package engine.android.game;

import engine.android.core.extra.SplashScreen;
import engine.android.core.extra.SplashScreen.SplashCallback;
import engine.android.core.extra.SplashScreen.SplashLoading;
import engine.android.util.image.ImageCache;
import engine.android.util.image.ImageUtil;
import engine.android.util.image.ImageUtil.ImageDecoder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏画布
 * 
 * @author Daimon
 * @since 5/15/2012
 */
public abstract class GameCanvas extends SurfaceView implements Callback, OnTouchListener {

    /** 全屏拉伸 **/
    public static final int FULLSCREEN   = 1;
    /** 按比例扩大画布的size，使得画布长(宽)等于或大于View的长(宽) **/
    public static final int CROP         = 2;
    /** 按比例缩小画布的size，使得画布长(宽)等于或小于View的长(宽) **/
    public static final int INSIDE       = 3;

    private static GameCanvas instance;                         // 自身实例
    
    private final GameSettings settings = new GameSettings();   // 游戏设置

    private static final long DEFAULT_REFRESH_TIME = 50;        // 默认刷新时间
    private long refresh_time = DEFAULT_REFRESH_TIME;           // 界面刷新时间

    private int backgroundColor;                                // 背景颜色

    private RenderEngine render;                                // 游戏渲染引擎
    
    private LayerManager buffer;                                // 缓冲界面（输出到画布）
    private LayerManager game;                                  // 游戏界面
    private Splash splash;                                      // 闪屏界面

    boolean isSupportAutoAdapt;                                 // 是否支持屏幕自适配
    boolean isFixScreenSize;                                    // 是否固定屏幕尺寸
    boolean isScreenAutoAdapt;                                  // 屏幕自适配开关
    int width, height;                                          // 屏幕自适配尺寸
    int scaleType;                                              // 画布缩放类型
    float scaleX, scaleY;                                       // 画布缩放比例

    private GestureDetector detector;                           // 手势解析
    private final TouchEvent touchEvent = new TouchEvent();     // 触屏事件

    private int keyState = -1;                                  // 按键状态（-1为无任何按键按下）
    
    private ImageCache<String> cache;                           // 图片缓存

    public GameCanvas(Context context) {
        super(context);
        init();
    }

    public GameCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        instance = this;

        setFocusable(true);
        setKeepScreenOn(true);

        cache = new ImageCache<String>();
        render = new RenderEngine();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (buffer == null)
        {
            sizeChanged(getWidth(), getHeight());
            buffer = game = new LayerManager(this);
            onSplashCreate(splash = new Splash(this));
            if (splash.getSize() == 0)
            {
                // 无闪屏界面
                splash = null;
                onCreate();
            }
            else
            {
                // 显示闪屏界面
                buffer = splash;
            }
        }
    
        pause(false);
    }

    public void surfaceChanged(SurfaceHolder holder, 
            int format, int width, int height) {
        sizeChanged(width, height);
    }

    /**
     * 屏幕尺寸改变（计算自适配属性）
     */
    private void sizeChanged(int width, int height) {
        isScreenAutoAdapt = false;
        if (isSupportAutoAdapt)
        {
            scaleX = width * 1.0f / this.width;
            scaleY = height * 1.0f / this.height;
            isScreenAutoAdapt = !(scaleX == 1 && scaleY == 1);
            if (isScreenAutoAdapt && scaleType != FULLSCREEN)
            {
                scaleX = scaleY = scaleType == CROP 
                        ? Math.max(scaleX, scaleY)
                        : Math.min(scaleX, scaleY);
            }
        }
        else if (!isFixScreenSize)
        {
            this.width = width;
            this.height = height;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        pause(true);
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    /**
     * 返回界面刷新时间
     */
    public final long getRefreshTime() {
        return refresh_time;
    }

    /**
     * 返回默认动画帧时间差
     * 
     * @return 游戏刷新时间的5倍
     */
    public long getAnimationInterval() {
        return refresh_time * 5;
    }

    /**
     * 获取图层管理器（往里添加显示图层）
     */
    public final LayerManager getContentPane() {
        return game;
    }

    /**
     * 加载图片（图片放在assets文件夹下）
     * 
     * @param name 图片名称
     */
    public Bitmap load(String name) {
        Bitmap image = cache.get(name);
        if (image != null)
        {
            return image;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        
        try {
            image = BitmapFactory.decodeStream(getResources().getAssets().open(name), null, opts);
            cache.put(name, image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 加载固定尺寸的图片（图片放在assets文件夹下）
     * 
     * @param name 图片名称
     * @param width,height 图片显示尺寸
     */
    public Bitmap load(String name, int width, int height) {
        Bitmap image = cache.get(name);
        if (image != null)
        {
            Bitmap b = ImageUtil.zoom(image, width, height);
            if (b != image)
            {
                cache.put(name, b);
            }
            
            return b;
        }

        try {
            image = ImageDecoder.decodeStream(getResources().getAssets().open(name), 
                    width, height, true);
            cache.put(name, image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 缓存图片
     * 
     * @param name 图片名称
     * @param image 图片
     */
    public void cache(String name, Bitmap image) {
        cache.put(name, image);
    }

    /**
     * 加载多张图片
     */
    public Bitmap[] loads(String... names) {
        if (names == null || names.length == 0)
        {
            return null;
        }

        Bitmap[] images = new Bitmap[names.length];
        for (int i = 0; i < names.length; i++)
        {
            images[i] = load(names[i]);
        }

        return images;
    }

    /**
     * 回收图片
     * 
     * @param name 图片名称
     */
    public void release(String name) {
        cache.remove(name);
    }

    /**
     * 回收所有图片
     */
    public void releaseAll() {
        cache.clear();
    }

    /**
     * 创建闪屏界面
     */
    protected void onSplashCreate(Splash splash) {};

    /**
     * 销毁闪屏界面
     */
    protected void onSplashDestroy() {};

    /**
     * 创建游戏界面
     */
    protected abstract void onCreate();

    /**
     * @see {@link Activity#onResume()}
     */
    protected void onResume() {}

    /**
     * @see {@link Activity#onPause()}
     */
    protected void onPause() {};

    /**
     * 销毁游戏界面
     */
    protected void onDestroy() {};

    /**
     * 逻辑运算处理（子类可重载）
     */
    protected void onLogic() {};

    /**
     * 游戏退出（需手动调用）
     */
    public final void exit() {
        Splash splash = this.splash;
        if (splash != null)
        {
            splash.splash.cancel();
            this.splash = null;
        }
        
        render.stop();
        onDestroy();
        buffer = game = null;
        releaseAll();
        GameResource.releaseResource();
        instance = null;
    }

    /**
     * 闪屏完毕（显示游戏界面）
     */
    void splashOver() {
        buffer = game;
        splash = null;
        onSplashDestroy();
    }

    /**
     * 是否正显示闪屏界面
     */
    public final boolean isLoading() {
        return buffer != game;
    }

    /**
     * 结束闪屏界面
     */
    public final void finishLoading() {
        Splash splash = this.splash;
        if (splash != null)
        {
            splash.splash.finish();
        }
    }

    /**
     * 注册游戏引擎
     */
    public final void register(GameEngine engine) {
        render.startManagingEngine(engine);
    }

    /**
     * 游戏暂停/恢复
     */
    public final void pause(boolean pause) {
        if (pause)
        {
            render.pause();
            onPause();
        }
        else
        {
            render.start();
            onResume();
        }
    }

    /**
     * 返回按键状态
     */
    public final int getKeyStates() {
        return keyState;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        keyState = keyCode;
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        keyState = -1;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buffer.touchAction(touchEvent.setAction(event.getAction()).setTriggerPosition(event));
        return true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    /**
     * 当前画布拍照
     */
    public Bitmap takePicture() {
        if (buffer == null)
        {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        render.render(c);
        return bitmap;
    }
    
    public GameSettings getSettings() {
        return settings;
    }
    
    public final class GameSettings {
        
        GameSettings() {}

        /**
         * 支持高级触屏事件捕获
         */
        public GameSettings supportAdvancedTouchEvents() {
            detector = new GestureDetector(getContext(), new EventAction());
            setLongClickable(true);
            setOnTouchListener(GameCanvas.this);
            return this;
        }

        /**
         * 支持屏幕自适配
         * 
         * @param autoAdapt 是否根据屏幕尺寸自动拉伸画布
         * @param scaleType 画布缩放类型
         * @param width,height 自适配尺寸
         */
        public GameSettings supportScreenAutoAdapt(boolean autoAdapt, 
                int scaleType, int width, int height) {
            isFixScreenSize = true;
            isSupportAutoAdapt = autoAdapt;
            GameCanvas.this.scaleType = scaleType;
            GameCanvas.this.width = width;
            GameCanvas.this.height = height;
            return this;
        }

        /**
         * 设置帧速率
         * 
         * @param fps 每秒绘制帧数（初始为20）
         */
        public GameSettings setFPS(double fps) {
            render.setPeriod(refresh_time = (long) (1000 / fps));
            return this;
        }

        /**
         * 是否显示帧速率（调试用）
         */
        public GameSettings showFPS(boolean show) {
            render.showFPS(show);
            return this;
        }
    }

    /**
     * 游戏闪屏
     */
    public final class Splash extends LayerManager {
        
        private class Helper implements SplashCallback, SplashLoading {

            @Override
            public void loadInBackground() {
                onCreate();
            }

            @Override
            public void onSplashDisplayed() {
                // Do nothing.
            }

            @Override
            public void onSplashFinished() {
                splashOver();
            }
        }
        
        private final Helper helper = new Helper();
        
        private final SplashScreen splash;

        Splash(GameCanvas gc) {
            super(gc);
            splash = new SplashScreen(helper, helper);
        }

        @Override
        void render(Canvas canvas) {
            splash.start();
            super.render(canvas);
        }

        /**
         * 设置闪屏显示时间
         * 
         * @param duration 如小于0则一直显示
         */
        public void setDuration(long duration) {
            splash.setDuration(duration);
        }
    }

    /**
     * 游戏渲染引擎
     */
    private class RenderEngine extends GameEngine {

        private Canvas canvas;                                      // 绘制画布

        private double FPS;                                         // 帧速率
        private double FPS_default;                                 // 默认帧速率
        private long fps_time;                                      // 纳秒时间（用于计算帧速率）
        private int fps_count;                                      // 帧计数

        private final int thread_num;                               // 启动线程数+渲染引擎

        private Paint fps_paint;                                    // 帧速率绘制画笔
        private final Paint clear_paint;                            // 擦除画布画笔

        private final SurfaceHolder holder;                         // 游戏处理器

        public RenderEngine() {
            thread_num = Thread.activeCount() + 1;

            clear_paint = new Paint();
            clear_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            // 添加回调函数
            (holder = getHolder()).addCallback(GameCanvas.this);
            setPeriod(refresh_time);
        }

        @Override
        public void start() {
            if (isPaused())
            {
                onStart();
            }

            super.start();
        }

        @Override
        protected void onStart() {
            FPS = 0;
            fps_time = System.nanoTime();
            fps_count = 0;
            FPS_default = Math.round(100.0 * 1000 / refresh_time) / 100.0;
        }

        @Override
        protected void doEngine() {
            try {
                // 锁住画布
                if ((canvas = holder.lockCanvas()) != null)
                {
                    onLogic();
                    render(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null)
                {
                    // 刷新绘制
                    holder.unlockCanvasAndPost(canvas);
                    canvas = null;
                }
            }

            if (fps_paint != null && ++fps_count == 20)
            {
                long l = System.nanoTime(); // 纳秒计时
                FPS = Math.round(100000000000.0 * fps_count / (l - fps_time)) / 100.0; // 计算帧速率
                fps_time = l;
                fps_count = 0;
            }
        }

        /**
         * 绘制方法
         * 
         * @param canvas 绘制画布
         */
        private void render(Canvas canvas) {
            canvas.drawPaint(clear_paint);
            canvas.drawColor(backgroundColor);
            buffer.render(canvas);
            if (fps_paint != null)
            {
                canvas.drawText("FPS:" + (FPS == 0 ? "N/A" : FPS + "/" + FPS_default), 
                        5, 23, fps_paint);
                canvas.drawText("process:" + (Thread.activeCount() - thread_num), 
                        5, 40, fps_paint);
            }
        }

        public void showFPS(boolean shown) {
            if (shown)
            {
                fps_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                fps_paint.setTextSize(22);
                fps_paint.setColor(Color.WHITE);
            }
            else
            {
                fps_paint = null;
            }
        }
    }

    /**
     * 触屏事件
     */
    public final class TouchEvent {

        private int action;

        private float downX,downY;

        private float triggerX,triggerY;

        boolean isFlingHorizontal;
        boolean isFlingVertical;

        float flingVelocityX;
        float flingVelocityY;
        
        TouchEvent() {}

        TouchEvent setAction(int action) {
            this.action = action;
            downX = downY = triggerX = triggerY = 0;
            isFlingHorizontal = isFlingVertical = false;
            flingVelocityX = flingVelocityY = 0;
            return this;
        }

        TouchEvent setDownPosition(MotionEvent event) {
            downX = changeX(event.getX());
            downY = changeY(event.getY());
            return this;
        }

        TouchEvent setTriggerPosition(MotionEvent event) {
            triggerX = changeX(event.getX());
            triggerY = changeY(event.getY());
            return this;
        }

        TouchEvent move(int x, int y) {
            downX += x;
            downY += x;
            triggerX += x;
            triggerY += y;
            return this;
        }

        /**
         * 自适配水平坐标转换
         */
        private float changeX(float x) {
            if (isScreenAutoAdapt)
            {
                x /= scaleX;
            }

            return x;
        }

        /**
         * 自适配垂直坐标转换
         */
        private float changeY(float y) {
            if (isScreenAutoAdapt)
            {
                y /= scaleY;
            }

            return y;
        }

        public int getAction() {
            return action;
        }

        public float getDownX() {
            return downX;
        }

        public float getDownY() {
            return downY;
        }

        public float getTriggerX() {
            return triggerX;
        }

        public float getTriggerY() {
            return triggerY;
        }

        public boolean isFlingHorizontal() {
            return isFlingHorizontal;
        }

        public boolean isFlingVertical() {
            return isFlingVertical;
        }

        public float getFlingVelocityX() {
            return flingVelocityX;
        }

        public float getFlingVelocityY() {
            return flingVelocityY;
        }

        public float getMaxVelocity() {
            return ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        }
    }

    /**
     * 高级触屏事件处理<br>
     * Daimon:GestureDetector
     */
    private class EventAction implements OnGestureListener, OnDoubleTapListener, EventConstants {

        private static final float FLING_MIN_DISTANCE = 50.0f;      // 滑动最小距离

        private static final float FLING_MAX_OFF_PATH = 280.0f;     // 滑动最大路径

        private static final float FLING_MIN_VELOCITY = 120.0f;     // 滑动最小速度

        /**
         * 用户轻触触摸屏，由1个MotionEvent.ACTION_DOWN触发
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        /**
         * 用户按下触摸屏，快速移动后松开，由1个MotionEvent.ACTION_DOWN，
         * 多个ACTION_MOVE，1个ACTION_UP触发
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, 
                float velocityX, float velocityY) {
            /*
			 * 参数解释：
			 * velocityX:X轴上的移动速度，像素/秒
			 * velocityY:Y轴上的移动速度，像素/秒
             */
            boolean isFlingHorizontal = false;
            boolean isFlingVertical = false;

            // 触发条件：X轴上的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY
            if (Math.abs(e1.getX() - e2.getX()) > FLING_MIN_DISTANCE
            &&  Math.abs(velocityX) > FLING_MIN_VELOCITY)
            {
                // 水平方向滑动
                if (Math.abs(e1.getY() - e2.getY()) < FLING_MAX_OFF_PATH)
                {
                    // 判断是否超出滑动范围
                    isFlingHorizontal = true;
                }
            }

            // 触发条件：Y轴上的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY
            if (Math.abs(e1.getY() - e2.getY()) > FLING_MIN_DISTANCE
            &&  Math.abs(velocityY) > FLING_MIN_VELOCITY)
            {
                // 垂直方向滑动
                if (Math.abs(e1.getX() - e2.getX()) < FLING_MAX_OFF_PATH)
                {
                    // 判断是否超出滑动范围
                    isFlingVertical = true;
                }
            }

            if (isFlingHorizontal || isFlingVertical)
            {
                touchEvent.setAction(ACTION_FLING);
                touchEvent.isFlingHorizontal = isFlingHorizontal;
                touchEvent.isFlingVertical = isFlingVertical;
                touchEvent.flingVelocityX = velocityX;
                touchEvent.flingVelocityY = velocityY;
                buffer.touchAction(touchEvent.setDownPosition(e1).setTriggerPosition(e2));
            }

            return false;
        }

        /**
         * 用户长按触摸屏，由多个MotionEvent.ACTION_DOWN触发
         */
        @Override
        public void onLongPress(MotionEvent e) {
            buffer.touchAction(touchEvent.setAction(ACTION_LONG_PRESS)
                    .setDownPosition(e).setTriggerPosition(e));
        }

        /**
         * 用户按下触摸屏，并拖动，由1个MotionEvent.ACTION_DOWN，多个ACTION_MOVE触发
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            buffer.touchAction(touchEvent.setAction(ACTION_SCROLL)
                    .setDownPosition(e1).setTriggerPosition(e2));
            return false;
        }

        /**
         * 用户轻触触摸屏，尚未松开或拖动，由1个MotionEvent.ACTION_DOWN触发
         */
        @Override
        public void onShowPress(MotionEvent e) {
            // Ignored.
        }

        /**
         * 用户（轻触触摸屏后）松开，由1个MotionEvent.ACTION_UP触发
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**
         * 用户双击触摸屏（双击的第2下MotionEvent.ACTION_DOWN时触发）
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            touchEvent.setAction(ACTION_DOUBLE_CLICK).setDownPosition(e);
            return false;
        }

        /**
         * 用户双击触摸屏（双击的第2下MotionEvent.ACTION_DOWN，ACTION_MOVE，ACTION_UP时都会触发）
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN)
            {
                buffer.touchAction(touchEvent.setTriggerPosition(e));
            }

            return false;
        }

        /**
         * 用户快速单击触摸屏
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            buffer.touchAction(touchEvent.setAction(ACTION_CLICK)
                    .setDownPosition(e).setTriggerPosition(e));
            return false;
        }
    }

    /**
     * 游戏资源
     */
    public static class GameResource {

        private static final Map<String, Object> map                // 资源查询表
        = new HashMap<String, Object>();

        /**
         * 获取游戏实例
         */
        public static GameCanvas getGame() {
            return instance;
        }

        /**
         * 加载游戏资源
         */
        public static void loadResource(GameResourceLoader loader)  {
            loader.load(map);
        }

        /**
         * 释放游戏资源
         */
        public static void releaseResource() {
            map.clear();
        }

        /**
         * 获取游戏资源
         */
        public static Object getResource(String key) {
            return map.get(key);
        }

        /**
         * 获取游戏文本
         */
        public static String getText(String key) {
            Object obj = map.get(key);
            if (obj instanceof String)
            {
                return (String) obj;
            }

            return null;
        }

        /**
         * 游戏资源加载器
         */
        public interface GameResourceLoader {

            void load(Map<String, Object> map);
        }
    }
}

/**
 * 触屏事件常量
 */
interface EventConstants {

    int ACTION_DOWN             = MotionEvent.ACTION_DOWN;
    int ACTION_UP               = MotionEvent.ACTION_UP;
    int ACTION_MOVE             = MotionEvent.ACTION_MOVE;

    /***** 高级事件 *****/
    int ACTION_SCROLL           = -1;            // 滚动事件
    int ACTION_CLICK            = -2;            // 单击事件
    int ACTION_DOUBLE_CLICK     = -3;            // 双击事件
    int ACTION_LONG_PRESS       = -4;            // 长按事件
    int ACTION_FLING            = -5;            // 滑动事件
}