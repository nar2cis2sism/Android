package com.project.app;

import com.project.storage.MyDAOManager;
import com.project.storage.MySharedPreferences;

public class MyInitial {

    /**
     * Run in background thread.
     */
    public static void init() {
//        reset();
        
        initDatabase();

        MySession.initialize();
    }

    /**
     * 这里通过第一次访问SQLiteDatabase创建或更新数据库
     */
    private static void initDatabase() {
        MyDAOManager.getDAO().getDataBase();
    }
    
    private static void reset() {
        MyDAOManager.getDAO().deleteSelf();
        MySharedPreferences.getInstance().reset();
    }
}