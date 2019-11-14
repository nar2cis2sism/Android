package com.project.app;

import engine.android.core.ApplicationManager;
import engine.android.core.Injector;
import engine.android.core.util.LogFactory;
import engine.android.framework.app.AppGlobal;
import engine.android.util.AndroidUtil;
import engine.android.util.extra.ReflectObject;
import engine.android.util.manager.MySensorManager;
import engine.android.util.manager.MySensorManager.RotateSensorListener;

import android.app.Activity;
import android.os.StrictMode;

import com.project.app.config.MyConfiguration;
import com.project.app.service.AppService;
import com.project.widget.LogUploadDialog;
import com.project.widget.LogUploadDialog.LogUploadTask;

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
        // 7.0手机拍照不加这个会crash
        setupStrictMode();
        // 解决在Android P上的提醒弹窗
        //（Detected problems with API compatibility(visit g.co/dev/appcompat for more info)
        closeAndroidPDialog();
        // 配置注解
        if (!isDebuggable()) Injector.enableAptBuild();
        // 摇一摇上传日志
        if (isDebuggable())
        {
            MySensorManager sm = new MySensorManager(this);
            sm.addSensorListener(new ShakeSensorListener());
            sm.register();
        }
        // 开启日志
        LogFactory.enableLOG(true);
    }
    
    private static void setupStrictMode() {
        if (AndroidUtil.getVersion() < 11) return;
        // StrictMode.enableDefaults()有bug
        // (android.os.StrictMode$InstanceCountViolation:instance=2;limit=1)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectCustomSlowCalls()
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
    
    private static void closeAndroidPDialog() {
        if (AndroidUtil.getVersion() < 28) return;
        try {
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = ReflectObject.invokeStatic(cls, "currentActivityThread");
            ReflectObject ref_currentActivityThread = new ReflectObject(currentActivityThread);
            ref_currentActivityThread.set("mHiddenApiWarningShown", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected boolean handleException(Throwable ex) {
        new LogUploadTask().execute();
        return true;
    }
}
    
class ShakeSensorListener extends RotateSensorListener {

    long time;

    @Override
    public void notifyRotate(int rotateX, int rotateY, float speed) {
        if (speed > 250 && System.currentTimeMillis() - time > 2000)
        {
            showLogUploadDialog();
            time = System.currentTimeMillis();
        }
    }

    LogUploadDialog dialog;
    private void showLogUploadDialog() {
        if (dialog != null && dialog.isShowing())
        {
            return;
        }

        Activity activity = MyApp.getApp().currentActivity();
        if (activity != null)
        {
            (dialog = new LogUploadDialog(activity)).show();
        }
    }
}