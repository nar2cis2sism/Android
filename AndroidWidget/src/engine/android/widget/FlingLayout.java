package engine.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import engine.android.widget.base.CustomViewGroup;

/**
 * 滑动布局
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class FlingLayout extends CustomViewGroup {

    private static final int SNAP_VELOCITY = 600;

    private int touchSlop;

    private int itemCount;

    private int currentItem = 0;

    private OnViewChangeListener listener;

    public FlingLayout(Context context) {
        super(context);
    }

    public FlingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void init(Context context) {
        super.init(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnViewChangeListener(OnViewChangeListener onViewChangeListener) {
        listener = onViewChangeListener;
    }

    public int getItemCount() {
        return itemCount;
    }

    public View getItem(int index) {
        if (index < 0 || index >= itemCount)
        {
            return null;
        }

        int visibleIndex = 0;
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE && visibleIndex++ == index)
            {
                return child;
            }
        }

        return null;
    }

    public void setCurrentItem(int item) {
        item = getValidItem(item);
        if (currentItem != item)
        {
            scrollTo((currentItem = item) * getWidth(), 0);
            invalidate();
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Calculate our view width
        int width = right - left;

        itemCount = 0;
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE)
            {
                itemCount++;
                child.layout(
                        childLeft,
                        childTop,
                        childLeft + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());
                childLeft += width;
            }
        }

        if (changed) scrollTo(currentItem * width, 0);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return delegate.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return delegate.onTouchEvent(event);
    }
    
    private final TouchEventDelegate delegate = new TouchEventDelegate() {
        
        public boolean interceptActionDown(MotionEvent event, int x, int y) {
            return abortScroll();
        };
        
        public boolean interceptActionMove(MotionEvent event, int x, int y) {
            return Math.abs(lastMotionX - x) > touchSlop;
        };

        @Override
        public boolean handleActionDown(MotionEvent event, int x, int y) {
            return true;
        }

        @Override
        public void handleActionMove(MotionEvent event, int x, int y) {
            move(lastMotionX - x);
        }

        @Override
        public void handleActionUp(MotionEvent event, float velocityX, float velocityY) {
            if (velocityX > SNAP_VELOCITY && currentItem > 0)
            {
                // Fling enough to move left
                snapToItem(currentItem - 1);
            }
            else if (velocityX < -SNAP_VELOCITY && currentItem < itemCount - 1)
            {
                // Fling enough to move right
                snapToItem(currentItem + 1);
            }
            else
            {
                snapToDestination();
            }
        }
        
        public void handleActionCancel(MotionEvent event) {
            snapToDestination();
        };
    };

    private void move(int deltaX) {
        int scrollX = getScrollX() + deltaX;
        if (deltaX > 0)
        {
            scrollX = Math.min(scrollX, (itemCount - 1) * getWidth());
        }
        else if (deltaX < 0)
        {
            scrollX = Math.max(scrollX, 0);
        }
        
        if (scrollX != getScrollX())
        {
            scrollTo(scrollX, getScrollY());
        }
    }
    
    /**
     * Get the valid item in case out of range.
     */
    private int getValidItem(int item) {
        return Math.max(0, Math.min(item, itemCount - 1));
    }

    private void snapToDestination() {
        int width = getWidth();
        snapToItem((getScrollX() + width / 2) / width);
    }

    private void snapToItem(int item) {
        item = getValidItem(item);
        int delta = item * getWidth() - getScrollX();
        if (delta != 0)
        {
            smoothScroll(delta, 0, Math.abs(delta) * 2);
            if (currentItem != item) notifyOnViewChanged(currentItem = item);
        }
    }
    
    private void notifyOnViewChanged(int item) {
        if (listener != null) listener.OnViewChanged(item);
    }

    public interface OnViewChangeListener {

        void OnViewChanged(int childIndex);
    }
}