package engine.android.plugin.proxy.component;

import android.app.Application;

import engine.android.plugin.PluginManager;

public class PluginApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        PluginManager.init();
    }
}