package com.project.network;

import com.project.app.MyConfiguration;

/**
 * 网络通讯配置
 * 
 * @author Daimon
 */
public class NetworkConfig implements URLConfig {
    
    public static final String HTTP_URL = MyConfiguration.NET_TEST ? URL_TEST : URL_PRODUCT;
}

interface URLConfig {
    
    String URL_PRODUCT  = "http://123.56.120.144:8080/ysService/BaseServlet";
    
    String URL_TEST     = "http://10.57.145.170:8080/AppServer/app";
}