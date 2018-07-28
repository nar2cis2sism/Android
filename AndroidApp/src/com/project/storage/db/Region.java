package com.project.storage.db;

import android.provider.BaseColumns;

import com.project.storage.RegionDataBase;

import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;

/**
 * 地区
 */
@DAOTable(name=RegionDataBase.TABLE)
public class Region implements RegionColumns {
    
    @DAOPrimaryKey(column=_ID)
    public int id;                          // 地区ID

    @DAOProperty(column=NAME)
    public String name;                     // 地区名称

    @DAOProperty(column=REGION_CODE)
    public String code;                     // 区域编码
}

interface RegionColumns extends BaseColumns {

    String NAME = "name";
    String REGION_CODE = "region_code";
}