package com.project.app;

import com.project.storage.MyDAOManager;

public class MyInitial {

    /**
     * Run in background thread.
     */
    public static void init() {
        initDatabase();

        MySession.initialize();
    }

    /**
     * 这里通过第一次访问SQLiteDatabase创建或更新数据库
     */
    private static void initDatabase() {
        if (MyApp.isDebuggable())
        {
            MyDAOManager.getDAO().deleteSelf();
        }
        
        MyDAOManager.getDAO().getDataBase();
    }
}