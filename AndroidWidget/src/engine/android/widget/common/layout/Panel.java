package engine.android.widget.common.layout;

import static engine.android.util.api.MathUtil.ensureRange;

import engine.android.widget.R;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;

/**
 * 抽屉<p>
 * 替代{@link SlidingDrawer}只支持BOTTOM和RIGHT两个开关位置
 * 
 * @author Daimon
 * @since 8/1/2013
 */
@SuppressWarnings("deprecation")
public class Panel extends LinearLayout {

    /******************** 开关摆放位置 ********************/

    private static final int TOP = 0;

    private static final int BOTTOM = 1;

    private static final int LEFT = 2;

    private static final int RIGHT = 3;

    /******************** 开关状态 ********************/

    private static final int PREPARE = 0;

    private static final int SCROLL = 1;

    private static final int FLING = 2;

    private static final int ANIMATE = 3;

    private static final int FINISH = 4;

    private int position;                               // 开关摆放位置
    private Drawable handle_open;                       // 开关展开图片
    private Drawable handle_close;                      // 开关闭合图片
    private boolean animationEnable;                    // 开合是否有动画效果
    
    private final int handleId;
    private final int contentId;
    
    private int animationDuration;                      // 动画演变时长
    private Interpolator interpolator;                  // 动画插入器
    
    private int orientation;                            // 布局方向

    private GestureDetector detector;                   // 手势解析
    private PanelOnGestureListener gestureListener;     // 手势监听器
    
    private View handler;                               // 开关视图
    private View content;                               // 抽屉视图
    
    private int contentWidth, contentHeight;            // 抽屉大小
    private boolean isContentExpand;                    // 抽屉是否展开

    private int state = FINISH;                         // 开关状态

    private float translateX, translateY;               // 画布偏移量
    private float velocity;                             // 滑动加速度

    private OnPanelListener panelListener;              // 抽屉监听器

    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Panel);
        position = a.getInt(R.styleable.Panel_Panel_position, BOTTOM);
        handle_open = a.getDrawable(R.styleable.Panel_Panel_open);
        handle_close = a.getDrawable(R.styleable.Panel_Panel_close);
        animationEnable = a.getBoolean(R.styleable.Panel_Panel_animation, false);
        
        handleId = a.getResourceId(R.styleable.Panel_android_handle, 0);
        contentId = a.getResourceId(R.styleable.Panel_android_content, 0);
        animationDuration = a.getInteger(R.styleable.Panel_android_animationDuration, 750);
        int interpolatorId = a.getResourceId(R.styleable.Panel_android_interpolator, 0);
        if (interpolatorId > 0)
        {
            try {
                interpolator = AnimationUtils.loadInterpolator(context, interpolatorId);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        a.recycle();
        // 根据摆放位置决定水平或垂直布局
        setOrientation(orientation = (position == LEFT || position == RIGHT) ? HORIZONTAL : VERTICAL);

        detector = new GestureDetector(context, gestureListener = new PanelOnGestureListener());
        detector.setIsLongpressEnabled(false);
    }

    /**
     * 视图解析完成时调用，用于得到开关和抽屉
     */
    @Override
    protected void onFinishInflate() {
        handler = findViewById(handleId);
        if (handler == null)
        {
            throw new RuntimeException("缺少开关");
        }

        handler.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setState(!isContentExpand, animationEnable);
            }
        });
        handler.setOnTouchListener(new OnTouchListener() {

            private int initX, initY;

            private boolean init;
            
            private boolean lock;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (state == FINISH)
                        {
                            lock = false;
                            v.setPressed(true);
                        }
                        else
                        {
                            lock = true;
                            return true;
                        }
                        
                        initX = initY = 0;
                        if (!isContentExpand)
                        {
                            if (orientation == VERTICAL)
                            {
                                initY = position == TOP ? -1 : 1;
                            }
                            else
                            {
                                initX = position == LEFT ? -1 : 1;
                            }
                        }

                        init = true;
                        break;
                    default:
                        if (lock)
                        {
                            return true;
                        }
                        
                        if (init)
                        {
                            initX *= contentWidth;
                            initY *= contentHeight;

                            gestureListener.init(initX, initY);
                            init = false;

                            initX = -initX;
                            initY = -initY;
                        }

                        event.offsetLocation(initX, initY);
                        break;
                }

                if (!detector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP)
                {
                    handler.setPressed(false);
                    if (animationEnable)
                    {
                        post(panelAnimation);
                    }
                    else
                    {
                        state = FINISH;
                        content.setVisibility(isContentExpand ? VISIBLE : GONE);
                        update();
                        invalidate();
                    }
                }

                return true;
            }
        });

        content = findViewById(contentId);
        if (content == null)
        {
            throw new RuntimeException("缺少抽屉");
        }
        // 先移除开关和抽屉，然后根据摆放位置决定二者的添加次序
        removeView(handler);
        removeView(content);
        if (position == TOP || position == LEFT)
        {
            addView(content);
            addView(handler);
        }
        else
        {
            addView(handler);
            addView(content);
        }

        if (handle_close != null)
        {
            handler.setBackgroundDrawable(handle_close);
        }
        // 隐藏抽屉
        content.setVisibility(GONE);
    }

    /**
     * 布局时（计算视图区域）调用，用于得到抽屉的大小
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        contentWidth = content.getWidth();
        contentHeight = content.getHeight();
    }

    /**
     * 调整抽屉位置
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (state == PREPARE)
        {
            if (isContentExpand)
            {
                int delta = orientation == VERTICAL ? contentHeight : contentWidth;
                if (position == LEFT || position == TOP)
                {
                    delta = -delta;
                }

                if (orientation == VERTICAL)
                {
                    canvas.translate(0, delta);
                }
                else
                {
                    canvas.translate(delta, 0);
                }
            }
        }
        else if (state == SCROLL || state == FLING)
        {
            canvas.translate(translateX, translateY);
        }

        super.dispatchDraw(canvas);
    }

    /**
     * 更新状态
     */
    private void update() {
        // 更新开关背景图
        if (isContentExpand && handle_open != null)
        {
            handler.setBackgroundDrawable(handle_open);
        }
        else if (!isContentExpand && handle_close != null)
        {
            handler.setBackgroundDrawable(handle_close);
        }
        // 监听器回调
        if (panelListener != null)
        {
            if (isContentExpand)
            {
                panelListener.onPanelOpened(this);
            }
            else
            {
                panelListener.onPanelClosed(this);
            }
        }
    }

    /**
     * 设置抽屉监听器
     */
    public void setOnPanelListener(OnPanelListener panelListener) {
        this.panelListener = panelListener;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    /**
     * 设置抽屉状态
     * 
     * @param open 是否打开
     * @param animate 是否启用动画
     */
    public void setState(boolean open, boolean animate) {
        if (state != FINISH)
        {
            return;
        }

        if (isContentExpand ^ open)
        {
            isContentExpand = open;
            if (animate)
            {
                state = PREPARE;
                if (open)
                {
                    content.setVisibility(VISIBLE);
                }

                post(panelAnimation);
            }
            else
            {
                state = FINISH;
                content.setVisibility(open ? VISIBLE : GONE);
                update();
            }
        }
    }

    /**
     * 判断抽屉是否打开
     */
    public boolean isOpened() {
        return isContentExpand;
    }

    public interface OnPanelListener {

        /**
         * 抽屉打开
         */
        void onPanelOpened(Panel panel);

        /**
         * 抽屉关闭
         */
        void onPanelClosed(Panel panel);
    }

    /**
     * 抽屉动画
     */
    private final Runnable panelAnimation = new Runnable() {

        @Override
        public void run() {
            int fromXDelta = 0, toXDelta = 0, fromYDelta = 0, toYDelta = 0;
            int duration = 0;
            if (orientation == VERTICAL)
            {
                int height = contentHeight;
                if (!isContentExpand)
                {
                    toYDelta = position == TOP ? -height : height;
                }
                else
                {
                    fromYDelta = position == TOP ? -height : height;
                }

                if (state == SCROLL)
                {
                    if (Math.abs(translateY - fromYDelta) < Math.abs(translateY - toYDelta))
                    {
                        isContentExpand = !isContentExpand;
                        toYDelta = fromYDelta;
                    }

                    fromYDelta = (int) translateY;
                }
                else if (state == FLING)
                {
                    fromYDelta = (int) translateY;
                }

                if (state == FLING)
                {
                    duration = (int) (1000 * Math.abs(toYDelta - fromYDelta) / velocity);
                    duration = Math.max(duration, 20);
                }
                else
                {
                    duration = animationDuration * Math.abs(toYDelta - fromYDelta) / contentHeight;
                }
            }
            else
            {
                int width = contentWidth;
                if (!isContentExpand)
                {
                    toXDelta = position == LEFT ? -width : width;
                }
                else
                {
                    fromXDelta = position == LEFT ? -width : width;
                }

                if (state == SCROLL)
                {
                    if (Math.abs(translateX - fromXDelta) < Math.abs(translateX - toXDelta))
                    {
                        isContentExpand = !isContentExpand;
                        toXDelta = fromXDelta;
                    }

                    fromXDelta = (int) translateX;
                }
                else if (state == FLING)
                {
                    fromXDelta = (int) translateX;
                }

                if (state == FLING)
                {
                    duration = (int) (1000 * Math.abs(toXDelta - fromXDelta) / velocity);
                    duration = Math.max(duration, 20);
                }
                else
                {
                    duration = animationDuration * Math.abs(toXDelta - fromXDelta) / contentWidth;
                }
            }

            translateX = translateY = 0;
            if (duration == 0)
            {
                state = FINISH;
                if (!isContentExpand)
                {
                    content.setVisibility(GONE);
                }

                update();
                return;
            }

            TranslateAnimation anim = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
            anim.setDuration(duration);
            anim.setAnimationListener(animListener);
            if (state == FLING)
            {
                anim.setInterpolator(new LinearInterpolator());
            }
            else if (interpolator != null)
            {
                anim.setInterpolator(interpolator);
            }

            startAnimation(anim);
        }
    };

    /**
     * 抽屉动画监听器
     */
    private final AnimationListener animListener = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            state = ANIMATE;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // 动画结束后的善后工作
            state = FINISH;
            if (!isContentExpand)
            {
                setAnimation(new TranslateAnimation(0, 0, 0, 0)); // 解决抽屉会闪动一下的问题
                content.setVisibility(GONE);
            }

            update();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };

    private class PanelOnGestureListener implements OnGestureListener {

        private float scrollX, scrollY;

        public void init(float scrollX, float scrollY) {
            this.scrollX = scrollX;
            this.scrollY = scrollY;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            state = PREPARE;
            if (isContentExpand = !isContentExpand)
            {
                content.setVisibility(VISIBLE);
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            velocity = orientation == VERTICAL ? velocityY : velocityX;
            if (Math.abs(velocity) > 200)
            {
                state = FLING;
                isContentExpand = (position == TOP || position == LEFT) ^ (velocity < 0);
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {}

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            state = SCROLL;
            float x = 0, y = 0;
            if (orientation == VERTICAL)
            {
                scrollY -= distanceY;
                if (position == TOP)
                {
                    y = ensureRange(scrollY, -contentHeight, 0);
                }
                else
                {
                    y = ensureRange(scrollY, 0, contentHeight);
                }
            }
            else
            {
                scrollX -= distanceX;
                if (position == LEFT)
                {
                    x = ensureRange(scrollX, -contentWidth, 0);
                }
                else
                {
                    x = ensureRange(scrollX, 0, contentWidth);
                }
            }

            if (x != translateX || y != translateY)
            {
                translateX = x;
                translateY = y;
                invalidate();
            }

            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {}

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    }
}