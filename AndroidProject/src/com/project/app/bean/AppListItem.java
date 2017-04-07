package com.project.app.bean;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppListItem {
    
    public final ApplicationInfo appInfo;
    
    public final Drawable icon;
    public final String label;
    
    public boolean isDisabled;
    
    public AppListItem(PackageManager pm, PackageInfo packageInfo) {
        appInfo = packageInfo.applicationInfo;
        icon = getIcon(pm);
        label = getLabel(pm);
    }
    
    private Drawable getIcon(PackageManager pm) {
        return appInfo.loadIcon(pm);
    }
    
    private String getLabel(PackageManager pm) {
        CharSequence cs = appInfo.loadLabel(pm);
        return cs != null ? cs.toString() : appInfo.packageName;
    }
}