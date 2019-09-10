package engine.android.widget.common.layout;

import engine.android.widget.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * 流式布局
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class FlowLayout extends ViewGroup {
    
    private int numColumns;
    private int horizontalSpacing;
    private int verticalSpacing;
    private int gravity;
    
    private int rows;
    private int itemWidth;
    private int itemHeight;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        
        numColumns = a.getInteger(R.styleable.FlowLayout_android_numColumns, 1);
        horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_android_horizontalSpacing, 0);
        verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_android_verticalSpacing, 0);
        int index = a.getInt(R.styleable.FlowLayout_android_gravity, 0);
        if (index > 0) setGravity(index);
        
        a.recycle();
    }
    
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }
    
    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }
    
    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }
    
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() == 0)
        {
            rows = 0;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        else
        {
            rows = (getChildCount() - 1) / numColumns + 1;
            measureWidthAndHeight(widthMeasureSpec, heightMeasureSpec);
        }
    }
    
    private void measureWidthAndHeight(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int itemWidth = widthSize - getPaddingLeft() - getPaddingRight();
        itemWidth -= (numColumns - 1) * horizontalSpacing;
        itemWidth /= numColumns;
        
        int itemHeight = heightSize - getPaddingTop() - getPaddingBottom();
        itemHeight -= (rows - 1) * verticalSpacing;
        itemHeight /= rows;
        
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(itemWidth, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(itemHeight, heightMode);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        
        int width = widthSize;
        int height = heightSize;
        if (widthMode != MeasureSpec.EXACTLY)
        {
            // 重新计算
            itemWidth = getChildAt(0).getMeasuredWidth();
            width = itemWidth * numColumns;
            width += (numColumns - 1) * horizontalSpacing;
            width += getPaddingLeft() + getPaddingRight();
            width = Math.min(width, widthSize);
        }

        if (heightMode != MeasureSpec.EXACTLY)
        {
            // 重新计算
            itemHeight = getChildAt(0).getMeasuredHeight();
            height = itemHeight * rows;
            height += (rows - 1) * verticalSpacing;
            height += getPaddingTop() + getPaddingBottom();
            height = Math.min(height, heightSize);
        }

        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        final LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, 0, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, 0, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        if (rows == 0) return;
        
        int childLeft = 0;
        int childTop = 0;
        for (int i = 0, childCount = getChildCount(); i < childCount; i++)
        {
            if (i % numColumns == 0)
            {
                childLeft = getChildLeft(i / numColumns, childCount);
                if (i != 0)
                {
                    childTop += itemHeight + verticalSpacing;
                }
                else
                {
                    childTop = getChildTop();
                }
            }
            else
            {
                childLeft += itemWidth + horizontalSpacing;
            }
            
            getChildAt(i).layout(childLeft, childTop, childLeft + itemWidth, childTop + itemHeight);
        }
    }
    
    private int getChildLeft(int row, int childCount) {
        if (childCount < numColumns * (row + 1))
        {
            int gravity = this.gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            switch (gravity) {
                case Gravity.CENTER_HORIZONTAL:
                    return (getWidth() - getChildWidth(childCount)) / 2;
                case Gravity.RIGHT:
                    return getWidth() - getChildWidth(childCount) - getPaddingRight();
            }
        }
        
        return getPaddingLeft();
    }
    
    private int getChildWidth(int childCount) {
        int num = childCount % numColumns;
        return num * itemWidth + (num - 1) * horizontalSpacing;
    }
    
    private int getChildTop() {
        int childHeight = rows * itemHeight + (rows - 1) * verticalSpacing;
        if (childHeight + getPaddingTop() + getPaddingBottom() < getHeight())
        {
            int gravity = this.gravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (gravity) {
                case Gravity.CENTER_VERTICAL:
                    return (getHeight() - childHeight) / 2;
                case Gravity.BOTTOM:
                    return getHeight() - childHeight - getPaddingBottom();
            }
        }
        
        return getPaddingTop();
    }
}