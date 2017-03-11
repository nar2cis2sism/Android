package com.project.app.storage;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DBUpdateListener;

public class DAOManager implements DBUpdateListener {

    /******************************* 数据库配置 *******************************/
    
    public static final String DB_NAME = "project.db";
    public static final int DB_VERSION = 1;

    @Override
    public void onCreate(DAOTemplate dao) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onUpdate(DAOTemplate dao, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        
    }
}