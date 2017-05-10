package demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A ListView that maintains a header pinned at the top of the list.<br>
 * The pinned header can be pushed up and dissolved as needed.
 * @author Daimon
 * @version 4.0
 * @since 7/30/2012
 */

public class PinnedHeaderListView extends ListView implements OnScrollListener {

    /**
     * Pinned header state: don't show the header.
     */
    private static final int PINNED_HEADER_GONE = 0;

    /**
     * Pinned header state: show the header at the top of the list.
     */
    private static final int PINNED_HEADER_VISIBLE = 1;

    /**
     * Pinned header state: show the header. If the header extends beyond
     * the bottom of the first shown element, push it up and clip.
     */
    private static final int PINNED_HEADER_PUSHED_UP = 2;
	
	private View pinnedHeaderView;
	private boolean pinnedHeaderViewVisible;
	private int pinnedHeaderViewWidth;
	private int pinnedHeaderViewHeight;
	
	private PinnedHeaderAdapter adapter;
	private int pinnedHeaderState = PINNED_HEADER_GONE;
	
	private OnScrollListener onScrollListener;

	public PinnedHeaderListView(Context context) {
		super(context);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setPinnedHeaderView(View view) {
		setVerticalFadingEdgeEnabled((pinnedHeaderView = view) == null);
		requestLayout();
	}
	
	public void setPinnedHeaderView(int resId) {
		setPinnedHeaderView(LayoutInflater.from(getContext()).inflate(resId, this, false));
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter instanceof PinnedHeaderAdapter)
		{
			this.adapter = (PinnedHeaderAdapter) adapter;
			super.setOnScrollListener(this);
		}
		else
		{
			this.adapter = null;
			super.setOnScrollListener(onScrollListener);
			pinnedHeaderViewVisible = false;
		}
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
	    if (adapter == null)
	    {
	        super.setOnScrollListener(onScrollListener = l);
	    }
	    else
	    {
	        onScrollListener = l;
	    }
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (pinnedHeaderView != null)
		{
			measureChild(pinnedHeaderView, widthMeasureSpec, heightMeasureSpec);
			pinnedHeaderViewWidth = pinnedHeaderView.getMeasuredWidth();
			pinnedHeaderViewHeight = pinnedHeaderView.getMeasuredHeight();
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (pinnedHeaderView != null && pinnedHeaderViewWidth > 0 && pinnedHeaderViewHeight > 0)
		{
			pinnedHeaderView.layout(0, 0, pinnedHeaderViewWidth, pinnedHeaderViewHeight);
			if (getCount() > 0) configurePinnedHeader(getFirstVisiblePosition());
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (pinnedHeaderViewVisible && pinnedHeaderView.getVisibility() == VISIBLE)
		{
			drawChild(canvas, pinnedHeaderView, getDrawingTime());
		}
	}
	
	public void configurePinnedHeader(int position) {
		if (pinnedHeaderView == null || adapter == null)
		{
			return;
		}
		
		switch (pinnedHeaderState) {
		case PINNED_HEADER_GONE:
			pinnedHeaderViewVisible = false;
			break;
		case PINNED_HEADER_VISIBLE:
			adapter.configurePinnedHeader(pinnedHeaderView, position, 1.0f);
			if (pinnedHeaderView.getTop() != 0)
			{
				pinnedHeaderView.layout(0, 0, pinnedHeaderViewWidth, pinnedHeaderViewHeight);
			}
			
			pinnedHeaderViewVisible = true;
			break;
		case PINNED_HEADER_PUSHED_UP:
			View firstView = getChildAt(0);
			if (firstView != null)
			{
				int bottom = firstView.getBottom();
				int y = 0;
				float ratio = 1.0f;
				if (bottom < pinnedHeaderViewHeight)
				{
					y = bottom - pinnedHeaderViewHeight;
					if (pinnedHeaderViewHeight > 0)
					{
						ratio = ratio * bottom / pinnedHeaderViewHeight;
					}
				}
				
				adapter.configurePinnedHeader(pinnedHeaderView, position, ratio);
				if (pinnedHeaderView.getTop() != y)
				{
					pinnedHeaderView.layout(0, y, pinnedHeaderViewWidth, pinnedHeaderViewHeight);
				}
				
				pinnedHeaderViewVisible = true;
			}
			
			break;
		}
	}

	/**
	 * Adapter interface.The list adapter must implement this interface.
	 */
	
	public static interface PinnedHeaderAdapter {
        
        /**
         * Computes the desired state of the pinned header for the given
         * position of the first visible list item.
         *
         * @param position position of the first visible list item.
         * @return true indicates the pinned header view needs to push up.
         */
        public boolean getPinnedHeaderState(int position);

        /**
         * Configures the pinned header view to match the first visible list item.
         *
         * @param header pinned header view.
         * @param position position of the first visible list item.
         * @param visible ratio, between 0.0 and 1.0.
         */
        public void configurePinnedHeader(View header, int position, float visibleRatio);
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (onScrollListener != null)
		{
		    onScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	    if (onScrollListener != null)
	    {
	        onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	    }
	    
		if (adapter != null && totalItemCount > 0)
		{
			if (firstVisibleItem < getHeaderViewsCount())
			{
				pinnedHeaderState = PINNED_HEADER_GONE;
			}
			else
			{
				if (firstVisibleItem < totalItemCount - 1
				&&  adapter.getPinnedHeaderState(firstVisibleItem))
				{
					pinnedHeaderState = PINNED_HEADER_PUSHED_UP;
				}
				else
				{
				    pinnedHeaderState = PINNED_HEADER_VISIBLE;
				}
			}
			
			configurePinnedHeader(firstVisibleItem);
		}
	}
}