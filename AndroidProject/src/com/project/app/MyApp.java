package com.project.app;

import com.project.app.MyConfiguration.MyConfiguration_HTTP;
import com.project.app.MyConfiguration.MyConfiguration_NETWORK;
import com.project.app.MyConfiguration.MyConfiguration_SOCKET;
import com.project.app.storage.DAOManager;
import com.project.network.http.HttpInterceptor;
import com.project.network.http.HttpServlet;
import com.project.network.socket.SocketInterceptor;
import com.project.network.socket.SocketServlet;

import engine.android.framework.app.App;

public class MyApp extends App implements
MyConfiguration_NETWORK, 
MyConfiguration_HTTP, 
MyConfiguration_SOCKET {
    
    @Override
    protected AppConfig initAppConfig() {
        return new AppConfig();
    }
    
    private static class AppConfig extends engine.android.framework.app.AppConfig {
        
        public AppConfig() {
            configNetwork(configNetwork());
            configHttp(configHttp());
            configSocket(configSocket());
            configDatabase(configDatabase());
        }
        
        private void configNetwork(NetworkConfig config) {
            if (isDebuggable()) config.setOffline(NET_OFF).setProtocolLog(NET_LOG_PROTOCOL);
        }
        
        private void configHttp(HttpConfig config) {
//            config.setServlet(new HttpServlet());
            config.setInterceptor(new HttpInterceptor());
            config.setTimeout(HTTP_TIMEOUT);
        }
        
        private void configSocket(SocketConfig config) {
//            config.setServlet(new SocketServlet());
            config.setInterceptor(new SocketInterceptor());
            config.setTimeout(SOCKET_TIMEOUT);
        }
        
        private void configDatabase(DatabaseConfig config) {
            config.setName(DAOManager.DB_NAME).setVersion(DAOManager.DB_VERSION).setListener(new DAOManager());
        }
    }
}