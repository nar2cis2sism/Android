package com.project.app;

import com.project.storage.MyDAOManager;


public class MyInitial {

    /**
     * Run in background thread.
     */
    public static void init() {
        initDatabase();
        initBesidePlugin();
    }

    /**
     * 这里通过第一次访问SQLiteDatabase创建或更新数据库
     */
    private static void initDatabase() {
        MyDAOManager.getDAO().getDataBase();
    }
    
    /**
     * 加载身边插件
     */
    private static void initBesidePlugin() {
//        try {
//            Plugin.loadPluginFromAssets("AndroidBeside.apk", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}