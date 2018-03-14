package com.project.app.bean;

import com.project.app.MySession;

public class ServerUrl {
    
    public String socket_server_url;        // Socket服务器地址
    public String upload_server_url;        // 文件上传服务器地址
    public String download_server_url;      // 文件下载服务器地址
    
    public static String getSocketServerUrl() {
        ServerUrl url = MySession.getServerUrl();
        return url == null ? null : url.socket_server_url;
    }
}