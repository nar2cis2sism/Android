package engine.android.util.ui;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

/**
 * 弹出窗口控制
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public class MyPopupWindow extends PopupWindow implements Runnable {

    private View anchor;

    private int gravity;

    public MyPopupWindow(View contentView) {
        super(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        setBackgroundDrawable(new ColorDrawable());
        setOutsideTouchable(focusable);
    }

    public void setAnchor(View anchor, int gravity) {
        this.anchor = anchor;
        this.gravity = gravity;
    }

    public View getAnchor() {
        return anchor;
    }

    /**
     * @param hideDelayed 显示时间过后消失
     */
    public void showAtLocation(int x, int y, long hideDelayed) {
        if (isShowing())
        {
            getContentView().removeCallbacks(this);
            update(x, y, -1, -1);
        }
        else
        {
            showAtLocation(anchor, gravity, x, y);
        }

        if (hideDelayed > 0)
        {
            getContentView().postDelayed(this, hideDelayed);
        }
    }

    @Override
    public void run() {
        super.dismiss();
    }

    @Override
    public void dismiss() {
        getContentView().removeCallbacks(this);
        super.dismiss();
    }
}