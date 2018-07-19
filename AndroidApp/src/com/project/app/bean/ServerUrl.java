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
    
    public static String getUploadServerUrl() {
        ServerUrl url = MySession.getServerUrl();
        return url == null ? null : url.upload_server_url;
    }
    
    public static String getDownloadServerUrl() {
        ServerUrl url = MySession.getServerUrl();
        return url == null ? null : url.download_server_url;
    }
    
    /**
     * 获取文件下载地址
     * 
     * @param url 文件相对地址
     */
    public static String getDownloadUrl(String url) {
        return getDownloadServerUrl() + url;
    }
}