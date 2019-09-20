package com.project.app.config;

import engine.android.framework.app.AppConfig;

import android.content.Context;

import com.project.app.MyApp;
import com.project.network.http.HttpInterceptor;
import com.project.network.http.servlet.HttpServlet;
import com.project.network.socket.SocketInterceptor;
import com.project.network.socket.SocketPushReceiver;
import com.project.network.socket.servlet.SocketServlet;

/**
 * 全局配置
 * 
 * @author Daimon
 */
interface IConfiguration {
    
    /** 单机不联网 **/
    boolean NET_OFF = true;

    /** 打印协议 **/
    boolean NET_LOG_PROTOCOL = true;
    
    /** 测试服务器 **/
    boolean NET_TEST = true;
    
    /** 上传日志到服务器 **/
    boolean NET_UPLOAD_LOG = !NET_OFF;
    
    int HTTP_TIMEOUT   = 10000; // (10s)
    
    int SOCKET_TIMEOUT = 5000;  // (5s)
}

public class MyConfiguration extends AppConfig implements IConfiguration {

    public MyConfiguration(Context context) {
        super(context);
        
        configNetwork(configNetwork());
        configHttp(configHttp());
        configSocket(configSocket());
        configImage(configImage());
    }
    
    private void configNetwork(NetworkConfig config) {
        if (MyApp.getApp().isDebuggable())
        {
            config.setOffline(NET_OFF).setProtocolLog(NET_LOG_PROTOCOL);
        }
    }
    
    private void configHttp(HttpConfig config) {
        config.setServlet(new HttpServlet());
        config.setInterceptor(new HttpInterceptor());
        config.setTimeout(HTTP_TIMEOUT);
    }
    
    private void configSocket(SocketConfig config) {
        config.setServlet(new SocketServlet());
        config.setPushReceiver(new SocketPushReceiver());
        config.setInterceptor(new SocketInterceptor());
        config.setTimeout(SOCKET_TIMEOUT);
    }
    
    private void configImage(ImageConfig config) {
        config.setTransformer(new ImageTransformer());
    }
}