package engine.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import engine.android.util.ui.ButtonGroup;
import engine.android.util.ui.ButtonGroup.OnCheckedChangeListener;

/**
 * 页面指示器
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class PageIndicator extends LinearLayout {
    
    private LayoutInflater inflater;
    
    private ButtonGroup group;

    public PageIndicator(Context context) {
        super(context);
        init(context);
    }
    
    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator);
        int indicator_count = a.getInteger(R.styleable.PageIndicator_indicator_count, 0);
        
        a.recycle();
        
        if (indicator_count > 0)
        {
            for (int i = 0; i < indicator_count; i++)
            {
                RadioButton indicator = (RadioButton) inflater.inflate(
                        R.layout.page_indicator_item, this, false);
                addViewInLayout(indicator, -1, indicator.getLayoutParams(), true);
                group.add(indicator);
            }
        }
    }
    
    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        group = new ButtonGroup();
    }

    public void setIndicatorCount(int count) {
        if (count == getChildCount())
        {
            return;
        }
        
        removeAllViews();
        for (int i = 0; i < count; i++)
        {
            RadioButton indicator = (RadioButton) inflater.inflate(
                    R.layout.page_indicator_item, this, false);
            addView(indicator);
            group.add(indicator);
        }
    }
    
    public void check(int index) {
        group.check(index);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        group.setOnCheckedChangeListener(listener);
    }
}