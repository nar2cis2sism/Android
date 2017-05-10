package demo.android.util;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.view.IWindowManager;

import engine.android.util.AndroidUtil;

import java.util.Locale;

public final class SystemUtil {
    
    /**
     * 使用隐藏API的例子
     */
    
    public static void changeLocale(Locale locale) {
        try {
            IActivityManager iam = ActivityManagerNative.getDefault();
            Configuration config = iam.getConfiguration();
            config.locale = locale;
            
            // 此处需要声明权限:android.permission.CHANGE_CONFIGURATION,会重新调用 onCreate();
            iam.updateConfiguration(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 判断是否是平板
     */
    
    public static boolean isTablet(Context context) {
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(
                    AndroidUtil.getServiceIBinder(Context.WINDOW_SERVICE));
            return !wm.canStatusBarHide();
        } catch (Throwable e) {
            // e.printStackTrace();
        }
        
        if (AndroidUtil.getVersion() >= 13)
        {
            System.out.println("swdp:" + context.getResources().getConfiguration()
                    .smallestScreenWidthDp);
            return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
        }
        
        // 不精确
        return context.getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
    }
}