package com.project.app;

import engine.android.core.ApplicationManager;
import engine.android.framework.app.AppGlobal;

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
    
//    @Override
//    public void onCreate() {
//        // 配置环境变量
//        AppGlobal.config(new MyConfiguration(this));
//        // 设置调试模式
//        if (isDebuggable(this) && AndroidUtil.getVersion() >= 11) AndroidUtil.setupStrictMode();
//        // 开启日志
//        LogFactory.enableLOG(true);
//        
////        LOG.log(getConfig().isOffline() ? "单机版" : "网络版");
//    }
//    
//    @Override
//    protected boolean handleException(Throwable ex) {
//        LogUploader.uploadLog();
//        return false;
//    }
}