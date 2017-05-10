package engine.android.framework.app;

import engine.android.core.ApplicationManager;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.ui.util.ImageManager;

/**
 * 应用程序入口
 * 
 * @author Daimon
 */
public class App extends ApplicationManager {
    
    private static AppGlobal app;
    
    protected static AppGlobal global() {
        if (app == null) app = AppGlobal.get(getMainApplication());
        return app;
    }
    
    public static AppConfig getConfig() {
        return global().getConfig();
    }
    
    public static HttpManager getHttpManager() {
        return global().getHttpManager();
    }
    
    public static SocketManager getSocketManager() {
        return global().getSocketManager();
    }
    
    public static ImageManager getImageManager() {
        return global().getImageManager();
    }
}