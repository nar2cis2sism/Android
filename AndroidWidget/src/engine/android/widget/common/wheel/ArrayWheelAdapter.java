package engine.android.widget.common.wheel;

/**
 * The simple Array wheel adapter
 * 
 * @author Daimon
 * @since 8/21/2012
 */
public class ArrayWheelAdapter<T> implements WheelAdapter {
    
    /** The default items length */
    private static final int DEFAULT_LENGTH = -1;
    
    /** Contains the array of objects that represent the data of this ArrayWheelAdapter. **/
    private final T[] items;
    
    private final int length;
    
    public ArrayWheelAdapter(T[] items) {
        this(items, DEFAULT_LENGTH);
    }
    
    public ArrayWheelAdapter(T[] items, int length) {
        this.items = items;
        this.length = length;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public String getItem(int position) {
        if (position < 0 || position >= items.length)
        {
            return null;
        }
        
        return items[position].toString();
    }

    @Override
    public int getMaximumLength() {
        return length;
    }
}