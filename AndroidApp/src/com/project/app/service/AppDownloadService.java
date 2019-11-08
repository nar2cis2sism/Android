package com.project.app.service;

import engine.android.core.util.LogFactory.LOG;
import engine.android.util.AndroidUtil;
import engine.android.util.file.FileDownloader;
import engine.android.util.file.FileDownloader.DownloadStateListener;
import engine.android.util.file.FileSize;
import engine.android.util.manager.SDCardManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

import com.daimon.yueba.R;

/**
 * App安装包下载
 */
public class AppDownloadService extends Service implements DownloadStateListener, Runnable {
    
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_URL   = "url";
    
    /**
     * 后台服务下载
     * 
     * @param title 显示标题
     * @param url 下载地址
     */
    public static void download(Context context, String title, String url) {
        context.startService(new Intent(context, AppDownloadService.class)
        .putExtra(EXTRA_TITLE, title)
        .putExtra(EXTRA_URL, url));
    }
    
    Handler handler;
    MyBroadcastReceiver receiver;
    FileDownloader fd;
    
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        (receiver = new MyBroadcastReceiver(this)).register();
    }
    
    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        receiver.unregister();
        if (fd != null) fd.close();
        super.onDestroy();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (fd == null)
        {
            buildNotification(intent.getStringExtra(EXTRA_TITLE));
            
            fd = new FileDownloader(intent.getStringExtra(EXTRA_URL), SDCardManager.openSDCardAppDir(this));
            fd.config().setDownloadThreadNum(3).setBreakPointEnabled(true);
            fd.setStateListener(this);
            fd.startDownload();
        }
        
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStateChanged(FileDownloader fileDownloader, int downloadState) {
        switch (downloadState) {
            case START:
                remoteViews.setTextViewText(R.id.speed, "正在下载");
                notifyUpdate();
                break;
            case DOWNLOADING:
                run();
                break;
            case STOP:
                handler.removeCallbacks(this);
                break;
            case FINISH:
                handler.removeCallbacks(this);
                remoteViews.setProgressBar(android.R.id.progress, 100, 100, false);
                remoteViews.setTextViewText(R.id.speed, "下载完毕，点击进行安装");
                remoteViews.setTextViewText(R.id.size, null);
                notification.contentIntent = pendingBroadcastIntent(
                        new Intent(MyBroadcastReceiver.ACTION_FINISH));
                notifyUpdate();
                break;
        }
    }

    @Override
    public void onDownloadError(FileDownloader fileDownloader, Throwable throwable) {
        LOG.log(throwable);
        stopSelf();
    }
    
    @Override
    public void run() {
        float percent = 1f * fd.getDownloadSize() / fd.getFileSize();
        remoteViews.setProgressBar(android.R.id.progress, 10000, (int) (percent * 10000), false);
        
        FileSize downloadSpeed = FileSize.convert(fd.getDownloadSpeed(1000));
        remoteViews.setTextViewText(R.id.speed, String.format("%.2f %s/s", 
                downloadSpeed.getSize(), downloadSpeed.getUnit().name()));

        FileSize fileSize = FileSize.convert(fd.getFileSize());
        FileSize downloadSize = new FileSize(fd.getDownloadSize()).toUnit(fileSize.getUnit());
        remoteViews.setTextViewText(R.id.size, String.format("%.2f / %.2f %s", 
                downloadSize.getSize(), fileSize.getSize(), fileSize.getUnit().name()));
        
        notifyUpdate();
        handler.postDelayed(this, 100);
    }

    RemoteViews remoteViews;
    Notification notification;
    private void buildNotification(String title) {
        remoteViews = new RemoteViews(getPackageName(), R.layout.app_download_notification);
        remoteViews.setTextViewText(R.id.title, title);
        
        Builder builder = new Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContent(remoteViews)
        .setDeleteIntent(pendingBroadcastIntent(new Intent(MyBroadcastReceiver.ACTION_DELETE)))
        .setOngoing(true);
        notification = builder.build();
        // 通知栏默认高度64dp不够显示，使用大布局
        notification.bigContentView = remoteViews;
    }

    private PendingIntent pendingBroadcastIntent(Intent intent) {
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private void notifyUpdate() {
        startForeground(hashCode(), notification);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        
        public static final String ACTION_DELETE = "delete";
        public static final String ACTION_FINISH = "finish";
        
        private final Context context;
        
        public MyBroadcastReceiver(Context context) {
            this.context = context;
        }
        
        public void register() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DELETE);
            filter.addAction(ACTION_FINISH);
            
            context.registerReceiver(this, filter);
        }
        
        public void unregister() {
            context.unregisterReceiver(this);
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_DELETE.equals(action))
            {
                fd.stopDownload();
            }
            else if (ACTION_FINISH.equals(action))
            {
                AndroidUtil.installApp(context, fd.getDownloadFile());
            }

            stopSelf();
        }
    }
}