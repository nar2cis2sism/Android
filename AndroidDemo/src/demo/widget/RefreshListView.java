package demo.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import demo.android.R;

import java.lang.reflect.Method;

/**
 * 下拉刷新列表
 * 
 * @author Daimon
 */

public class RefreshListView extends ListView {

    private static final int NONE = 0;
    private static final int PULL_TO_REFRESH = 1;
    private static final int RELEASE_TO_REFRESH = 2;
    private static final int REFRESHING = 3;

    private int refreshState = -1;

    private RotateAnimation pullAnim;
    private RotateAnimation releaseAnim;

    private LinearLayout refreshLayout;
    private TextView refreshText;
    private ImageView refreshImage;
    private ProgressBar refreshProgress;

    private OnRefreshListener refreshListener;

    private boolean isRefreshable;

    private int refreshLayout_originalTopPadding;
    private int refreshLayout_height;
    private int touchY;

    private ValueAnimator refreshLayout_anim;

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

        refreshLayout = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.refresh_list_header, null);
        refreshText = (TextView) refreshLayout.findViewById(R.id.refreshText);
        refreshImage = (ImageView) refreshLayout.findViewById(R.id.refreshImage);
        refreshProgress = (ProgressBar) refreshLayout.findViewById(R.id.refreshProgress);

        refreshLayout_originalTopPadding = refreshLayout.getPaddingTop();
        refreshLayout.measure(0, 0);
        refreshLayout_height = refreshLayout.getMeasuredHeight();

        addHeaderView(refreshLayout);

        resetHeader(false);
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isRefreshable || refreshState == REFRESHING)
        {
            return super.onTouchEvent(ev);
        }

        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (getFirstVisiblePosition() == 0)
                {
                    refreshImage.setVisibility(VISIBLE);

                    int bottom = refreshLayout.getBottom();
                    if (bottom < refreshLayout_height && refreshState != PULL_TO_REFRESH)
                    {
                        refreshText.setText(R.string.refresh_pull);
                        if (refreshState != NONE)
                        {
                            refreshImage.clearAnimation();
                            refreshImage.startAnimation(releaseAnim);
                        }

                        refreshState = PULL_TO_REFRESH;
                    }
                    else if (bottom >= refreshLayout_height && refreshState != RELEASE_TO_REFRESH)
                    {
                        refreshText.setText(R.string.refresh_release);
                        refreshImage.clearAnimation();
                        refreshImage.startAnimation(pullAnim);
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
                if (refreshState == RELEASE_TO_REFRESH)
                {
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

        return super.onTouchEvent(ev);
    }

    private void applyHeaderPadding(MotionEvent ev) {
        int historySize = ev.getHistorySize();

        // Workaround for getPointerCount() which is unavailable in 1.5
        // (it's always 1 in 1.5)
        int pointerCount = 1;
        try {
            Method m = MotionEvent.class.getDeclaredMethod("getPointerCount");
            pointerCount = (Integer) m.invoke(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int h = 0; h < historySize; h++)
        {
            for (int p = 0; p < pointerCount; p++)
            {
                int historicalY = 0;
                try {
                    // For Android > 2.0
                    Method m = MotionEvent.class.getDeclaredMethod("getHistoricalY", Integer.TYPE,
                            Integer.TYPE);
                    historicalY = ((Float) m.invoke(ev, p, h)).intValue();
                } catch (NoSuchMethodException e) {
                    // For Android < 2.0
                    historicalY = (int) (ev.getHistoricalY(h));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Calculate the padding to apply, we divide by 1.7 to
                // simulate a more resistant effect during pull.
                int topPadding = (int) ((historicalY - touchY) / 1.7);
                setHeaderPadding(refreshLayout_originalTopPadding - refreshLayout_height
                        + topPadding);
            }
        }
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
            animSetHeaderPadding(refreshLayout_originalTopPadding - refreshLayout_height);
        }
        else
        {
            setHeaderPadding(refreshLayout_originalTopPadding - refreshLayout_height);
        }
    }

    private void setHeaderPadding(int paddingTop) {
        refreshLayout.setPadding(
                refreshLayout.getPaddingLeft(),
                paddingTop,
                refreshLayout.getPaddingRight(),
                refreshLayout.getPaddingBottom());
    }

    private void animSetHeaderPadding(int paddingTop) {
        if (refreshLayout_anim != null)
        {
            refreshLayout_anim.cancel();
        }

        refreshLayout_anim = ValueAnimator.
                ofInt(refreshLayout.getPaddingTop(), paddingTop)
                .setDuration(400);
        refreshLayout_anim.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator anim) {
                setHeaderPadding((Integer) anim.getAnimatedValue());
            }
        });
        refreshLayout_anim.start();
    }

    private void prepareForRefresh() {
        animSetHeaderPadding(refreshLayout_originalTopPadding);

        refreshImage.setVisibility(GONE);
        // We need this hack, otherwise it will keep the previous drawable.
        refreshImage.setImageDrawable(null);
        refreshProgress.setVisibility(VISIBLE);

        // Set refresh view text to the refreshing label
        refreshText.setText(R.string.refresh_loading);

        refreshState = REFRESHING;

        new RefreshTask().execute();
    }

    /**
     * Interface definition for a callback to be invoked when list should be
     * refreshed.
     */

    public static interface OnRefreshListener {

        /**
         * Called when the list should be refreshed in another thread
         */

        public void doRefresh();
    }

    /**
     * start a thread to refresh list
     */

    private class RefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            onRefresh();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Call onRefreshComplete when the list has been refreshed.
            onRefreshComplete();
        }
    }

    void onRefresh() {
        if (refreshListener != null)
        {
            refreshListener.doRefresh();
        }
    }

    /**
     * Resets the list to a normal state after a refresh.
     */

    void onRefreshComplete() {
        resetHeader(true);

        refreshText.setText(R.string.refresh_completed);
        refreshImage.setImageResource(R.drawable.refresh_arrow);
        refreshImage.clearAnimation();
        refreshImage.setVisibility(GONE);
        refreshProgress.setVisibility(GONE);
    }
}