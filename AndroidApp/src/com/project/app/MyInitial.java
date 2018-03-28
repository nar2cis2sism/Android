package com.project.app;

import com.project.storage.MyDAOManager;

/**
 * 应用程序初始化
 * 
 * @author Daimon
 */
public class MyInitial {

    /**
     * Run in background thread.
     */
    public static void init() {
        initDatabase();
    }

    /**
     * 这里通过第一次访问SQLiteDatabase创建或更新数据库
     */
    private static void initDatabase() {
//        MyDAOManager.getDAO().deleteSelf();
        MyDAOManager.getDAO().getDataBase();
    }
}