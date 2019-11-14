package com.project.app;

import com.project.app.service.AppService;
import com.project.app.util.MySoundPlayer;
import com.project.storage.MyDAOManager;
import com.project.storage.MySharedPreferences;

/**
 * 应用程序初始化
 * 
 * @author Daimon
 */
public class MyInitial {
    
    /** 清除缓存数据 **/
    private static final boolean RESET = false;
    
    static
    {
        if (RESET)
        {
            MyDAOManager.getDAO().deleteSelf();
            MySharedPreferences.getInstance().reset();
            MyApp.global().getImageManager().getStorage().clear();
        }
    }

    /**
     * Run in background thread.
     */
    public static void init() {
        // 这里通过第一次访问SQLiteDatabase创建或更新数据库
        MyDAOManager.getDAO().getDataBase();
        // 加载声音资源
        MySoundPlayer.getInstance();
        // 播放背景音乐
        AppService.getService();
    }
}