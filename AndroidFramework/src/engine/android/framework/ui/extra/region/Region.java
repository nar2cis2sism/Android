package engine.android.framework.ui.extra.region;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import engine.android.dao.DAOTemplate;
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

/**
 * 中国行政区域数据库
 */
class RegionDataBase {

    public static final String TABLE = "area";
    
    public static final SQLiteDatabase open(Context context) {
        return new RegionDataBase(context).dao;
    }

    private final SQLiteDatabase dao;
    
    private RegionDataBase(Context context) {
        dao = DAOTemplate.loadAssetsDB(context, "region.db");
    }
}