package com.project;

import engine.android.core.ApplicationManager;
import engine.android.core.Session;

public class MySession {

    private static final Session session = ApplicationManager.getSession();

    /******************************* 闪屏初始化 *******************************/

    private static final String SESSION_INITIALIZE = "initialize";

    public static void initialize() {
        session.setAttribute(SESSION_INITIALIZE, true);
    }

    public static boolean initialized() {
        return session.hasAttribute(SESSION_INITIALIZE);
    }
}
