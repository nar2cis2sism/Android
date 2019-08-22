package engine.android.util.os;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 显示窗口函数工具
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public final class WindowUtil {

    /**
     * 设置全屏模式
     * 
     * @param noTitle 去掉标题栏
     */
    public static void setFullScreenMode(Window window, boolean noTitle) {
        window.setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        if (noTitle)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
    }

    /**
     * 设置横屏模式
     */
    public static void setLandscapeMode(Activity a) {
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 设置屏幕常亮
     */
    public static void setKeepScreenOn(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 获取状态栏高度（布局完成后才能获取到）
     */
    public static int getStatusBarHeight(Window window) {
        // 包括标题栏，不包括状态栏
        View decorView = window.getDecorView();
        // 状态栏以下的屏幕区域
        Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);

        return outRect.top;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Resources res) {
        int resId = res.getIdentifier("status_bar_height", "dimen", "android");
        return res.getDimensionPixelSize(resId);
    }

    /**
     * 获取标题栏高度
     */
    public static int getTitleBarHeight(Window window) {
        // 当前显示的View根（是一个FrameLayout对象，不包括标题栏）
        View root = window.findViewById(Window.ID_ANDROID_CONTENT);
        // 状态栏以下的屏幕区域
        Rect outRect = new Rect();
        root.getWindowVisibleDisplayFrame(outRect);

        return Math.max(0, outRect.height() - root.getHeight());
    }

    /**
     * 获取ActionBar的高度
     */
    public static int getActionBarHeight(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, outValue, true);
        return context.getResources().getDimensionPixelSize(outValue.resourceId);
    }

    /**
     * 获取背景图
     */
    public static Drawable getWindowBackground(Context context) {
        int[] attrs = { android.R.attr.windowBackground };

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attrs[0], outValue, true);

        TypedArray a = context.obtainStyledAttributes(outValue.resourceId, attrs);
        Drawable windowBackground = a.getDrawable(0);
        a.recycle();

        return windowBackground;
    }

    /**
     * 获取屏幕分辨率
     */
    public static DisplayMetrics getResolution(Activity a) {
        DisplayMetrics dm = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * @param 深色字体 设置状态栏字体的颜色为黑色/白色
     */
    public static boolean 沉浸式状态栏(Window window, boolean 深色字体) {
        if (VERSION.SDK_INT >= 23)
        {
            window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 让应用主题内容占用系统状态栏的空间
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | (深色字体 ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : View.SYSTEM_UI_FLAG_LAYOUT_STABLE));
            window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 设置状态栏颜色为透明
            window.setStatusBarColor(Color.TRANSPARENT);

            return true;
        }

        return false;
    }
}