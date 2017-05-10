package com.project.app;

import android.os.StrictMode;

import com.project.util.LogUploader;

import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.app.AppGlobal;
import engine.android.util.AndroidUtil;

public class MyApp extends engine.android.framework.app.App {
    
    private static MyApp instance;
    
    public static MyApp getApp() {
        return instance;
    }
    
    public MyApp() { instance = this; }
    
    @Override
    public void onCreate() {
        // 配置环境变量
        AppGlobal.config(new MyConfiguration(this));
        // 设置调试模式
        setupStrictMode();
        // 开启日志
        LogFactory.enableLOG(true);
        
        LOG.log(getConfig().isOffline() ? "单机版" : "网络版");
    }
    
    @Override
    protected boolean handleException(Throwable ex) {
        LogUploader.uploadLog();
        return false;
    }
    
    private void setupStrictMode() {
        if (isDebuggable(this) && AndroidUtil.getVersion() >= 11)
        {
            // StrictMode.enableDefaults()有bug
            // (android.os.StrictMode$InstanceCountViolation:instance=2;limit=1)
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectCustomSlowCalls()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyLog()
            .penaltyDeathOnNetwork()
            .penaltyFlashScreen()
            .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .build());
        }
    }
}