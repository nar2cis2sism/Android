package engine.android.util.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 不能滑动的GridView
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class NoScrollGridView extends GridView {
    
    private boolean isScrollable;

    public NoScrollGridView(Context context) {
        super(context);
    }

    public NoScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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