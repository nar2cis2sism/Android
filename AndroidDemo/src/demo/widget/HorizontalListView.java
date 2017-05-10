package demo.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

import java.util.LinkedList;
import java.util.Queue;

public class HorizontalListView extends AdapterView<ListAdapter> {

    ListAdapter mAdapter;
    int mLeftViewIndex = -1;
    private int mRightViewIndex = 0;
    private int mCurrentX;
    int mNextX;
    int mMaxX = Integer.MAX_VALUE;
    private int mDisplayOffset = 0;
    Scroller mScroller;
    private GestureDetector mGesture;
    private Queue<View> mRemovedViewQueue = new LinkedList<View>();
    boolean mDataChanged = false;

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalListView(Context context) {
        super(context);
        init();
    }
    
    private void init()
    {
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;
        mScroller = new Scroller(getContext());
        mGesture = new GestureDetector(getContext(), mOnGesture);
    }
    
    private OnGestureListener mOnGesture = new SimpleOnGestureListener() {
        
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            return true;
        };
        
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            synchronized (HorizontalListView.this) {
                mNextX += (int) distanceX;
            }
            
            requestLayout();
            return true;
        };
        
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            synchronized (HorizontalListView.this) {
                mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
            }
            
            requestLayout();
            return true;
        };
        
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Rect rect = new Rect();
            for (int i = 0, count = getChildCount(); i < count; i++)
            {
                View child = getChildAt(i);
                rect.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                if (rect.contains((int) e.getX(), (int) e.getY()))
                {
                    int position = mLeftViewIndex + 1 + i;
                    performItemClick(child, position, mAdapter.getItemId(position));
                    
                    OnItemSelectedListener onItemSelectedListener = getOnItemSelectedListener();
                    if (onItemSelectedListener != null)
                    {
                        onItemSelectedListener.onItemSelected(
                                HorizontalListView.this, child, position, mAdapter.getItemId(position));
                    }
                    
                    break;
                }
            }
            
            return true;
        };
        
        public void onLongPress(MotionEvent e) {
            Rect rect = new Rect();
            for (int i = 0, count = getChildCount(); i < count; i++)
            {
                View child = getChildAt(i);
                rect.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                if (rect.contains((int) e.getX(), (int) e.getY()))
                {
                    OnItemLongClickListener onItemLongClickListener = getOnItemLongClickListener();
                    if (onItemLongClickListener != null)
                    {
                        int position = mLeftViewIndex + 1 + i;
                        onItemLongClickListener.onItemLongClick(
                                HorizontalListView.this, child, position, mAdapter.getItemId(position));
                    }
                    
                    break;
                }
            }
        };
    };

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null)
        {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        
        if ((mAdapter = adapter) != null)
        {
            mAdapter.registerDataSetObserver(mDataObserver);
        }
        
        reset();
    }
    
    private void reset()
    {
        init();
        removeAllViewsInLayout();
        requestLayout();
    }
    
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        
        public void onChanged() {
            synchronized (HorizontalListView.this) {
                mDataChanged = true;
            }
            
            invalidate();
            requestLayout();
        };
        
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        };
    };
    
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        if (mAdapter == null)
        {
            return;
        }
        
        if (mDataChanged)
        {
            int oldCurrentX = mCurrentX;
            init();
            removeAllViewsInLayout();
            mNextX = oldCurrentX;
            mDataChanged = false;
        }

        if (mScroller.computeScrollOffset())
        {
            mNextX = mScroller.getCurrX();
        }
        
        if (mNextX <= 0)
        {
            mNextX = 0;
            mScroller.forceFinished(true);
        }
        
        if (mNextX >= mMaxX)
        {
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }
        
        int dx = mCurrentX - mNextX;
        
        removeNonVisibleItems(dx);
        fillList(dx);
        positionItems(dx);
        
        mCurrentX = mNextX;
        
        if (!mScroller.isFinished())
        {
            post(requestRefresh);
        }
    };
    
    private final Runnable requestRefresh = new Runnable() {
        
        @Override
        public void run() {
            requestLayout();
        }
    };
    
    private void removeNonVisibleItems(final int dx)
    {
        View child = getChildAt(0);
        while (child != null && child.getRight() + dx <= 0)
        {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);
        }
        
        child = getChildAt(getChildCount() -1);
        while (child != null && child.getLeft() + dx >= getWidth())
        {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount() -1);
        }
    }
    
    private void fillList(final int dx)
    {
        int edge = 0;
        View child = getChildAt(getChildCount() -1);
        if(child != null)
        {
            edge = child.getRight();
        }
        
        fillListRight(edge, dx);
        
        edge = 0;
        child = getChildAt(0);
        if(child != null)
        {
            edge = child.getLeft();
        }
        
        fillListLeft(edge, dx);
    }
    
    private void fillListRight(int rightEdge, final int dx)
    {
        while (rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount())
        {
            View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();
            
            if (mRightViewIndex == mAdapter.getCount() -1)
            {
                mMaxX = mCurrentX + rightEdge - getWidth();
            }
            
            if (mMaxX < 0)
            {
                mMaxX = 0;
            }
            
            mRightViewIndex++;
        }
    }
    
    private void fillListLeft(int leftEdge, final int dx)
    {
        while (leftEdge + dx > 0 && mLeftViewIndex >= 0)
        {
            View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, 0);
            leftEdge -= child.getMeasuredWidth();
            
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
    }
    
    private void addAndMeasureChild(final View child, int viewPos)
    {
        LayoutParams params = child.getLayoutParams();
        if (params == null)
        {
            params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST), 
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
    }
    
    private void positionItems(final int dx)
    {
        if (getChildCount() > 0)
        {
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for (int i = 0, count = getChildCount(); i < count; i++)
            {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                left += childWidth;
            }
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mGesture.onTouchEvent(ev);
    }

    @Override
    public View getSelectedView() {
        // TODO implement
        return null;
    }

    @Override
    public void setSelection(int position) {
        // TODO implement
    }
    
    public void scrollTo(int x)
    {
        mScroller.startScroll(mNextX, 0, x - mNextX, 0);
        requestLayout();
    }
}