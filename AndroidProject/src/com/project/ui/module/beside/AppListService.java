package com.project.ui.module.beside;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.project.app.bean.AppListItem;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.app.service.LocalService;

public class AppListService extends LocalService {
    
    private final ConcurrentHashMap<String, AppListItem> map               // [key为进程名]
    = new ConcurrentHashMap<String, AppListItem>();
    
    private ExecutorService threadPool;
    private Task task;
    
    @Override
    public void onCreate() {
        threadPool = Executors.newSingleThreadExecutor();
    }
    
    public void disable(AppListItem item) {
        boolean disable = item.isDisabled;
        LOG.log(String.format("%s程序运行:%s", disable ? "禁止" : "允许", item.label));
        if (disable)
        {
            map.put(item.appInfo.processName, item);
        }
        else
        {
            map.remove(item.appInfo.processName);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (task == null) threadPool.execute(task = new Task());
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
        threadPool.shutdownNow();
        super.onDestroy();
    }
    
    private class Task implements Runnable {
        
        private static final int INTERVEL = 10 * 1000;

        @Override
        public void run() {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            while (true)
            {
                List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
                if (list != null && !list.isEmpty())
                {
                    for (RunningAppProcessInfo info : list)
                    {
                        AppListItem item = isProcessRunning(info.processName);
                        if (item != null)
                        {
                            LOG.log("kill程序:" + item.label);
                            am.killBackgroundProcesses(item.appInfo.packageName);
                        }
                    }
                }
                
                SystemClock.sleep(INTERVEL);
            }
        }
        
        private AppListItem isProcessRunning(String process) {
            int index = process.indexOf(":");
            if (index >= 0)
            {
                process = process.substring(0, index);
            }
            
            return map.get(process);
        }
    }
}