package engine.android.util.manager;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * 我的键盘管理器<br>
 * 需设置android:windowSoftInputMode="adjustResize"方有用
 * 
 * @author Daimon
 * @version N
 * @since 7/16/2013
 */
public class MyKeyboardManager implements OnGlobalLayoutListener {

    private final View decorView;
    private final int displayHeight;

    private boolean isKeyboardShown;
    private KeyboardListener listener;

    public MyKeyboardManager(Activity a) {
        decorView = a.getWindow().getDecorView();
        displayHeight = a.getResources().getDisplayMetrics().heightPixels;
    }

    public MyKeyboardManager(View decorView) {
        displayHeight = (this.decorView = decorView).getResources().getDisplayMetrics().heightPixels;
    }

    public boolean isKeyboardShown() {
        if (listener != null)
        {
            return isKeyboardShown;
        }

        return isKeyboardShown = detectKeyboardVisible();
    }

    public void setKeyboardListener(KeyboardListener listener) {
        decorView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        if ((this.listener = listener) != null)
        {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    private boolean detectKeyboardVisible() {
        Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        return outRect.top + outRect.height() < displayHeight;
    }

    public interface KeyboardListener {

        void keyboardChanged(boolean isKeyboardShown);
    }

    @Override
    public void onGlobalLayout() {
        if (isKeyboardShown ^ detectKeyboardVisible())
        {
            isKeyboardShown = !isKeyboardShown;
            if (listener != null)
            {
                listener.keyboardChanged(isKeyboardShown);
            }
        }
    }
}