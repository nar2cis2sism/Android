package demo.activity.test;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOClause;
import engine.android.dao.DAOTemplate.DAOClause.DAOParam;
import engine.android.dao.DAOTemplate.DAOListener;
import engine.android.dao.DAOTemplate.DAOQueryBuilder;
import engine.android.dao.DAOTemplate.DAOSQLBuilder;
import engine.android.dao.DAOTemplate.DAOTransaction;
import engine.android.dao.DAOTemplate.DBUpdateListener;
import engine.android.dao.DAOTemplate.DAOSQLBuilder.DAOExpression;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import engine.android.util.Util;
import engine.android.util.manager.SDCardManager;

import java.io.File;
import java.sql.Date;
import java.sql.Time;

public class TestOnDataBase extends TestOnBase implements DBUpdateListener, DAOListener {
    
    int VERSION = 1;
    
    DAOTemplate dao;
    
    long time;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dao = new DAOTemplate(this, getClass().getSimpleName(), VERSION, this);
        dao.registerListener(TestBean.class, this);

        recordTime();
        dao.resetTable(TestBean.class);
        log("resetTable");
        super.log("");

        testSave();
        super.log("");
        testRemove();
        super.log("");
        testEdit();
        super.log("");
        
        dao.close();
        VERSION = 2;
        dao = new DAOTemplate(this, getClass().getSimpleName(), VERSION, this);
        
        testFind();
        
        dao.unregisterListener(TestBean.class, this);
        dao.close();
        try {
            File exportDir = new File(SDCardManager.openSDCardAppDir(this), "databases");
            dao.export(exportDir);
            super.log("数据库文件导出到：" + exportDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dao.deleteSelf();
        
        showContent();
    }
    
    private void testSave() {
        final TestBean[] bean = new TestBean[50];
        for (int i = 0; i < bean.length; i++)
        {
            bean[i] = new TestBean(i);
        }
        
        recordTime();
        log("testSave:" + convert(dao.save(bean)));

        //如果一次存储数据太多会报异常，建议使用事务，且可以缩短执行时间
        recordTime();
        log("testTransaction:" + convert(dao.execute(new DAOTransaction() {
            
            @Override
            public boolean execute(DAOTemplate dao) throws Exception {
                //分批存储比在一条语句中执行更有效率
                for (TestBean b : bean)
                {
                    if (!dao.save(b))
                    {
                        return false;
                    }
                }
                
                return true;
            }
        })));
    }
    
    private void testRemove() {
        recordTime();
        log("testRemove:" + convert(dao.remove(DAOSQLBuilder.create(TestBean.class)
                .setWhereClause(DAOExpression.create("testboolean").equal(false)))));
    }
    
    private void testEdit() {
        recordTime();
        log("testEdit:" + convert(dao.edit(DAOSQLBuilder.create(TestBean.class)
                .setWhereClause(DAOExpression.create("id").equal(1)), new TestBean(99))));
    }
    
    private void testFind() {
        DAOQueryBuilder<TestBean> builder = DAOQueryBuilder.create(TestBean.class);

        recordTime();
        long num = dao.find(builder, Long.class);
        log("testFindCount:" + num);
        super.log("");

        recordTime();
        TestBean[] array = dao.find(builder, TestBean[].class);
        log("testFindArray:" + array.length);
        super.log("");

        recordTime();
        TestBean obj = dao.find(builder, TestBean.class);
        log("testFindObj:\n" + Util.toString(obj));
        super.log("");

        recordTime();
        Cursor cursor = dao.find(builder, Cursor.class);
        log("testFindCursor\n:" + DAOTemplate.printCursor(cursor, 0));
        super.log("");
    }

    @Override
    public void onCreate(DAOTemplate dao) {
        recordTime();
        dao.createTable(TestBean.class);
        dao.createIndex(TestBean.class, "testindex", 
                DAOClause.create(new DAOParam("testint"))
                .add(new DAOParam("testInteger"))
                .add(new DAOParam("testString")));
        log("createTable and createIndex");
        super.log("");
    }

    @Override
    public void onUpdate(DAOTemplate dao, int oldVersion, int newVersion) {
        if (newVersion < oldVersion)
        {
            return;
        }

        super.log("onUpdate");

        recordTime();
        dao.execute(new DAOTransaction() {
            
            @Override
            public boolean execute(DAOTemplate dao) throws Exception {
                dao.renameTable("db_test", "db_test1");
                dao.createTable(TestBean.class);
                dao.execute("INSERT INTO db_test SELECT * FROM db_test1");
                dao.execute("ALTER TABLE db_test ADD add_column");
                return true;
            }
        });

        log("isTableExist:" + dao.isTableExist(TestBean.class));
        super.log("");
    }
    
    private String convert(boolean b)
    {
        return b ? "successful" : "fail";
    }
    
    @DAOTable(name="db_test")
    public static class TestBean implements BaseColumns {
        
        @DAOPrimaryKey(column=_ID, autoincrement=true)
        public long id;
        
        @DAOProperty(column="db_boolean")
        public boolean testboolean;

        @DAOProperty(column="db_int")
        public int testint;

        @DAOProperty(column="db_char")
        public char testchar;

        @DAOProperty(column="db_short")
        public short testshort;

        @DAOProperty(column="db_float")
        public float testfloat;

        @DAOProperty(column="db_double")
        public double testdouble;

        @DAOProperty(column="db_byte")
        public byte testbyte;

        @DAOProperty(column="dbLong")
        public Long testLong;

        @DAOProperty(column="dbBoolean")
        public Boolean testBoolean;

        @DAOProperty(column="dbInteger")
        public Integer testInteger;

        @DAOProperty(column="dbCharacter")
        public Character testCharacter;

        @DAOProperty(column="dbShort")
        public Short testShort;

        @DAOProperty(column="dbFloat")
        public Float testFloat;

        @DAOProperty(column="dbDouble")
        public Double testDouble;

        @DAOProperty(column="dbByte")
        public Byte testByte;

        @DAOProperty(column="dbString")
        public String testString;

        @DAOProperty(column="dbBlob")
        public byte[] testBlob;

        @DAOProperty(column="db_date")
        public Date date;

        @DAOProperty(column="db_time")
        public Time time;
        
        public TestBean() {}
        
        public TestBean(int i) {
            testboolean = i % 2 == 0;
            testint = i;
            testchar = (char) i;
            testshort = (short) i;
            testfloat = i / 3.0f;
            testdouble = i / 3.0;
            testbyte = (byte) i;
            
            testLong = (long) i;
            testBoolean = i % 2 == 0;
            testInteger = i;
            testCharacter = (char) i;
            testShort = (short) i;
            testFloat = i / 3.0f;
            testDouble = i / 3.0;
            testByte = (byte) i;
            
            testString = String.valueOf(i);
            testBlob = new byte[]{(byte) i};
            
            date = new Date(System.currentTimeMillis());
            time = new Time(System.currentTimeMillis());
        }
    }

    @Override
    public void onChange() {
        super.log("Database onChange");
    }
    
    private void recordTime() {
        time = System.currentTimeMillis();
    }
    
    @Override
    protected void log(String content) {
        super.log(content + "[" + (System.currentTimeMillis() - time) + "ms]");
    }
}