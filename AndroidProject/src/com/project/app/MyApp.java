package com.project.app;

import com.project.app.MyConfiguration.MyConfiguration_HTTP;
import com.project.app.MyConfiguration.MyConfiguration_NETWORK;
import com.project.app.MyConfiguration.MyConfiguration_SOCKET;
import com.project.network.http.HttpInterceptor;
import com.project.network.http.servlet.HttpServlet;
import com.project.network.socket.SocketInterceptor;
import com.project.network.socket.SocketServlet;

import engine.android.framework.app.App;
import engine.android.plugin.PluginManager;

public class MyApp extends App implements
MyConfiguration_NETWORK, 
MyConfiguration_HTTP, 
MyConfiguration_SOCKET {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            PluginManager.init();
            PluginManager.getInstance().loadPluginFromAssets("AndroidBeside.apk", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected AppConfig initAppConfig() {
        return new AppConfig();
    }
    
    private static class AppConfig extends engine.android.framework.app.AppConfig {
        
        public AppConfig() {
            configNetwork(configNetwork());
            configHttp(configHttp());
            configSocket(configSocket());
        }
        
        private void configNetwork(NetworkConfig config) {
            if (isDebuggable()) config.setOffline(NET_OFF).setProtocolLog(NET_LOG_PROTOCOL);
        }
        
        private void configHttp(HttpConfig config) {
            config.setServlet(new HttpServlet());
            config.setInterceptor(new HttpInterceptor());
            config.setTimeout(HTTP_TIMEOUT);
        }
        
        private void configSocket(SocketConfig config) {
            config.setServlet(new SocketServlet());
            config.setInterceptor(new SocketInterceptor());
            config.setTimeout(SOCKET_TIMEOUT);
        }
    }
}