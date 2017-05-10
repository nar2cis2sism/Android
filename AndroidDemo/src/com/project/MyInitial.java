package com.project;

import android.content.Context;

import com.project.storage.MyDAOManager;

public class MyInitial {

    /**
     * Run in background thread.
     */
    public static void init(Context context) {
        initDatabase(context);

        MySession.initialize();
    }

    /**
     * 这里通过第一次访问SQLiteDatabase创建或更新数据库
     */
    private static void initDatabase(Context context) {
        MyDAOManager.getDAO().getDataBase();
    }
}