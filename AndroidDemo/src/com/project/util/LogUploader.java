package com.project.util;

import android.content.Context;
import android.util.Log;

import com.project.MyContext;
import com.project.MyConfiguration.MyConfiguration_SHARED_PREFERENCES;
import com.project.storage.MyDAOManager;

import engine.android.core.util.LogFactory;
import engine.android.util.ReflectObject;
import engine.android.util.Singleton;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;

import java.io.File;

/**
 * 日志上传工具（上传到SD卡）
 * 
 * @author Daimon
 */

public class LogUploader {

    private static final Singleton<LogUploader> instance
    = new Singleton<LogUploader>() {

        @Override
        protected LogUploader create() {
            return new LogUploader(MyContext.getContext());
        }
    };

    private final Context context;

    LogUploader(Context context) {
        this.context = context.getApplicationContext();
    }

    /******************************** 华丽丽的分割线 ********************************/

    /**
     * Run in background thread.
     */

    public static void uploadLog() {
        LogFactory.flush();
        instance.get().uploadLog(LogFactory.getLogDir());
    }

    void uploadLog(File logDir) {
        final String tag = "uploadLog";
        if (SDCardManager.isEnabled())
        {
            boolean success = true;
            File desDir = new File(SDCardManager.openSDCardAppDir(context), "logs");

            // 上传日志
            success &= FileManager.copyTo(desDir, logDir);

            desDir = new File(desDir, logDir.getName());
            // 上传数据库
            success &= uploadDatabase(desDir);

            // 上传轻量存储
            success &= uploadSharedPreferences(desDir);

            if (success)
            {
                Log.i(tag, String.format("日志成功上传到(%s)", desDir));
            }
            else
            {
                FileManager.delete(desDir);
                Log.w(tag, "日志上传失败");
            }
        }
        else
        {
            Log.w(tag, "没有SD卡");
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
                    MyConfiguration_SHARED_PREFERENCES.SHARED_PREFERENCES_NAME);
            return FileManager.copyTo(desDir, srcFile);
        } catch (Exception e) {
            Log.w("uploadSharedPreferences", "", e);
        }

        return false;
    }
}