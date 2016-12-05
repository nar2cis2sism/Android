package com.project;

import engine.android.core.ApplicationManager;
import engine.android.core.Session;

public class MySession {

    private static final Session session = ApplicationManager.getSession();

    /******************************* 初始化 *******************************/
    private static final String INITIALIZED = "INITIALIZED";

    public static void initialize() {
        session.putAttribute(INITIALIZED);
    }

    public static boolean initialized() {
        return session.hasAttribute(INITIALIZED);
    }

    /******************************* 导航配置 *******************************/
    private static final String NAVIGATION = "NAVIGATION";
    
    public static void getNavigation() {
        session.putAttribute(NAVIGATION);
    }
    
    public static boolean hasNavigation() {
        return session.hasAttribute(NAVIGATION);
    }
}