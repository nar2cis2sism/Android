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
    
    String URL_TEST     = "http://10.66.50.86:8080/AppServer/app";
    
    String URL_PRODUCT  = URL_TEST;
}