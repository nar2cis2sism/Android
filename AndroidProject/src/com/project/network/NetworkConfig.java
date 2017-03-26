package com.project.network;

import com.project.app.MyConfiguration.MyConfiguration_NETWORK;

/**
 * 网络通讯配置
 * 
 * @author Daimon
 */
public class NetworkConfig {
    
    public static final String HTTP_URL = MyConfiguration_NETWORK.NET_TEST ? 
            URLConfig.URL_TEST : URLConfig.URL_PRODUCT;
    
    public interface URLConfig {
        
        String URL_PRODUCT  = "http://123.56.120.144:8080/ysService/BaseServlet";
        
        String URL_TEST     = "http://192.168.1.102:8080/AppServer/app";
    }
}