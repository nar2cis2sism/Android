package engine.android.widget.common.layout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 自适应软键盘（不被挤压）
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class KeyboardLayout extends FrameLayout {
    
    private final Rect outRect = new Rect();

    private int originHeight;

    public KeyboardLayout(Context context) {
        super(context);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getWindowVisibleDisplayFrame(outRect);
        int displayHeight = outRect.height();
        if (originHeight > displayHeight)
        {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(displayHeight, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (originHeight == 0) originHeight = getMeasuredHeight();
    }
}