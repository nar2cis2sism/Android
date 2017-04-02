package engine.android.framework.app;

import android.content.Context;

import java.util.HashMap;

import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.ui.util.ImageManager;

/**
 * 提供应用程序公用的功能组件
 * 
 * @author Daimon
 */
public class AppGlobal {
    
    private static final HashMap<Context, AppGlobal> map
    = new HashMap<Context, AppGlobal>();
    
    public static AppGlobal get(Context context) {
        context = context.getApplicationContext();
        AppGlobal app = map.get(context);
        if (app == null)
        {
            map.put(context, app = new AppGlobal(context));
        }
        
        return app;
    }
    
    public static AppConfig getConfig(Context context) {
        return get(context).getConfig();
    }
    
    private final Context context;
    
    private AppConfig config;
    
    private AppGlobal(Context context) {
        this.context = context;
    }
    
    public AppConfig getConfig() {
        if (config == null) config = new AppConfig();
        return config;
    }
    
    public void setConfig(AppConfig config) {
        if (this.config != null)
        {
            throw new RuntimeException("Config is exist.");
        }
        
        this.config = config;
    }
    
    private HttpManager http;
    
    private SocketManager socket;
    
    private ImageManager image;
    
    public HttpManager getHttpManager() {
        if (http == null) http = new HttpManager(context);
        return http;
    }
    
    public SocketManager getSocketManager() {
        if (socket == null) socket = new SocketManager(context);
        return socket;
    }
    
    public ImageManager getImageManager() {
        if (image == null) image = new ImageManager(context);
        return image;
    }
}