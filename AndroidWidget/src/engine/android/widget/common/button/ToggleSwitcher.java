package engine.android.widget.common.button;

import engine.android.widget.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.Checkable;
import android.widget.ToggleButton;

/**
 * Displays checked/unchecked states as a switcher.
 * 
 * @author Daimon
 * @since 4/15/2013
 * 
 * @see {@link ToggleButton}
 */
public class ToggleSwitcher extends View implements Checkable {
    
    private static final long ANIMATION_SPEED = 450;    // move pixel per second
    private static final long ANIMATION_NONE  = -1;
    private static final int NO_ALPHA = 0xFF;

    private static final Xfermode MASK_XFERMODE
    = new PorterDuffXfermode(Mode.SRC_IN);
    
    private int mAlpha = NO_ALPHA;                      // 当前透明度，这里主要用于控件Disable时设置半透明
    private int mPositionLeft,mPositionRight;           // 滑动范围
    private int mThumbLeft,mThumbRight;                 // 滑块区域
    private int mTop;

    private Paint mPaint;
    private Bitmap mMask;
    private Bitmap mBackground;
    private Bitmap mFrame;
    private Bitmap mThumbNormal;
    private Bitmap mThumbPressed;
    private int mTouchSlop, mClickTimeout;

    private int mPosition;
    private int mDownX, mLastMotionX;
    private long mAnimationStartTime = ANIMATION_NONE;
    private int mAnimationStartPosition;
    
    private boolean mChecked;
    private boolean mBroadcasting;

    private OnSwitchChangeListener mOnSwitchChangeListener;

    public ToggleSwitcher(Context context) {
        this(context, null);
    }

    public ToggleSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMask = BitmapFactory.decodeResource(res, R.drawable.toggle_switcher_mask);
        mBackground = BitmapFactory.decodeResource(res, R.drawable.toggle_switcher_bg);
        mFrame = BitmapFactory.decodeResource(res, R.drawable.toggle_switcher_frame);
        mThumbNormal = BitmapFactory.decodeResource(res, R.drawable.toggle_switcher_thumb_normal);
        mThumbPressed = BitmapFactory.decodeResource(res, R.drawable.toggle_switcher_thumb_pressed);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mClickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mAlpha = enabled ? NO_ALPHA : NO_ALPHA / 2;
        super.setEnabled(enabled);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked)
        {
            mChecked = checked;
            refreshDrawableState();
            invalidate();
            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting)
            {
                return;
            }

            mBroadcasting = true;
            if (mOnSwitchChangeListener != null)
            {
                mOnSwitchChangeListener.onSwitchChanged(this, mChecked);
            }

            mBroadcasting = false;
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    public void setOnSwitchChangeListener(OnSwitchChangeListener listener) {
        mOnSwitchChangeListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mMask.getWidth();
        int height = mMask.getHeight();
        setMeasuredDimension(width, height);

        int thumbWidth = mThumbNormal.getWidth();
        int thumbHeight = mThumbNormal.getHeight();

        width -= getPaddingLeft() + getPaddingRight();
        int dx = width - thumbWidth;
        if (dx > 0)
        {
            mPositionLeft = 0;
            mPositionRight = dx;
            mThumbLeft = 0;
            mThumbRight = thumbWidth;
        }
        else
        {
            mPositionLeft = dx;
            mPositionRight = 0;
            thumbWidth = 2 * width - thumbWidth;
            mThumbLeft = width - thumbWidth;
            mThumbRight = mThumbLeft + thumbWidth;
        }

        height -= getPaddingTop() + getPaddingBottom();
        mTop = (height - thumbHeight) / 2 + getPaddingTop();
    }

    @SuppressLint("WrongCall")
    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.saveLayerAlpha(null, mAlpha, Canvas.ALL_SAVE_FLAG);
        onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPositionLeft == mPositionRight)
        {
            return;
        }

        if (mAnimationStartTime != ANIMATION_NONE)
        {
            long animTime = AnimationUtils.currentAnimationTimeMillis() - mAnimationStartTime;
            mPosition = (int) (mAnimationStartPosition + ANIMATION_SPEED
                      * (mChecked ? -animTime : animTime) / 1000);
            if (mPosition < mPositionLeft)
            {
                mPosition = mPositionLeft;
            }

            if (mPosition > mPositionRight)
            {
                mPosition = mPositionRight;
            }

            boolean finish = mPosition == (mChecked ? mPositionLeft : mPositionRight);
            if (finish)
            {
                mAnimationStartTime = ANIMATION_NONE;
            }
            else
            {
                invalidate();
            }
        }
        else if (!isPressed())
        {
            mPosition = mChecked ? mPositionLeft : mPositionRight;
        }

        _draw(canvas);
    }

    private void _draw(Canvas canvas) {
        int left = getPaddingLeft() + mPosition;
        // 绘制蒙板
        canvas.drawBitmap(mMask, 0, mTop, mPaint);
        // 绘制背景
        mPaint.setXfermode(MASK_XFERMODE);
        canvas.drawBitmap(mBackground, left, mTop, mPaint);
        mPaint.setXfermode(null);
        // 绘制边框
        canvas.drawBitmap(mFrame, 0, mTop, mPaint);
        // 绘制滑块
        canvas.drawBitmap(isPressed() ? mThumbPressed : mThumbNormal, left, mTop, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
        {
            return false;
        }

        if (mAnimationStartTime == ANIMATION_NONE)
        {
            int x = (int) event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int deltaX = (mDownX = x) - getPaddingLeft() - mPosition;
                    if (deltaX >= mThumbLeft && deltaX <= mThumbRight)
                    {
                        mLastMotionX = x;
                        setPressed(true);
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isPressed())
                    {
                        int position = mPosition + x - mLastMotionX;
                        if (position < mPositionLeft)
                        {
                            position = mPositionLeft;
                        }

                        if (position > mPositionRight)
                        {
                            position = mPositionRight;
                        }

                        if (mPosition != position)
                        {
                            mPosition = position;
                            mLastMotionX = x;
                            invalidate();
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(x - mDownX) < mTouchSlop
                    &&  event.getEventTime() - event.getDownTime() < mClickTimeout)
                    {
                        performClick();
                        setPressed(false);
                        break;
                    }
                    else if (!isPressed())
                    {
                        break;
                    }
                case MotionEvent.ACTION_CANCEL:
                    snapToSwitch(mPosition < (mPositionLeft + mPositionRight) / 2);
                    setPressed(false);
                    break;
            }
        }

        return true;
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    private void snapToSwitch(boolean switchOn) {
        setChecked(switchOn);
        startParkingAnimation();
    }

    private void startParkingAnimation() {
        mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        mAnimationStartPosition = mPosition;
    }

    @Override
    public boolean performClick() {
        /* When clicked, toggle the state */
        toggle();
        startParkingAnimation();
        return super.performClick();
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a ToggleSwitcher changed.
     */
    public interface OnSwitchChangeListener {

        /**
         * Called when the checked state of a ToggleSwitcher has changed.
         * 
         * @param switcher The ToggleSwitcher view whose state has changed.
         * @param isChecked The new checked state of ToggleSwitcher.
         */
        void onSwitchChanged(ToggleSwitcher switcher, boolean isChecked);
    }
}