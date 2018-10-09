package com.project.app;

import com.project.app.bean.ServerUrl;
import com.project.storage.db.User;

import engine.android.core.Session;
import protocol.http.NavigationData.AppUpgradeInfo;

public class MySession {

    private static final Session session = MyApp.getApp().getSession();

    /******************************* 初始化 *******************************/
    private static final String INITIALIZED = "INITIALIZED";

    public static void initialize() {
        session.putAttribute(INITIALIZED);
    }

    public static boolean initialized() {
        return session.hasAttribute(INITIALIZED);
    }

    /******************************* 服务器地址 *******************************/
    private static final String SERVER_URL = "SERVER_URL";

    public static void setServerUrl(ServerUrl url) {
        session.setAttribute(SERVER_URL, url);
    }
    
    public static ServerUrl getServerUrl() {
        return session.getAttribute(SERVER_URL, null);
    }

    /******************************* 升级信息 *******************************/
    private static final String APP_UPGRADE_INFO = "APP_UPGRADE_INFO";
    
    public static void setUpgradeInfo(AppUpgradeInfo info) {
        session.setAttribute(APP_UPGRADE_INFO, info);
    }
    
    public static AppUpgradeInfo getUpgradeInfo() {
        return session.getAttribute(APP_UPGRADE_INFO, null);
    }

    /******************************* 导航配置 *******************************/
    private static final String NAVIGATION = "NAVIGATION";
    
    public static void getNavigation() {
        session.putAttribute(NAVIGATION);
    }
    
    public static boolean hasNavigation() {
        return session.hasAttribute(NAVIGATION);
    }

    /******************************* 用户登录凭证 *******************************/
    private static final String TOKEN = "TOKEN";
    
    public static void setToken(String token) {
        session.setAttribute(TOKEN, token);
    }
    
    public static String getToken() {
        return session.getAttribute(TOKEN, null);
    }

    /******************************* 当前登录用户 *******************************/
    private static final String USER = "USER";
    
    public static void setUser(User user) {
        session.setAttribute(USER, user);
    }
    
    public static User getUser() {
        return session.getAttribute(USER, null);
    }
}