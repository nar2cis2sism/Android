package engine.android.widget.common.layout;

import engine.android.widget.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 不超过给定尺寸的布局
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class MaxSizeLayout extends FrameLayout {
    
    private int maxWidth;
    private int maxHeight;

    public MaxSizeLayout(Context context) {
        this(context, null);
    }

    public MaxSizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaxSizeLayout);
        
        maxWidth = a.getDimensionPixelSize(R.styleable.MaxSizeLayout_android_maxWidth, maxWidth);
        maxHeight = a.getDimensionPixelSize(R.styleable.MaxSizeLayout_android_maxHeight, maxHeight);
        
        a.recycle();
    }
    
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
    
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(makeMeasureSpec(maxWidth, widthMeasureSpec), 
                        makeMeasureSpec(maxHeight, heightMeasureSpec));
    }
    
    private int makeMeasureSpec(int maxSize, int measuerSpec) {
        if (maxSize < 0)
        {
            maxSize = MeasureSpec.getSize(measuerSpec);
        }
        
        if (maxSize > 0)
        {
            measuerSpec = MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.AT_MOST);
        }
        
        return measuerSpec;
    }
}