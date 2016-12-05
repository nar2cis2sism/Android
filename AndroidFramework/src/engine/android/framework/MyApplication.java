package engine.android.framework;

import android.os.StrictMode;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.MyConfiguration.MyConfiguration_APP;
import engine.android.framework.MyConfiguration.MyConfiguration_NET;
import engine.android.framework.util.LogUploader;
import engine.android.util.AndroidUtil;

public class MyApplication extends ApplicationManager implements 
MyConfiguration_APP {

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

        LOG.log(MyConfiguration_NET.NET_OFF ? "单机版" : "网络版");

        traceActivityStack(isDebuggable());
    }

    @Override
    protected void doExit() {
        if (APP_TESTING)
        {
            LogUploader.uploadLog();
        }
    }

    @Override
    protected boolean handleException(Throwable ex) {
        LogUploader.uploadLog();

        return super.handleException(ex);
    }
}