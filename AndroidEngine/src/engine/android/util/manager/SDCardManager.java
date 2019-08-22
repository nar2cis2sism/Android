package engine.android.util.manager;

import engine.android.util.Util;
import engine.android.util.file.FileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.LinkedList;

/**
 * SDCard管理器（也有可能是内置存储器）<br>
 * 需要声明权限
 * <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * 
 * @author Daimon
 * @since 3/15/2012
 */
public class SDCardManager {

    private final Context context;

    private BroadcastReceiver receiver;             // SDCard状态监听器

    public SDCardManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 监听SDCard插拔状态
     * 
     * @param receiver 状态监听器，如为Null则取消监听
     */
    public void listen(BroadcastReceiver receiver) {
        if (receiver == null)
        {
            if (this.receiver != null)
            {
                context.unregisterReceiver(this.receiver);
                this.receiver = null;
            }
        }
        else if (this.receiver == null)
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            context.registerReceiver(this.receiver = receiver, filter);
        }
    }

    /**
     * SDCard是否有效（已经挂载并且拥有读写权限）
     */
    public static boolean isEnabled() {
        /**
         * Environment.getExternalStorageState()方法用于获取SDCard的状态，
         * 如果手机装有SDCard，并且可以进行读写，那么方法返回的状态等于Environment.MEDIA_MOUNTED
         */
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 打开文件
     * 
     * @param path SDCard目录下的文件路径
     */
    public static File openSDCardFile(String path) {
        /**
         * Environment.getExternalStorageDirectory()方法用于获取SDCard的目录
         * 2.2的时候为:/mnt/sdcard 2.1的时候为:/sdcard ...
         */
        return new File(Environment.getExternalStorageDirectory(), path);
    }

    /**
     * 打开SDCard存储应用程序目录
     */
    public static File openSDCardAppDir(Context context) {
        return openSDCardFile(context.getPackageName());
    }

    /**
     * 写入文本到SD卡文件
     * 
     * @param filePath 文件相对SD卡的路径
     * @param content 写入内容
     * @param overide true为覆盖,false为追加
     * @return 是否成功写入
     */
    public static boolean writeSDCardFile(String filePath, String content, boolean overide) {
        return isEnabled() && FileManager.writeFile(openSDCardFile(filePath), 
                content.getBytes(), !overide);
    }

    /**
     * 打开SD卡中的数据库
     * 
     * @param path SDCard目录下的文件路径
     */
    public static SQLiteDatabase openSDCardDatabase(String path) {
        return SQLiteDatabase.openOrCreateDatabase(openSDCardFile(path), null);
    }

    public static SDCardInfo[] getAvailableSDCard() {
        LinkedList<SDCardInfo> list = new LinkedList<SDCardInfo>();
        int index = 0;
        File file;
        while ((file = new File(String.format("/sys/block/mmcblk%d/device", index++))).exists())
        {
            list.add(new SDCardInfo(file.getAbsolutePath()));
        }

        return list.toArray(new SDCardInfo[list.size()]);
    }

    public static final class SDCardInfo {

        private final String path;

        private String type;                // 类型
        private String name;                // 厂商
        private String cid;                 // SD卡 ID
        private String csd;
        private String serial;              // 串号/序列号
        private String date;                // 生产日期

        private SDCardInfo(String path) {
            this.path = path;
        }

        /**
         * 类型:SD
         */
        public String getType() {
            if (type == null) type = internal("type");
            return type;
        }

        /**
         * 是否外接SD卡，or内置存储器
         */
        public boolean isSDCard() {
            return getType().equalsIgnoreCase("SD");
        }

        /**
         * 厂商:SU16G
         */
        public String getName() {
            if (name == null) name = internal("name");
            return name;
        }

        /**
         * SD卡ID
         */
        public String getCid() {
            if (cid == null) cid = internal("cid");
            return cid;
        }

        public String getCsd() {
            if (csd == null) csd = internal("csd");
            return csd;
        }

        /**
         * 串号/序列号
         */
        public String getSerial() {
            if (serial == null) serial = internal("serial");
            return serial;
        }

        /**
         * 生产日期:08/2011
         */
        public String getDate() {
            if (date == null) date = internal("date");
            return date;
        }

        private String internal(String param) {
            return Util.getString(FileManager.readFirstLine(new File(path, param)), "");
        }
    }
}