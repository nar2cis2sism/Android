package com.project.network;

import com.project.app.config.MyConfiguration;

/**
 * 网络通讯配置
 * 
 * @author Daimon
 */
public class NetworkConfig implements URLConfig {
    
    /** 服务器地址 **/
    private static final String SERVER_URL = MyConfiguration.NET_TEST ? URL_TEST : URL_PRODUCT;
    /** App后台 **/
    public static final String HTTP_URL = SERVER_URL + "app";
    /** 日志上传 **/
    public static final String LOG_URL = SERVER_URL + "log";
}

interface URLConfig {

    /** 测试环境 **/
    String URL_TEST     = "http://192.168.43.28:8080/AppServer/";
    /** 生产环境 **/
    String URL_PRODUCT  = URL_TEST;
}