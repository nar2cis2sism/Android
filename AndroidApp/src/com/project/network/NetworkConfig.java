package com.project.network;

import com.project.app.config.MyConfiguration;

/**
 * 网络通讯配置
 * 
 * @author Daimon
 */
public class NetworkConfig implements URLConfig {
    
    public static final String HTTP_URL = MyConfiguration.NET_TEST ? URL_TEST : URL_PRODUCT;

    /** 日志上传 **/
    public static final String LOG_UPLOAD_URL = HTTP_URL + "/log";
}

interface URLConfig {
    
    String URL_TEST     = "http://192.168.1.101:8080/AppServer/app";
    
    String URL_PRODUCT  = URL_TEST;
}