package engine.android.widget.common.layout;

import engine.android.widget.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * 可多选的控件
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class CheckableItem extends FrameLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    private boolean isBackgroundSet;
    private boolean isChecked;

    public CheckableItem(Context context) {
        super(context);
    }

    public CheckableItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        isBackgroundSet = true;
    }
    
    private void ensureBackgroundDrawable() {
        if (!isBackgroundSet)
        {
            setBackgroundResource(R.drawable.checkable_item);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked ^ checked)
        {
            isChecked = checked;
            ensureBackgroundDrawable();
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }
}