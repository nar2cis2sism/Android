package engine.android.widget.common.wheel;

/**
 * An WheelAdapter object acts as a bridge between a {@link WheelView} and the
 * underlying data for that view. The Adapter provides access to the data items.
 * 
 * @author Daimon
 * @since 8/21/2012
 * 
 * @see ArrayWheelAdapter
 * @see NumericWheelAdapter
 */
public interface WheelAdapter {
    
    /**
     * How many items are in the data set represented by this Adapter.
     * 
     * @return Count of items.
     */
    int getCount();
    
    /**
     * Get the data item associated with the specified position in the data set.
     * 
     * @param position Position of the item whose data we want within the adapter's data set.
     * @return The data text at the specified position.
     */
    String getItem(int position);
    
    /**
     * Gets maximum item length. It is used to determine the wheel width. 
     * If -1 is returned there will be used the default wheel width.
     * 
     * @return the maximum item length or -1
     */
    int getMaximumLength();
}