package engine.android.widget.common.list;

import engine.android.widget.R;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 下拉刷新&上拉加载列表
 * 
 * @author Daimon
 * @since 7/30/2012
 */
public class RefreshListView extends ListView implements OnScrollListener {

    private static final int NONE = 0;
    private static final int PULL_TO_REFRESH = 1;
    private static final int RELEASE_TO_REFRESH = 2;
    private static final int REFRESHING = 3;
    
    private int refreshState = -1;
    private boolean isLoading;

    private RotateAnimation pullAnim;
    private RotateAnimation releaseAnim;

    private LinearLayout refreshLayout;
    private ImageView refreshArrow;
    private ProgressBar refreshProgress;
    private TextView refreshText;

    private FrameLayout loadLayout;
    private TextView loadText;

    private int refreshLayout_originalTopPadding;
    private int refreshLayout_height;
    private ValueAnimator refreshLayout_anim;
    private int touchY;
    
    private OnRefreshListener refreshListener;
    private OnLoadListener loadListener;
    private OnScrollListener scrollListener;
    
    private boolean isRefreshable;
    private boolean isLoadable;
    
    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        // Load all of the animations we need in code rather than through XML
        pullAnim = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, 
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        pullAnim.setDuration(250);
        pullAnim.setFillAfter(true);
        releaseAnim = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, 
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        releaseAnim.setDuration(250);
        releaseAnim.setFillAfter(true);
        
        refreshLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.refresh_header, this, false);
        refreshArrow = (ImageView) refreshLayout.findViewById(R.id.arrow);
        refreshProgress = (ProgressBar) refreshLayout.findViewById(R.id.progress);
        refreshText = (TextView) refreshLayout.findViewById(R.id.text);
        
        loadLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.refresh_footer, this, false);
        loadText = (TextView) loadLayout.findViewById(R.id.text);
        
        refreshLayout_originalTopPadding = refreshLayout.getPaddingTop();
        refreshLayout.measure(0, 0);
        refreshLayout_height = refreshLayout.getMeasuredHeight();
        
        addHeaderView(refreshLayout, null, false);
        addFooterView(loadLayout);
        
        resetHeader(false);
        super.setOnScrollListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        scrollListener = l;
    }
    
    public void setRefreshEnabled(boolean enabled) {
        isRefreshable = enabled;
    }
    
    /**
     * Register a callback to be invoked when this list should be refreshed.
     * 
     * @param refreshListener The callback to run.
     */
    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        isRefreshable = (this.refreshListener = refreshListener) != null;
    }
    
    public void setLoadable(boolean enabled) {
        if (isLoadable = enabled)
        {
            loadText.setText(R.string.refresh_load);
            loadText.setVisibility(VISIBLE);
        }
        else
        {
            if (isLoading)
            {
                loadText.setText(R.string.refresh_no_more_data);
            }
            else
            {
                loadText.setVisibility(GONE);
            }
        }

        onLoad(false);
    }

    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Register a callback to be invoked when this list should be loaded.
     *
     * @param loadListener The callback to run.
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    /**
     * Start/Stop to refresh for external call.
     */
    public void notifyRefresh(boolean startOrStop) {
        if (startOrStop)
        {
            if (refreshState != REFRESHING) prepareForRefresh();
        }
        else
        {
            if (refreshState != NONE) onRefreshComplete();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRefreshable && refreshState != REFRESHING)
        {
            int y = (int) ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (getFirstVisiblePosition() == 0)
                    {
                        refreshArrow.setVisibility(VISIBLE);
                        
                        int bottom = refreshLayout.getBottom();
                        if (bottom < refreshLayout_height
                        &&  refreshState != PULL_TO_REFRESH)
                        {
                            refreshText.setText(R.string.refresh_pull);
                            if (refreshState != NONE)
                            {
                                refreshArrow.clearAnimation();
                                refreshArrow.startAnimation(releaseAnim);
                            }
                            
                            refreshState = PULL_TO_REFRESH;
                        }
                        else if (bottom >= refreshLayout_height
                             &&  refreshState != RELEASE_TO_REFRESH)
                        {
                            refreshText.setText(R.string.refresh_release);
                            refreshArrow.clearAnimation();
                            refreshArrow.startAnimation(pullAnim);
                            refreshState = RELEASE_TO_REFRESH;
                        }
                        
                        applyHeaderPadding(ev);
                    }
                    else
                    {
                        touchY = y;
                    }
                    
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (refreshState == RELEASE_TO_REFRESH)
                    {
                        onLoad(false);
                        // Initiate the refresh
                        prepareForRefresh();
                    }
                    else if (refreshState == PULL_TO_REFRESH)
                    {
                        // Abort refresh and reset
                        resetHeader(true);
                    }
                    
                    break;
            }
        }
            
        return super.onTouchEvent(ev);
    }
    
    /**
     * Resets the header to the original state.
     */
    private void resetHeader(boolean anim) {
        if (refreshState != NONE)
        {
            refreshState = NONE;
            resetHeaderPadding(anim);
        }
    }

    /**
     * Sets the header padding back to original size.
     */
    private void resetHeaderPadding(boolean anim) {
        if (anim)
        {
            animSetHeaderPadding(refreshLayout_originalTopPadding
                    - refreshLayout_height);
        }
        else
        {
            setHeaderPadding(refreshLayout_originalTopPadding
                    - refreshLayout_height);
        }
    }

    private void animSetHeaderPadding(int paddingTop) {
        if (refreshLayout_anim != null)
        {
            refreshLayout_anim.cancel();
        }
        
        refreshLayout_anim = ValueAnimator
                .ofInt(refreshLayout.getPaddingTop(), paddingTop)
                .setDuration(400);
        refreshLayout_anim.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator anim) {
                setHeaderPadding((Integer) anim.getAnimatedValue());
            }
        });
        refreshLayout_anim.start();
    }

    private void setHeaderPadding(int paddingTop) {
        refreshLayout.setPadding(
                refreshLayout.getPaddingLeft(), 
                paddingTop, 
                refreshLayout.getPaddingRight(), 
                refreshLayout.getPaddingBottom());
    }
    
    private void applyHeaderPadding(MotionEvent ev) {
        int historySize = ev.getHistorySize();
        int pointerCount = ev.getPointerCount();
        
        for (int h = 0; h < historySize; h++)
        {
            for (int p = 0; p < pointerCount; p++)
            {
                int historicalY = (int) ev.getHistoricalY(p, h);
                // Calculate the padding to apply, we divide by 1.7 to
                // simulate a more resistant effect during pull.
                int topPadding = (int) ((historicalY - touchY) / 1.7f);
                setHeaderPadding(refreshLayout_originalTopPadding - 
                        refreshLayout_height + topPadding);
            }
        }
    }

    private void prepareForRefresh() {
        animSetHeaderPadding(refreshLayout_originalTopPadding);
        
        refreshArrow.setVisibility(GONE);
        refreshArrow.clearAnimation();
        refreshProgress.setVisibility(VISIBLE);
        // Set refresh view text to the refreshing label
        refreshText.setText(R.string.refresh_progress);
        
        refreshState = REFRESHING;
        if (refreshListener != null) refreshListener.onRefresh();
    }
    
    /**
     * Resets the list to a normal state after a refresh.
     */
    private void onRefreshComplete() {
        resetHeader(true);
        
        refreshText.setText(R.string.refresh_completed);
        refreshProgress.setVisibility(GONE);
    }
    
    private void onLoad(boolean loading) {
        if (isLoading = loading)
        {
            loadText.setText(R.string.refresh_loading);
            if (loadListener != null) loadListener.onLoad();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollListener != null) scrollListener.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (scrollListener != null) scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        if (isLoadable && !isLoading && firstVisibleItem + visibleItemCount >= totalItemCount)
        {
            onLoad(true);
        }
    }

    /**
     * Interface definition for a callback to be invoked 
     * when list should be refreshed.
     */
    public interface OnRefreshListener {
        
        /**
         * Called when the list should be refreshed in UI thread
         */
        void onRefresh();
    }

    /**
     * Interface definition for a callback to be invoked
     * when list should be loaded.
     */
    public interface OnLoadListener {

        /**
         * Called when the list should be loaded in UI thread
         */
        void onLoad();
    }
}