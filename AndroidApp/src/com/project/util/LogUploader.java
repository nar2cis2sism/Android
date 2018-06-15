package com.project.util;

import android.content.Context;
import android.util.Log;

import com.project.storage.MyDAOManager;
import com.project.storage.MySharedPreferences;

import engine.android.core.util.CalendarFormat;
import engine.android.core.util.LogFactory;
import engine.android.util.extra.ReflectObject;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;

import java.io.File;
import java.util.Calendar;

/**
 * 日志上传工具（上传到SD卡）
 * 
 * @author Daimon
 */
public class LogUploader {
    
    private static final String TAG = LogUploader.class.getSimpleName();

    /**
     * 上传日志文件，数据库文件，轻量存储文件到SD卡<br>
     * Call it in background thread.
     */
    public static void upload(Context context) {
        if (SDCardManager.isEnabled())
        {
            String desDirName = CalendarFormat.format(Calendar.getInstance(), "yyyyMMdd#HHmmss");
            File desDir = new File(SDCardManager.openSDCardAppDir(context), "logs/" + desDirName);

            if (uploadLog(desDir)                           // 上传日志
            &&  uploadDatabase(desDir)                      // 上传数据库
            &&  uploadSharedPreferences(context, desDir))   // 上传轻量存储
            {
                Log.i(TAG, String.format("日志成功上传到(%s)", desDir));
            }
            else
            {
                FileManager.delete(desDir);
                Log.w(TAG, "日志上传失败");
            }
        }
        else
        {
            Log.w(TAG, "没有SD卡");
        }
    }
    
    private static boolean uploadLog(File desDir) {
        return FileManager.copyTo(desDir, LogFactory.getLogDir().listFiles());
    }

    /**
     * /data/data/package/databases/name
     */
    private static boolean uploadDatabase(File desDir) {
        return MyDAOManager.getDAO().export(desDir);
    }

    /**
     * /data/data/package/shared_prefs/name.xml
     */
    private static boolean uploadSharedPreferences(Context context, File desDir) {
        try {
            ReflectObject context_ref = new ReflectObject(context);
            File srcFile = (File) context_ref.invoke(
                    context_ref.getMethod("getSharedPrefsFile", String.class),
                    MySharedPreferences.SHARED_PREFERENCES_NAME);
            return FileManager.copyTo(desDir, srcFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}