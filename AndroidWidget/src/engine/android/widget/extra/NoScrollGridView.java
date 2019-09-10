package engine.android.widget.extra;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 不能滑动的GridView
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class NoScrollGridView extends GridView {
    
    private boolean isScrollable;

    public NoScrollGridView(Context context) {
        super(context);
    }

    public NoScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isScrollable)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}