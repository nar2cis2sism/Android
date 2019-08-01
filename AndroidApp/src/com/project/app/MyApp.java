package com.project.app;

import com.project.app.config.MyConfiguration;
import com.project.util.LogUploader;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.framework.app.AppGlobal;
import engine.android.util.AndroidUtil;

/**
 * 应用程序入口
 * 
 * @author Daimon
 */
public class MyApp extends ApplicationManager {
    
    private static MyApp instance;
    private static AppGlobal global;
    
    public static final MyApp getApp() {
        return instance;
    }
    
    public static final AppGlobal global() {
        if (global == null) global = AppGlobal.get(instance);
        return global;
    }
    
    public MyApp() { instance = this; }
    
    @Override
    public void onCreate() {
        // 配置环境变量
        AppGlobal.config(new MyConfiguration(this));
        // 开启日志
        LogFactory.enableLOG(true);
        // 设置调试模式
        if (isDebuggable()) AndroidUtil.setupStrictMode();
        
//        LOG.log(getConfig().isOffline() ? "单机版" : "网络版");
    }
    
    @Override
    protected boolean handleException(Throwable ex) {
        LogUploader.upload(this);
        return false;
    }
}