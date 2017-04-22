package com.project.app;

import com.project.storage.MyDAOManager;
import com.project.storage.MySharedPreferences;

import engine.android.plugin.Plugin;

public class MyInitial {

    /**
     * Run in background thread.
     */
    public static void init() {
//        reset();
        
        initDatabase();
        initBesidePlugin();

        MySession.initialize();
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
        try {
            Plugin.loadPluginFromAssets("AndroidBeside.apk", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void reset() {
        MyDAOManager.getDAO().deleteSelf();
        MySharedPreferences.getInstance().reset();
    }
}