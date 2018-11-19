package com.project.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Looper;

/**
 * 提供一个全局上下文供访问
 * 
 * @author Daimon
 */
public class MyContext {

    private static final Context context = MyApp.getApp();

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
    
    public static int getResourceIdentifier(String name, String type) {
        return getResources().getIdentifier(name, type, context.getPackageName());
    }
    
    public static Drawable getDrawable(String name) {
        return getResources().getDrawable(getResourceIdentifier(name, "drawable"));
    }
}