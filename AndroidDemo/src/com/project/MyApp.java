package com.project;

import android.os.StrictMode;

import com.project.MyConfiguration.MyConfiguration_APP;
import com.project.MyConfiguration.MyConfiguration_NET;
import com.project.util.LogUploader;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.util.AndroidUtil;

public class MyApp extends ApplicationManager implements MyConfiguration_APP {

    @Override
    public void onCreate() {
        super.onCreate();

        if (isDebuggable() && AndroidUtil.getVersion() >= 11)
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

        LogFactory.enableLOG(true);

        LOG.log(getCurrentStackFrame(), MyConfiguration_NET.NET_OFF ? "单机版" : "网络版");

        traceActivityStack(isDebuggable());

        init();
    }

    private void init() {
        MyInitial.init(this);
    }

    @Override
    protected void doExit() {
        releaseResource();

        if (APP_TESTING)
        {
            uploadLog();
        }
    }

    private void releaseResource() {}

    @Override
    protected boolean handleException(Throwable ex) {
        uploadLog();

        return super.handleException(ex);
    }

    private void uploadLog() {
        LogUploader.uploadLog();
    }
}