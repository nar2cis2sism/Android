package demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * ImageView which can rotate it's content.
 * @author Daimon
 * @version 4.0
 * @since 4/15/2013
 */

public class RotateImageView extends ImageView {
    
    private int degree;
    private int fromDegree;
    private int toDegree;
    
    private static final long ANIMATION_NONE  = -1;
    private long mAnimationStartTime = ANIMATION_NONE;
    private long mAnimationEndTime;
    private long mDuration;

    public RotateImageView(Context context) {
        super(context);
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setDegree(int degree) {
        if (toDegree != degree)
        {
            this.degree = toDegree = (degree + 360) % 360;
            invalidate();
        }
    }
    
    public void setDegree(int degree, long duration) {
        rotateDegree(degree - this.degree, duration);
    }
    
    public void rotateDegree(int degree, long duration) {
        rotateDegree(degree > 0, Math.abs(degree), duration);
    }

    /**
     * @param cw clockwise
     */
    
    public void rotateDegree(boolean cw, int degree, long duration) {
        if (degree == 0)
        {
            return;
        }
        
        fromDegree = this.degree = toDegree;
        toDegree = cw ? fromDegree + degree : fromDegree - degree;
        
        mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        mAnimationEndTime = mAnimationStartTime + (mDuration = duration);
        invalidate();
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
        
        if (degree != toDegree)
        {
            if (mAnimationStartTime != ANIMATION_NONE)
            {
                long time = AnimationUtils.currentAnimationTimeMillis();
                if (time < mAnimationEndTime)
                {
                    long animTime = time - mAnimationStartTime;
                    degree = (int) (fromDegree + animTime * (toDegree - fromDegree) / mDuration);
                    invalidate();
                }
                else
                {
                    degree = (toDegree + 360) % 360;
                    mAnimationStartTime = ANIMATION_NONE;
                }
            }
            else
            {
                degree = toDegree;
            }
        }
        
        int left = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        int top = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();

        int saveCount = canvas.getSaveCount();
        canvas.translate(left, top);
        canvas.rotate(degree);
        canvas.translate(-drawableWidth / 2, -drawableHeight / 2);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}