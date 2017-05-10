package demo.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public class CheckableItem extends FrameLayout implements Checkable {

    private boolean isChecked;

    public CheckableItem(Context context) {
        super(context);
    }

    public CheckableItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setChecked(boolean checked) {
        if (isChecked ^ checked)
        {
            setBackgroundDrawable((isChecked = checked) ? new ColorDrawable(0xff30ABD5) : null);
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
}