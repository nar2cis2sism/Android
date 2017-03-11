package com.project.app;

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
//        if (MyConfiguration_APP.APP_TESTING)
//        {
//            MyDAOManager.getDAO().deleteSelf();
//        }
//        
//        MyDAOManager.getDAO().getDataBase();
    }
}