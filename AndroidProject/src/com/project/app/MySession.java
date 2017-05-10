package com.project.app;

import engine.android.core.Session;
import protocol.java.json.AppUpgradeInfo;

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

    /******************************* socket服务器地址 *******************************/
    private static final String SOCKET_ADDRESS = "SOCKET_ADDRESS";

    public static void setSocketAddress(String address) {
        session.setAttribute(SOCKET_ADDRESS, address);
    }
    
    public static String getSocketAddress() {
        return session.getAttribute(SOCKET_ADDRESS, null);
    }

    /******************************* 导航配置 *******************************/
    private static final String NAVIGATION = "NAVIGATION";
    
    public static void getNavigation() {
        session.putAttribute(NAVIGATION);
    }
    
    public static boolean hasNavigation() {
        return session.hasAttribute(NAVIGATION);
    }

    /******************************* 升级提示 *******************************/
    private static final String APP_UPGRADE_INFO = "APP_UPGRADE_INFO";
    
    public static void setUpgradeInfo(AppUpgradeInfo info) {
        session.setAttribute(APP_UPGRADE_INFO, info);
    }
    
    public static AppUpgradeInfo getUpgradeInfo() {
        return session.getAttribute(APP_UPGRADE_INFO, null);
    }

    /******************************* 用户登录凭证 *******************************/
    private static final String TOKEN = "TOKEN";
    
    public static void setToken(String token) {
        session.setAttribute(TOKEN, token);
    }
    
    public static String getToken() {
        return session.getAttribute(TOKEN, null);
    }
}