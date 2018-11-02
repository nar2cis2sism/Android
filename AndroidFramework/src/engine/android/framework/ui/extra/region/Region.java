package engine.android.framework.ui.extra.region;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.io.File;
import java.io.FileOutputStream;

import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;
import engine.android.util.manager.SDCardManager;

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

    public static final String DATABASE = "region.db";
    public static final String TABLE = "area";
    
    public static final SQLiteDatabase open(Context context) {
        return loadDB(context, DATABASE);
    }
    
    private static SQLiteDatabase loadDB(Context context, String name) {
        File db_file = new File(SDCardManager.openSDCardAppDir(context), name);
        
        if (!db_file.exists())
        {
            FileManager.createFileIfNecessary(db_file);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(db_file);
                IOUtil.writeStream(Region.class.getResourceAsStream(name), fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeSilently(fos);
            }
        }
        
        return SQLiteDatabase.openOrCreateDatabase(db_file, null);
    }
}