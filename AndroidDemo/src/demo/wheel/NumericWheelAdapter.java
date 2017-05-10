package demo.wheel;

import engine.android.util.StringUtil;

/**
 * Numeric wheel adapter
 * @author Daimon
 * @version 3.0
 * @since 8/21/2012
 */

public class NumericWheelAdapter implements WheelAdapter {
    
    /** The default min value */
    private static final int DEFAULT_MIN_VALUE = 0;

    /** The default max value */
    private static final int DEFAULT_MAX_VALUE = 9;
    
    //Values
    private int minValue;
    private int maxValue;
    
    //format
    private String format;
    
    public NumericWheelAdapter() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }
    
    public NumericWheelAdapter(int minValue, int maxValue) {
        this(minValue, maxValue, null);
    }
    
    public NumericWheelAdapter(int minValue, int maxValue, String format) {
        if (minValue > maxValue)
        {
            throw new IllegalArgumentException("minValue > maxValue");
        }
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
    }

    @Override
    public int getCount() {
        return maxValue - minValue + 1;
    }

    @Override
    public String getItem(int position) {
        if (position < 0 || position >= getCount())
        {
            return null;
        }
        
        int value = minValue + position;
        return getValueText(value);
    }

    @Override
    public int getMaximumLength() {
        return Math.max(StringUtil.getByteLength(getValueText(maxValue)), 
                        StringUtil.getByteLength(getValueText(minValue)));
    }
    
    private String getValueText(int value)
    {
        return format != null ? String.format(format, value) : Integer.toString(value);
    }
}