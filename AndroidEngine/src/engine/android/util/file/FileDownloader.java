package engine.android.util.file;

import static engine.android.util.file.FileDownloader.DownloadStateListener.DOWNLOADING;
import static engine.android.util.file.FileDownloader.DownloadStateListener.FINISH;
import static engine.android.util.file.FileDownloader.DownloadStateListener.INITIAL;
import static engine.android.util.file.FileDownloader.DownloadStateListener.START;
import static engine.android.util.file.FileDownloader.DownloadStateListener.STOP;

import engine.android.core.util.LogFactory.LOG;
import engine.android.http.HttpConnector;
import engine.android.http.HttpResponse;
import engine.android.util.extra.MyThreadFactory;
import engine.android.util.extra.SyncLock;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件下载器
 * 
 * @author Daimon
 * @since 12/1/2014
 */
public class FileDownloader {
    
    private final String downloadUrl;               // 下载地址
    private final File downloadFile;                // 下载文件
    
    private final File saveFile;                    // 本地保存临时文件
    private final File tmpFile;                     // 断点记录缓存文件
    
    private long fileSize = -1;                     // 文件大小
    private final AtomicLong downloadSize           // 已下载大小
    = new AtomicLong();
    
    private final Config userConfig = new Config(); // 用户配置参数
    private Config config;                          // 有效配置参数
    
    private final AtomicInteger state               // 当前下载状态
    = new AtomicInteger();
    private DownloadStateListener listener;         // 下载状态监听器
    
    /**
     * @param downloadUrl 下载地址
     * @param saveDir 本地存储目录
     */
    public FileDownloader(String downloadUrl, File saveDir) {
        if (TextUtils.isEmpty(downloadUrl))
        {
            throw new DownloadException("请设置下载地址");
        }

        if (!saveDir.exists() && !saveDir.mkdirs())
        {
            throw new DownloadException("不好意思，存储目录创建失败了");
        }

        downloadFile = new File(saveDir, getFileName(this.downloadUrl = downloadUrl));
        saveFile = new File(downloadFile.getAbsolutePath() + ".td");
        tmpFile = new File(saveFile.getAbsolutePath() + ".cfg");
    }
    
    /**
     * 开始/继续下载
     */
    public void startDownload() {
        int state = getDownloadState();
        if (state == START || state == DOWNLOADING)
        {
            // 正在下载
            return;
        }
        
        if (state == FINISH)
        {
            // 文件已下载
            callbackHandler.notifyStateChanged(state);
            return;
        }
        
        if (downloadSize.get() == fileSize)
        {
            notifyStateChanged(FINISH);
            return;
        }
        
        if (state == INITIAL)
        {
            // 初始化
            config = userConfig.clone();
            downloadManager = new DownloadManager();
        }
        
        if (fileSize > 0)
        {
            notifyStateChanged(DOWNLOADING);
            downloadManager.download();
        }
        else
        {
            notifyStateChanged(START);
            downloadManager.start();
        }
    }

    /**
     * 停止下载
     */
    public void stopDownload() {
        int state = getDownloadState();
        if (state == INITIAL || state == FINISH)
        {
            return;
        }
        
        if (notifyStateChanged(STOP))
        {
            downloadManager.stop();
        }
    }
    
    /**
     * 释放下载资源，可重新配置参数
     */
    public void close() {
        if (notifyStateChanged(INITIAL))
        {
            downloadManager.close();
        }
    }
    
    /**
     * 下载之前配置有效
     */
    public Config config() {
        return userConfig;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public File getDownloadFile() {
        return downloadFile;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public long getDownloadSize() {
        return downloadSize.get();
    }
    
    public int getDownloadState() {
        return state.get();
    }
    
    private long speedTime;
    private long speedSize;
    private long speed;
    
    /**
     * 获取下载速度
     * 
     * @param duration 一般为1000，即每秒下载字节数
     */
    public long getDownloadSpeed(int duration) {
        long time = System.currentTimeMillis();
        long size = getDownloadSize();
        
        if (speedTime != 0)
        {
            if (time - speedTime < duration)
            {
                return speed;
            }
            
            speed = duration * (size - speedSize) / (time - speedTime);
        }
        
        speedTime = time;
        speedSize = size;
        
        return speed;
    }
    
    public void setStateListener(DownloadStateListener listener) {
        this.listener = listener;
    }

    /**
     * Daimon:从url中解析出文件名称
     * 
     * @param url 下载地址
     */
    public static final String getFileName(String url) {
        int index = url.indexOf('?');
        if (index >= 0)
        {
            url = url.substring(0, index);
        }

        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (fileName.trim().length() == 0)
        {
            // 获取不到文件名
            fileName = UUID.randomUUID().toString();
        }

        return fileName;
    }
    
    private boolean notifyStateChanged(int newState) {
        if (state.getAndSet(newState) == newState)
        {
            return false;
        }

        callbackHandler.notifyStateChanged(newState);
        return true;
    }

    private void notifyDownloadError(Throwable throwable) {
        if (notifyStateChanged(STOP))
        {
            downloadManager.stop();
            callbackHandler.notifyDownloadError(throwable);
        }
    }

    /**
     * 下载更新
     */
    private void downloadUpdate(long size) {
        if (downloadSize.addAndGet(size) == fileSize)
        {
            if (tmpFile.exists()) tmpFile.delete();
            if (downloadFile.exists()) downloadFile.delete();
            saveFile.renameTo(downloadFile);
            
            int state = getDownloadState();
            if (state == INITIAL || state == STOP)
            {
                return;
            }

            notifyStateChanged(FINISH);
        }
        else
        {
            downloadManager.downloadUpdate.set(true);
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            listener = null;
            close();
        } finally {
            super.finalize();
        }
    }

    public static class Config {
        
        String name = "文件下载";                   // 下载器名称标识
        
        int downloadThreadNum = 1;                  // 下载线程数量
        
        boolean breakPointEnabled;                  // 断点续传开关
        
        int downloadBuffer = 4 * 1024;              // 下载缓冲区大小
        
        /**
         * 给下载线程取个名字
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * 默认单线程下载，可设置多个线程并行下载提高速度
         */
        public Config setDownloadThreadNum(int downloadThreadNum) {
            this.downloadThreadNum = downloadThreadNum;
            return this;
        }
        
        /**
         * 开启/关闭断点续传功能
         */
        public Config setBreakPointEnabled(boolean enable) {
            breakPointEnabled = enable;
            return this;
        }
        
        /**
         * 设置数据缓冲区大小，根据网络带宽可酌情提高，默认4k
         */
        public Config setDownloadBuffer(int downloadBuffer) {
            this.downloadBuffer = downloadBuffer;
            return this;
        }
        
        @Override
        protected Config clone() {
            Config config = new Config();
            config.name = name;
            config.downloadThreadNum = downloadThreadNum;
            config.breakPointEnabled = breakPointEnabled;
            config.downloadBuffer = downloadBuffer;
            return config;
        }
    }
    
    public interface DownloadStateListener {
        
        int INITIAL         = 0;                    // 未开始下载
        int START           = 1;                    // 启动下载，获取文件大小
        int DOWNLOADING     = 2;                    // 已取得文件大小，正在下载文件
        int STOP            = 3;                    // 停止下载
        int FINISH          = 4;                    // 文件下载完毕
        
        void onStateChanged(FileDownloader fileDownloader, int downloadState);
        
        void onDownloadError(FileDownloader fileDownloader, Throwable throwable);
    }
    
    private DownloadManager downloadManager;
    private class DownloadManager {

        private final ExecutorService downloadExecutor;     // 文件下载线程池
        
        private InitialRunnable initial;                    // 获取文件大小
        private DownloadRunnable[] downloads;               // 下载线程
        private final SyncLock lock = new SyncLock();       // 线程暂停锁
        
        public DownloadManager() {
            downloadExecutor = Executors.newFixedThreadPool(
                    config.downloadThreadNum, 
                    new MyThreadFactory(config.name));
        }
        
        public void start() {
            downloadExecutor.execute(initial = new InitialRunnable());
        }
        
        public void stop() {
            if (initial != null)
            {
                initial.cancel();
            }
        }
        
        public void close() {
            stop();
            if (downloads != null)
            {
                for (DownloadRunnable run : downloads)
                {
                    run.cancel();
                }
                
                lock.unlock();
            }
            
            downloadExecutor.shutdown();
        }
        
        public void download() {
            if (downloads == null)
            {
                if (config.breakPointEnabled) restore();
                
                int downloadThreadNum = config.downloadThreadNum;
                if (downloads == null)
                {
                    downloads = new DownloadRunnable[downloadThreadNum];
                }
                else
                {
                    downloadThreadNum = downloads.length;
                }
                
                long block = fileSize % downloadThreadNum == 0
                           ? fileSize / downloadThreadNum
                           : fileSize / downloadThreadNum + 1;
                for (int i = 0; i < downloadThreadNum; i++)
                {
                    if (downloads[i] == null)
                    {
                        long startPos = block * i;// 开始位置
                        long endPos = block * (i + 1) - 1;// 结束位置
                        long size = block;
                        if (endPos >= fileSize)
                        {
                            endPos = fileSize - 1;
                            size = fileSize - startPos;
                        }

                        downloads[i] = new DownloadRunnable(startPos, endPos, size);
                    }
                    else if (downloads[i].isFinished())
                    {
                        continue;
                    }

                    downloadExecutor.execute(downloads[i]);
                }
            }
            else
            {
                lock.unlock();
            }
        }

        /**
         * 断点存储
         */
        private final AtomicBoolean downloadUpdate = new AtomicBoolean();
        private synchronized void save() {
            if (getDownloadState() == FINISH)
            {
                return;
            }
            
            if (downloadUpdate.compareAndSet(true, false))
            {
                try {
                    DataOutputStream dos = null;
                    try {
                        dos = new DataOutputStream(new FileOutputStream(tmpFile));
                        dos.writeInt(0x43902758);
                        if (downloads == null)
                        {
                            dos.writeInt(0);
                        }
                        else
                        {
                            dos.writeInt(downloads.length);
                            for (DownloadRunnable run : downloads)
                            {
                                run.write(dos);
                            }
                        }
                    } finally {
                        if (dos != null)
                        {
                            dos.close();
                        }
                    }
                } catch (Exception e) {
                    LOG.log("断点存储失败", e);
                    // 不影响继续下载
                }
            }
        }

        /**
         * 断点恢复
         */
        private void restore() {
            if (saveFile.exists() && tmpFile.exists())
            {
                try {
                    DataInputStream dis = null;
                    try {
                        dis = new DataInputStream(new FileInputStream(tmpFile));
                        if (dis.readInt() != 0x43902758)
                        {
                            throw new IOException("文件被篡改");
                        }

                        int num = dis.readInt();
                        if (num == 0)
                        {
                            return;
                        }

                        DownloadRunnable[] downloads = new DownloadRunnable[num];

                        long downloadSize = 0;
                        long fileSize = 0;
                        for (int i = 0; i < num; i++)
                        {
                            downloads[i] = new DownloadRunnable();
                            downloads[i].read(dis);
                            
                            downloadSize += downloads[i].getDownloadSize();
                            fileSize += downloads[i].getSize();
                        }

                        if (fileSize != getFileSize())
                        {
                            throw new IllegalStateException("文件大小不匹配");
                        }

                        downloadUpdate(downloadSize);
                        this.downloads = downloads;
                    } finally {
                        if (dis != null)
                        {
                            dis.close();
                        }
                    }
                } catch (Exception e) {
                    LOG.log("断点恢复失败", e);
                    // 不影响重新下载
                }
            }
        }
    }

    private class InitialRunnable implements Runnable {
        
        protected final HttpConnector conn;         // 文件下载请求
        
        public InitialRunnable() {
            conn = new HttpConnector(downloadUrl, getHttpHeaders(), null);
        }
        
        /**
         * Daimon:有些网站为了安全起见，会对请求的http连接进行过滤，
         * 因此为了伪装这个http的连接请求，我们给httpHeader穿一件伪装服
         */
        private Map<String, String> getHttpHeaders() {
            HashMap<String, String> headers = new HashMap<String, String>();
            // 模拟从Ubuntu的firefox浏览器发出的请求
            headers.put("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) " +
                    "Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
            // 模拟浏览器请求的前一个触发页面，一般设置成首页域名就可以了
            headers.put("Referer", HttpConnector.getHost(downloadUrl));

            return headers;
        }

        @Override
        public void run() {
            try {
                if (fileSize <= 0)
                {
                    HttpResponse response = conn.connect();
                    if (response != null)
                    {
                        fileSize = response.getContentLength();
                    }
                    
                    if (fileSize <= 0)
                    {
                        throw new DownloadException("文件大小未知:" + fileSize);
                    }
                }
                
                if (!conn.isCancelled())
                {
                    notifyStateChanged(DOWNLOADING);
                    downloadManager.download();
                }
            } catch (Exception e) {
                if (!conn.isCancelled())
                {
                    notifyDownloadError(e);
                }
            }
        }
        
        public void cancel() {
            conn.cancel();
        }
    }
    
    private class DownloadRunnable extends InitialRunnable {
        
        private long startPos;                      // 下载起始位置

        private long endPos;                        // 下载结束位置

        private long size;                          // 下载大小

        private long downloadSize;                  // 已下载大小
        
        public DownloadRunnable() {}
        
        public DownloadRunnable(long startPos, long endPos, long size) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.size = size;
        }
        
        @Override
        public void run() {
            while (isDownloading())
            {
                try {
                    RandomAccessFile file = null;
                    try {
                        file = new RandomAccessFile(saveFile, "rwd");
                        file.seek(startPos + downloadSize);
                        // 设置数据获取范围区间
                        conn.getRequest().setHeader("Range",
                                String.format("bytes=%d-%d", startPos + downloadSize, endPos));
                        
                        InputStream is = conn.connect().getInputStream();
                        byte[] buffer = new byte[config.downloadBuffer];
                        int n = 0;
                        while (isDownloading() && (n = is.read(buffer)) > 0)
                        {
                            if (downloadSize + n > size)
                            {
                                n = (int) (size - downloadSize);
                            }
                            
                            if (n > 0)
                            {
                                file.write(buffer, 0, n);
                                downloadSize += n;
                                downloadUpdate(n);
                            }
                            
                            if (isFinished())
                            {
                                return;
                            }
                        }
                    } finally {
                        if (file != null)
                        {
                            file.close();
                        }
                    }
                } catch (Exception e) {
                    if (conn.isCancelled())
                    {
                        downloadManager.save();
                        return;
                    }
                    else
                    {
                        notifyDownloadError(e);
                        downloadManager.save();
                    }
                }
            }
        }
        
        /**
         * 未下载状态锁住当前线程
         */
        private boolean isDownloading() {
            if (getDownloadState() != DOWNLOADING)
            {
                downloadManager.lock.lock();
            }
            
            return true;
        }
        
        public long getSize() {
            return size;
        }
        
        public long getDownloadSize() {
            return downloadSize;
        }

        public boolean isFinished() {
            return downloadSize == size;
        }

        public void write(DataOutputStream dos) throws IOException {
            dos.writeLong(startPos);
            dos.writeLong(endPos);
            dos.writeLong(size);
            dos.writeLong(downloadSize);
        }

        public void read(DataInputStream dis) throws IOException {
            startPos = dis.readLong();
            endPos = dis.readLong();
            size = dis.readLong();
            downloadSize = dis.readLong();
        }
    }

    private final CallbackHandler callbackHandler = new CallbackHandler();
    private class CallbackHandler extends Handler {
        
        public CallbackHandler() {
            super(Looper.getMainLooper());
        }

        public void notifyStateChanged(int newState) {
            obtainMessage(newState).sendToTarget();
        }

        public void notifyDownloadError(Throwable throwable) {
            obtainMessage(-1, throwable).sendToTarget();
        }

        public void handleMessage(Message msg) {
            if (listener != null)
            {
                if (msg.obj != null)
                {
                    listener.onDownloadError(FileDownloader.this, (Throwable) msg.obj);
                }
                else
                {
                    listener.onStateChanged(FileDownloader.this, msg.what);
                }
            }
        }
    }

    private static class DownloadException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public DownloadException(String detailMessage) {
            super(detailMessage);
        }
    }
}