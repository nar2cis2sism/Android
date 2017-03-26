package engine.android.framework.app;

import android.os.StrictMode;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.util.LogUploader;
import engine.android.util.AndroidUtil;

/**
 * 应用程序入口
 * 
 * @author Daimon
 */
public class App extends ApplicationManager {
    
    private static AppGlobal app;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        (app = AppGlobal.get(this)).setConfig(initAppConfig());

        setupStrictMode();
        traceActivityStack(isDebuggable());
        LogFactory.enableLOG(true);
        
        LOG.log(getConfig().isOffline() ? "单机版" : "网络版");
    }
    
    protected AppConfig initAppConfig() {
        return new AppConfig();
    }

    @Override
    protected boolean handleException(Throwable ex) {
        LogUploader.uploadLog();
        return false;
    }
    
    private void setupStrictMode() {
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
    }
    
    public static final AppConfig getConfig() {
        return app.getConfig();
    }
    
    public static HttpManager getHttpManager() {
        return app.getHttpManager();
    }
    
    public static SocketManager getSocketManager() {
        return app.getSocketManager();
    }
}