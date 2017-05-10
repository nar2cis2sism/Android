package engine.android.framework.app;

import android.content.Context;

import java.util.HashMap;

import engine.android.core.ApplicationManager;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.ui.util.ImageManager;
import engine.android.util.StringUtil;

/**
 * 提供应用程序公用的功能组件
 * 
 * @author Daimon
 */
public abstract class AppGlobal {
    
    private static final HashMap<Context, AppGlobal> map
    = new HashMap<Context, AppGlobal>();
    
    public static final void config(AppConfig config) {
        getAppGlobal(config.getContext()).setConfig(config);
    }
    
    public static AppGlobal get(Context context) {
        return getAppGlobal(context.getApplicationContext());
    }
    
    private static AppGlobal getAppGlobal(Context appContext) {
        AppGlobal base = getDefault();
        
        AppGlobal app = map.get(appContext);
        if (app == null)
        {
            map.put(appContext, app = new AppGlobalWrapper(base));
        }
        
        return app;
    }
    
    private static AppGlobal getDefault() {
        Context context = ApplicationManager.getMainApplication();
        AppGlobal app = map.get(context);
        if (app == null)
        {
            map.put(context, app = new AppGlobalImpl(context));
        }
        
        return app;
    }
    
    abstract void setConfig(AppConfig config);
    
    public abstract AppConfig getConfig();
    
    public abstract HttpManager getHttpManager();
    
    public abstract SocketManager getSocketManager();
    
    public abstract ImageManager getImageManager();
}

class AppGlobalWrapper extends AppGlobal {
    
    private final AppGlobal mBase;
    private AppGlobal real;
    
    public AppGlobalWrapper(AppGlobal base) {
        real = mBase = base;
    }

    @Override
    void setConfig(AppConfig config) {
        if (real == mBase)
        {
            real = new AppGlobalImpl(config.getContext());
            real.setConfig(config);
        }
        
        throw new RuntimeException(StringUtil.format(
                "App Config of %s is exist.", real.getConfig().getContext().getPackageName()));
    }

    @Override
    public AppConfig getConfig() {
        return real.getConfig();
    }

    @Override
    public HttpManager getHttpManager() {
        return real.getHttpManager();
    }

    @Override
    public SocketManager getSocketManager() {
        return real.getSocketManager();
    }

    @Override
    public ImageManager getImageManager() {
        return real.getImageManager();
    }
}

class AppGlobalImpl extends AppGlobal {
    
    private final Context context;
    
    private AppConfig config;
    
    private HttpManager http;
    
    private SocketManager socket;
    
    private ImageManager image;
    
    public AppGlobalImpl(Context context) {
        this.context = context;
    }

    @Override
    void setConfig(AppConfig config) {
        if (this.config != null)
        {
            throw new RuntimeException(StringUtil.format(
                    "App Config of %s is exist.", context.getPackageName()));
        }
        
        this.config = config;
    }

    @Override
    public AppConfig getConfig() {
        if (config == null) config = new AppConfig(context);
        return config;
    }

    @Override
    public HttpManager getHttpManager() {
        if (http == null) http = new HttpManager(context);
        return http;
    }

    @Override
    public SocketManager getSocketManager() {
        if (socket == null) socket = new SocketManager(context);
        return socket;
    }

    @Override
    public ImageManager getImageManager() {
        if (image == null) image = new ImageManager(context);
        return image;
    }
}