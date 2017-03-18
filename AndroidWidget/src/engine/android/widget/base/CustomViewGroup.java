package engine.android.widget.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 自定义ViewGroup模板
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class CustomViewGroup extends ViewGroup {

    /** Daimon:Scroller **/
    protected Scroller scroller;

	public CustomViewGroup(Context context) {
		super(context);
		init(context);
	}

	public CustomViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
    
    protected void init(Context context) {
	    scroller = new Scroller(context);
	}
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE)
            {
                child.layout(
                        childLeft,
                        childTop,
                        childLeft + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());
            }
        }
    }

	@Override
	public void computeScroll() {
	    if (scroller.computeScrollOffset())
	    {
	        scrollTo(scroller.getCurrX(), scroller.getCurrY());
	        invalidate();
	    }
	}

    public void smoothScrollTo(int x, int y, int duration) {
        scroller.startScroll(getScrollX(), getScrollY(), x - getScrollX(), y - getScrollY(), duration);
        invalidate();
    }
    
    public void smoothScroll(int dx, int dy, int duration) {
        scroller.startScroll(getScrollX(), getScrollY(), dx, dy, duration);
        invalidate();
    }
    
    public boolean abortScroll() {
        if (scroller.isFinished()) return false;
        scroller.abortAnimation(); return true;
    }
    
    public static abstract class TouchEventDelegate implements OnTouchListener {

        /** Daimon:VelocityTracker **/
        private VelocityTracker velocityTracker;
        
        protected int lastMotionX;
        protected int lastMotionY;

        public boolean onInterceptTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
            {
                releaseVelocityTracker();
                return false;
            }

            obtainVelocityTracker().addMovement(event);

            int x = (int) event.getX();
            int y = (int) event.getY();
            
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return interceptActionDown(event, lastMotionX = x, lastMotionY = y);
                case MotionEvent.ACTION_MOVE:
                    if (interceptActionMove(event, x, y))
                    {
                        lastMotionX = x;
                        lastMotionY = y;
                        return true;
                    }
                    
                    break;
            }
            
            return false;
        }

        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_CANCEL)
            {
                releaseVelocityTracker();
                return false;
            }

            obtainVelocityTracker().addMovement(event);

            int x = (int) event.getX();
            int y = (int) event.getY();
            
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return handleActionDown(event, lastMotionX = x, lastMotionY = y);
                case MotionEvent.ACTION_MOVE:
                    handleActionMove(event, x, y);
                    lastMotionX = x;
                    lastMotionY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    velocityTracker.computeCurrentVelocity(1000);
                    handleActionUp(event, velocityTracker.getXVelocity(), velocityTracker.getYVelocity());
                    releaseVelocityTracker();
                    break;
            }
            
            return false;
        }
        
        protected VelocityTracker obtainVelocityTracker() {
            if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
            return velocityTracker;
        }

        protected void releaseVelocityTracker() {
            if (velocityTracker != null)
            {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }
        
        public boolean interceptActionDown(MotionEvent event, int x, int y) {
            return false;
        }
        
        public boolean interceptActionMove(MotionEvent event, int x, int y) {
            return false;
        }

        public abstract boolean handleActionDown(MotionEvent event, int x, int y);

        public abstract void handleActionMove(MotionEvent event, int x, int y);

        public abstract void handleActionUp(MotionEvent event, float velocityX, float velocityY);

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onTouchEvent(event);
        }
    }
}