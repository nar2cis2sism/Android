package engine.android.util.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 悬浮窗口控制
 *
 * @author Daimon
 * @since 3/26/2012
 */
public class FloatingWindow {

    private final View content;
    
    private final WindowManager wm;
    private final WindowManager.LayoutParams wl;
    
    private boolean isShown;
    
    public FloatingWindow(View view) {
        this(view, false);
    }
    
    /**
     * @param view 需要显示的窗口视图
     * @param system 系统窗口，需要声明权限
     * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     */
    public FloatingWindow(View view, boolean system) {
        Context context = (content = view).getContext();
        
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wl = new LayoutParams();
        if (system)
        {
            context = context.getApplicationContext();
            wl.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }
        else
        {
            wl.type = LayoutParams.TYPE_APPLICATION;
        }
        
        wl.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        wl.format = PixelFormat.TRANSLUCENT;
        // 以屏幕左上角为原点，设置x、y初始值
        wl.gravity = Gravity.LEFT | Gravity.TOP;
        wl.x = 0;
        wl.y = 0;
        // 设置悬浮窗口长宽数据
        wl.width = LayoutParams.WRAP_CONTENT;
        wl.height = LayoutParams.WRAP_CONTENT;
    }
    
    /**
     * 设置参数
     */
    public WindowManager.LayoutParams getLayoutParams() {
        return wl;
    }
    
    public void setType(int type) {
        wl.type = type;
    }
    
    public void setFullScreenMode() {
        wl.flags |= LayoutParams.FLAG_FULLSCREEN;
    }
    
    public void setDimMode(float dimAmount) {
        wl.flags |= LayoutParams.FLAG_DIM_BEHIND;
        wl.dimAmount = dimAmount;
    }
    
    public void setPosition(int gravity, int x, int y) {
        wl.gravity = gravity;
        wl.x = x;
        wl.y = y;
    }
    
    public void setSize(int width, int height) {
        wl.width = width;
        wl.height = height;
    }
    
    public final View getContentView() {
        return content;
    }
    
    public void show() {
        if (!isShown)
        {
            isShown = true;
            wm.addView(content, wl);
        }
    }
    
    public void update() {
        if (isShown)
        {
            wm.updateViewLayout(content, wl);
        }
    }
    
    public void hide() {
        if (isShown)
        {
            isShown = false;
            wm.removeView(content);
        }
    }
    
    public boolean isShown() {
        return isShown;
    }
}