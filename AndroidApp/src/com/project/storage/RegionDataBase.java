package com.project.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.project.app.MyContext;

import engine.android.dao.DAOTemplate;
import engine.android.util.extra.Singleton;

/**
 * 中国行政区域数据库
 * 
 * @author Daimon
 */
public class RegionDataBase {

    public static final String TABLE = "area";
    
    private static final Singleton<RegionDataBase> instance
    = new Singleton<RegionDataBase>() {
        
        @Override
        protected RegionDataBase create() {
            return new RegionDataBase(MyContext.getContext());
        }
    };
    
    public static final SQLiteDatabase getDAO() {
        return instance.get().dao;
    }

    /**************************** 华丽丽的分割线 ****************************/
    
    private final SQLiteDatabase dao;
    
    private RegionDataBase(Context context) {
        dao = DAOTemplate.loadAssetsDB(context, "region.db");
    }
}