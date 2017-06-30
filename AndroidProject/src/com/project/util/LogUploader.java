package com.project.util;

import android.content.Context;
import android.util.Log;

import com.project.app.MyContext;
import com.project.storage.MyDAOManager;
import com.project.storage.MySharedPreferences;

import java.io.File;
import java.util.Calendar;

import engine.android.core.util.CalendarFormat;
import engine.android.core.util.LogFactory;
import engine.android.util.extra.ReflectObject;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;

/**
 * 日志上传工具（上传到SD卡）
 * 
 * @author Daimon
 */
public class LogUploader {
    
    private static final String TAG = LogUploader.class.getSimpleName();

    private final Context context;

    private LogUploader(Context context) {
        this.context = context;
    }

    /**
     * Call it in background thread.
     */
    public static void uploadLog() {
        new LogUploader(MyContext.getContext()).uploadLog(LogFactory.flush());
    }

    /**
     * 上传日志文件，数据库文件，轻量存储文件到SD卡
     */
    public void uploadLog(File logDir) {
        if (SDCardManager.isEnabled())
        {
            boolean success = true;
            File desDir = new File(SDCardManager.openSDCardAppDir(context), 
                    "logs/" + CalendarFormat.format(Calendar.getInstance(), 
                            "yyyyMMdd#HHmmss"));

            // 上传日志
            success &= FileManager.copyTo(desDir, logDir.listFiles());

            // 上传数据库
            success &= uploadDatabase(desDir);

            // 上传轻量存储
            success &= uploadSharedPreferences(desDir);

            if (success)
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

    private boolean uploadDatabase(File desDir) {
        return MyDAOManager.getDAO().export(desDir);
    }

    /**
     * /data/data/package/shared_prefs/name.xml
     */
    private boolean uploadSharedPreferences(File desDir) {
        try {
            ReflectObject context_ref = new ReflectObject(context);
            File srcFile = (File) context_ref.invoke(
                    context_ref.getMethod("getSharedPrefsFile", String.class),
                    MySharedPreferences.SHARED_PREFERENCES_NAME);
            return FileManager.copyTo(desDir, srcFile);
        } catch (Exception e) {
            Log.w("uploadSharedPreferences", "", e);
        }

        return false;
    }
}