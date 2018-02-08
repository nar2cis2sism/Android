package com.project.app;

import engine.android.core.Session;

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
}