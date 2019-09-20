package com.project.util;

import engine.android.core.util.CalendarFormat;
import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.util.extra.ReflectObject;
import engine.android.util.file.FileManager;
import engine.android.util.image.ImageUtil;
import engine.android.util.manager.SDCardManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.project.storage.MyDAOManager;
import com.project.storage.MySharedPreferences;

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
     * 上传日志文件，数据库文件，轻量存储文件，截屏图片到SD卡<br>
     * PS:Call it in background thread.
     *
     * @return 日志存储SD卡目录
     */
    public static File upload(Context context, Bitmap screenshot) {
        if (SDCardManager.isEnabled())
        {
            String desDirName = CalendarFormat.format(Calendar.getInstance(), "yyyyMMdd#HHmmss");
            File desDir = new File(SDCardManager.openSDCardAppDir(context), "logs/" + desDirName);
            try {
                uploadScreenShot(screenshot, desDir);        // 上传截屏图片
                uploadSharedPreferences(context, desDir);    // 上传轻量存储
                uploadDatabase(desDir);                      // 上传数据库
                uploadLog(desDir);                           // 上传日志

                Log.i(TAG, String.format("日志成功上传到(%s)", desDir));
                return desDir;
            } catch (Exception e) {
                FileManager.delete(desDir);
                Log.w(TAG, e);
            }
        }
        else
        {
            Log.w(TAG, "没有SD卡");
        }

        return null;
    }
    
    private static void uploadScreenShot(Bitmap screenshot, File desDir) {
        byte[] bs = ImageUtil.image2bytes(screenshot);
        if (bs != null)
        {
            File file = new File(desDir, "screenshot.png");
            FileManager.writeFile(file, bs, false);
        }
    }

    /**
     * /data/data/package/shared_prefs/name.xml
     */
    private static void uploadSharedPreferences(Context context, File desDir) {
        try {
            ReflectObject context_ref = new ReflectObject(context);
            File srcFile = (File) context_ref.invoke(
                    context_ref.getMethod("getSharedPrefsFile", String.class),
                    MySharedPreferences.SHARED_PREFERENCES_NAME);
            FileManager.copyTo(desDir, srcFile);
        } catch (Exception e) {
            LOG.log(e);
        }
    }

    /**
     * /data/data/package/databases/name
     */
    private static void uploadDatabase(File desDir) {
        MyDAOManager.getDAO().export(desDir);
    }

    /**
     * /data/data/package/app_log
     */
    private static void uploadLog(File desDir) {
        FileManager.copyTo(desDir, LogFactory.getLogDir().listFiles());
    }
}