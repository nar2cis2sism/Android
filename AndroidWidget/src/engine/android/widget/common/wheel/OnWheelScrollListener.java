package engine.android.widget.common.wheel;

/**
 * Wheel scrolled listener interface.
 * 
 * @author Daimon
 * @since 8/21/2012
 */
public interface OnWheelScrollListener {
    
    /**
     * Callback method to be invoked when scrolling started
     * 
     * @param wheel the wheel view whose state has changed
     */
    void onScrollingStarted(WheelView wheel);
    
    /**
     * Callback method to be invoked when scrolling finished
     * 
     * @param wheel the wheel view whose state has changed
     */
    void onScrollingFinished(WheelView wheel);
}