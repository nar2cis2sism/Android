package demo.j2se.shell;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import engine.android.util.AndroidUtil;
import engine.android.util.file.FileUtils;

import java.io.File;

public class ProxyApplication extends Application {

    private static final String APPLICATION_CLASS_NAME = "APPLICATION_CLASS_NAME";
    
    private ApkLoader apkLoader;
    
    @Override
    public Resources getResources() {
        try {
            return AndroidUtil.getUninstalledAPKResources(this, apkLoader.getDexPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return super.getResources();
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("attachBaseContext");
        
        try {
            String apkName = "unshell.apk";
            
            File odex = getDir("unshell", MODE_PRIVATE);
            
            File apkFile = new File(odex, apkName);
            
            if (!FileUtils.copyToFile(getAssets().open(apkName), apkFile))
            {
                throw new Exception("Failed to copy file to:" + apkFile.getAbsolutePath());
            }
            
            apkLoader = new ApkLoader(apkFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //配置动态加载环境
        try {
            apkLoader.configClassLoader();
            apkLoader.configResourceLoader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("onCreate");

        try {
            String applicationClassName = null;
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey(APPLICATION_CLASS_NAME))
            {
                applicationClassName = bundle.getString(APPLICATION_CLASS_NAME);
            }
            
            if (applicationClassName != null)
            {
                apkLoader.configApplication(applicationClassName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}