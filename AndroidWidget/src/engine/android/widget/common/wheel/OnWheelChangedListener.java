package engine.android.widget.common.wheel;

/**
 * Wheel changed listener interface.
 * 
 * @author Daimon
 * @version N
 * @since 8/21/2012
 */
public interface OnWheelChangedListener {
    
    /**
     * Callback method to be invoked whenever current item is changed
     * 
     * @param wheel the wheel view whose state has changed
     * @param oldValue the old value of current item
     * @param newValue the new value of current item
     */
    void onChanged(WheelView wheel, int oldValue, int newValue);
}