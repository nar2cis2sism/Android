package engine.android.framework;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Looper;

import engine.android.core.ApplicationManager;

/**
 * 提供一个全局上下文供访问
 * 
 * @author Daimon
 */
public class MyContext {

    private static final Context context
    = ApplicationManager.getApplicationManager();

    public static Context getContext() {
        return context;
    }

    public static AssetManager getAssets() {
        return context.getAssets();
    }

    public static Resources getResources() {
        return context.getResources();
    }

    public static PackageManager getPackageManager() {
        return context.getPackageManager();
    }

    public static ContentResolver getContentResolver() {
        return context.getContentResolver();
    }

    public static Looper getMainLooper() {
        return context.getMainLooper();
    }
}