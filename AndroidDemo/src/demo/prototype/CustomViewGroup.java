package demo.prototype;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public class CustomViewGroup extends ViewGroup {

    /** Daimon:Scroller **/
    private Scroller scroller;

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
    
    private void init(Context context) {
	    scroller = new Scroller(context);
        // TODO Auto-generated method stub
	}
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    
        // TODO Auto-generated method stub
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        // TODO Auto-generated method stub
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            getChildAt(i).layout(left, top, right, bottom);
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    
        // TODO Auto-generated method stub
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            drawChild(canvas, getChildAt(i), getDrawingTime());
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
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    // TODO Auto-generated method stub
	    return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
	}

    public void smoothScrollTo(int x, int y, int duration) {
        // TODO Auto-generated method stub
        scroller.startScroll(getScrollX(), getScrollY(), x - getScrollX(), y - getScrollY(), duration);
        invalidate();
    }
    
    public static abstract class TouchEventDelegate implements OnTouchListener {

        /** Daimon:VelocityTracker **/
        private VelocityTracker velocityTracker;
        
        protected int lastMotionX;
        protected int lastMotionY;

        public boolean onTouchEvent(MotionEvent event) {
            if (velocityTracker == null)
            {
                velocityTracker = VelocityTracker.obtain();
            }

            velocityTracker.addMovement(event);

            boolean consumed = false;
            int x = (int) event.getX();
            int y = (int) event.getY();
            
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMotionX = x;
                lastMotionY = y;
                consumed = handleActionDown(event, x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                consumed = handleActionMove(event, x, y);
                lastMotionX = x;
                lastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = velocityTracker.getXVelocity();
                float velocityY = velocityTracker.getYVelocity();
                consumed = handleActionUp(event, velocityX, velocityY);
                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
            }
            
            return consumed;
        }

        private void releaseVelocityTracker() {
            if (velocityTracker != null)
            {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }

        public abstract boolean handleActionDown(MotionEvent event, int x, int y);

        public abstract boolean handleActionMove(MotionEvent event, int x, int y);

        public abstract boolean handleActionUp(MotionEvent event, float velocityX, float velocityY);

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onTouchEvent(event);
        }
    }
}