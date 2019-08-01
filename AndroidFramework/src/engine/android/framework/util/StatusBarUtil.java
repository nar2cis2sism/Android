package engine.android.framework.util;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import engine.android.util.AndroidUtil;

public final class StatusBarUtil {
    
    public static void 沉浸式状态栏(Activity activity) {
        if (AndroidUtil.getVersion() >= 21)
        {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 让应用主题内容占用系统状态栏的空间
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 设置状态栏颜色为透明
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}