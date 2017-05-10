package demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.Checkable;
import android.widget.ImageView;

/**
 * @see {@link #ToggleButton}
 * @author Daimon
 * @version 4.0
 * @since 4/15/2013
 */

public class ToggleSwitcher extends ImageView implements Checkable {
    
    private boolean mChecked;
    private boolean mBroadcasting;
    private OnSwitchChangeListener mOnSwitchChangeListener;
    
    private static final long ANIMATION_SPEED = 200;//move pixel per second
    private static final long ANIMATION_NONE  = -1;

    private int mPosition;
    private int mLastMotionY;
    private long mAnimationStartTime = ANIMATION_NONE;
    private int mAnimationStartPosition;

    public ToggleSwitcher(Context context) {
        super(context);
    }
    
    public ToggleSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleSwitcher(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        
        if (drawable == null)
        {
            return;
        }
        
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        
        if (drawableWidth == 0 || drawableHeight == 0)
        {
            //nothing to draw (empty bounds)
            return;
        }
        
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom() - drawableHeight;
        if (mAnimationStartTime != ANIMATION_NONE)
        {
            long time = AnimationUtils.currentAnimationTimeMillis();
            long animTime = time - mAnimationStartTime;
            mPosition = (int) (mAnimationStartPosition + ANIMATION_SPEED * (mChecked ? animTime : -animTime) / 1000);
            if (mPosition < 0)
            {
                mPosition = 0;
            }
            
            if (mPosition > availableHeight)
            {
                mPosition = availableHeight;
            }
            
            boolean finish = mPosition == (mChecked ? availableHeight : 0);
            if (!finish)
            {
                invalidate();
            }
            else
            {
                mAnimationStartTime = ANIMATION_NONE;
            }
        }
        else if (!isPressed())
        {
            mPosition = mChecked ? availableHeight : 0;
        }
        
        int left = (getWidth() - getPaddingLeft() - getPaddingRight() - drawableWidth) / 2 + getPaddingLeft();
        int top = getPaddingTop() + mPosition;
        
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(left, top);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consume = super.onTouchEvent(event);
        if (!consume && mAnimationStartTime == ANIMATION_NONE)
        {
            int action = event.getAction();
            int y = (int) event.getY();
            Drawable drawable = getDrawable();
            int drawableHeight = drawable.getIntrinsicHeight();
            int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom() - drawableHeight;
            
            if (action == MotionEvent.ACTION_DOWN)
            {
                if (y - getPaddingTop() >= mPosition && y - getPaddingTop() <= mPosition + drawableHeight)
                {
                    mLastMotionY = y;
                    setPressed(true);
                }
            }
            else if (isPressed())
            {
                switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    trackTouchEvent(y - mLastMotionY, availableHeight);
                    mLastMotionY = y;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    snapToSwitch(mPosition >= availableHeight / 2);
                    setPressed(false);
                    break;
                }
            }
        }
        
        return true;
    }
    
    private void trackTouchEvent(int distance, int availableHeight) {
        mPosition += distance;
        if (mPosition < 0)
        {
            mPosition = 0;
        }
        
        if (mPosition > availableHeight)
        {
            mPosition = availableHeight;
        }
        
        invalidate();
    }
    
    private void snapToSwitch(boolean switchOn) {
        try {
            setChecked(switchOn);
        } finally {
            startParkingAnimation();
        }
    }

    private void startParkingAnimation() {
        mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        mAnimationStartPosition = mPosition;
    }
    
    public void setOnSwitchChangeListener(OnSwitchChangeListener listener) {
        mOnSwitchChangeListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a ToggleSwitcher changed.
     */
    
    public static interface OnSwitchChangeListener {
        
        /**
         * Called when the checked state of a ToggleSwitcher has changed.
         * @param switcher The ToggleSwitcher view whose state has changed.
         * @param isChecked The new checked state of ToggleSwitcher.
         */
        
        public void onSwitchChanged(ToggleSwitcher switcher, boolean isChecked);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked)
        {
            mChecked = checked;
            refreshDrawableState();
            
            //Avoid infinite recursions if setChecked() is called from a listener
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
            invalidate();
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
    
    @Override
    public boolean performClick() {
        /* When clicked, toggle the state */
        toggle();
        return super.performClick();
    }
}