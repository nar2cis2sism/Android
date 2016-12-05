package engine.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 滑动布局
 * 
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */
public class FlingLayout extends ViewGroup {

    private static final int SNAP_VELOCITY = 600;

    private VelocityTracker velocityTracker;

    private Scroller scroller;

    private int itemCount;

    private int currentItem = 0;

    private int lastMotionX;

    private OnViewChangeListener onViewChangeListener;

    public FlingLayout(Context context) {
        super(context);
        init(context);
    }

    public FlingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
    }

    public void setOnViewChangeListener(OnViewChangeListener onViewChangeListener) {
        this.onViewChangeListener = onViewChangeListener;
    }

    public int getItemCount() {
        return itemCount;
    }

    public View getItem(int index) {
        if (index < 0 || index > itemCount - 1)
        {
            return null;
        }

        int visibleIndex = 0;
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE)
            {
                if (visibleIndex++ == index)
                {
                    return childView;
                }
            }
        }

        return null;
    }

    public void setCurrentItem(int currentItem) {
        if (this.currentItem != currentItem)
        {
            scrollTo((this.currentItem = currentItem) * getWidth(), 0);
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        if (changed)
        {
            // Calculate our view width
            int width = right - left;

            itemCount = 0;
            int childLeft = left;
            for (int i = 0, childCount = getChildCount(); i < childCount; i++)
            {
                View childView = getChildAt(i);
                if (childView.getVisibility() != GONE)
                {
                    itemCount++;
                    childView.layout(
                            childLeft,
                            top,
                            childLeft + childView.getMeasuredWidth(),
                            childView.getMeasuredHeight());
                    childLeft += width;
                }
            }

            scrollTo(currentItem * width, 0);
        }
    }

    private void snapToDestination() {
        int width = getWidth();
        int destination = (getScrollX() + width / 2) / width;
        snapToItem(destination);
    }

    private void snapToItem(int item) {
        // get the valid item
        item = Math.max(0, Math.min(item, itemCount - 1));
        if (getScrollX() != (item * getWidth()))
        {
            int delta = item * getWidth() - getScrollX();
            scroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
            invalidate();

            if (item != currentItem)
            {
                currentItem = item;
                if (onViewChangeListener != null)
                {
                    onViewChangeListener.OnViewChanged(currentItem);
                }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);

        int x = (int) event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished())
                {
                    scroller.abortAnimation();
                }

                lastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                move(lastMotionX - x);
                lastMotionX = x;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = velocityTracker.getXVelocity();
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

                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
        }

        return true;
    }

    private void releaseVelocityTracker() {
        if (velocityTracker != null)
        {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void move(int deltaX) {
        int scrollX = Math.min(Math.max(getScrollX() + deltaX, 0), (itemCount - 1) * getWidth());
        if (scrollX != getScrollX())
        {
            scrollTo(scrollX, getScrollY());
        }
    }

    public static interface OnViewChangeListener {

        public void OnViewChanged(int childIndex);
    }
}