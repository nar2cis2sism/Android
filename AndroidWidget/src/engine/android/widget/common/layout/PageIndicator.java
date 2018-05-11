package engine.android.widget.common.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import engine.android.util.ui.ButtonGroup;
import engine.android.util.ui.ButtonGroup.OnCheckedChangeListener;
import engine.android.widget.R;

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
    
    private int layoutRes;

    public PageIndicator(Context context) {
        this(context, null);
    }
    
    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.PageIndicator);

        int count = a.getInteger(R.styleable.PageIndicator_count, 0);
        layoutRes = a.getResourceId(R.styleable.PageIndicator_layout, R.layout.page_indicator_item);
        
        a.recycle();
        
        if (count > 0)
        {
            for (int i = 0; i < count; i++)
            {
                CompoundButton indicator = (CompoundButton) inflater.inflate(layoutRes, this, false);
                addViewInLayout(indicator, -1, indicator.getLayoutParams(), true);
                group.add(indicator);
            }
        }
    }
    
    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        group = new ButtonGroup();
    }

    public void setCount(int count) {
        int childCount = getChildCount();
        if (count == childCount)
        {
            return;
        }
        
        if (childCount != 0)
        {
            removeAllViews();
            group = new ButtonGroup();
        }
        
        for (int i = 0; i < count; i++)
        {
            CompoundButton indicator = (CompoundButton) inflater.inflate(layoutRes, this, false);
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