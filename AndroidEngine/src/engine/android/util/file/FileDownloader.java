//package engine.android.util.file;
//
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//import android.os.Process;
//import android.text.TextUtils;
//
//import engine.android.http.HttpRequest;
//import engine.android.util.MyThreadFactory;
//import engine.android.util.file.FileDownloader.DownloadStateListener.DownloadErrorMessage;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.RandomAccessFile;
//import java.net.HttpURLConnection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * 文件下载器
// * 
// * @author Daimon
// * @version final
// * @since 12/1/2014
// */
//
//// 重定向问题
//// http://image.baidu.com/i?tn=download&ipn=dwnl&word=download&ie=utf8&fr=result&url=http%3A%2F%2Fwww.siweiw.com%2FUpload%2Fsy%2F2013616%2F20136161824820.jpg&qq-pf-to=pcqq.c2c
//
//public class FileDownloader {
//
//    public static final class Config {
//
//        String name = "文件下载";                      // 下载器名称标识
//
//        int downloadThreadNum = 3;                  // 下载线程数量
//
//        boolean breakPointResumeEnabled;            // 断点续传开关
//
//        int downloadBuffer = 4 * 1024;              // 下载缓冲区大小
//
//        @Override
//        public Config clone() {
//            Config config = new Config();
//            config.name = name;
//            config.downloadThreadNum = downloadThreadNum;
//            config.breakPointResumeEnabled = breakPointResumeEnabled;
//            config.downloadBuffer = downloadBuffer;
//            return config;
//        }
//
//        public Config setName(String name) {
//            this.name = name;
//            return this;
//        }
//
//        public Config setDownloadThreadNum(int downloadThreadNum) {
//            this.downloadThreadNum = downloadThreadNum;
//            return this;
//        }
//
//        /**
//         * 是否允许断点续传
//         */
//
//        public Config enableBreakPointResume(boolean enable) {
//            breakPointResumeEnabled = enable;
//            return this;
//        }
//
//        public Config setDownloadBuffer(int downloadBuffer) {
//            this.downloadBuffer = downloadBuffer;
//            return this;
//        }
//    }
//
//    private final String downloadUrl;               // 下载地址
//
//    private final File downloadFile;                // 下载文件
//
//    final Config config;                            // 下载配置
//
//    final File saveFile;                            // 本地保存临时文件
//
//    final File tmpFile;                             // 断点记录缓存文件
//
//    long fileSize = -1;                             // 文件大小
//
//    private final AtomicLong downloadSize           // 已下载大小
//    = new AtomicLong();
//
//    private final AtomicInteger state
//    = new AtomicInteger(DownloadStateListener.STATE_NOT_STARTED);
//                                                    // 下载状态标识
//
//    DownloadStateListener stateListener;            // 下载状态监听器
//
//    private final DownloadHandler downloadHandler;  // 下载处理器
//
//    public FileDownloader(String downloadUrl, File saveDir) {
//        this(downloadUrl, saveDir, null);
//    }
//
//    /**
//     * 构造函数会创建后台线程，记得关闭以释放资源
//     * 
//     * @param downloadUrl 下载地址
//     * @param saveDir 存储目录
//     * @param config 下载配置
//     * @see #close()
//     */
//
//    public FileDownloader(String downloadUrl, File saveDir, Config config) {
//        if (TextUtils.isEmpty(downloadUrl))
//        {
//            throw new DownloadException("请设置下载地址");
//        }
//
//        if (!saveDir.exists() && !saveDir.mkdirs())
//        {
//            throw new DownloadException("不好意思，存储目录创建失败了");
//        }
//
//        downloadFile = new File(saveDir, getFileName(this.downloadUrl = downloadUrl));
//        saveFile = new File(downloadFile.getAbsolutePath() + ".td");
//        tmpFile = new File(saveFile.getAbsolutePath() + ".cfg");
//
//        if (config == null)
//        {
//            this.config = new Config();
//        }
//        else
//        {
//            this.config = config.clone();
//        }
//
//        // Daimon:HandlerThread
//        HandlerThread handlerThread = new HandlerThread(
//                this.config.name + "-FileDownloadHandler", Process.THREAD_PRIORITY_BACKGROUND);
//        handlerThread.start();
//        downloadHandler = new DownloadHandler(handlerThread);
//    }
//
//    /**
//     * 从url中解析下载的文件名称
//     * 
//     * @param url 下载地址
//     */
//
//    private String getFileName(String url) {
//        int index = url.indexOf('?');
//        if (index > -1)
//        {
//            url = url.substring(0, index);
//        }
//
//        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        if (fileName.trim().length() == 0)
//        {
//            // 获取不到文件名
//            fileName = UUID.randomUUID().toString();
//        }
//
//        return fileName;
//    }
//
//    protected HttpRequest newHttpRequest() {
//        return new HttpRequest(downloadUrl, getHttpHeader(), null);
//    }
//
//    /**
//     * Daimon:有些网站为了安全起见，会对请求的http连接进行过滤，因此为了伪装这个http的连接请求，我们给httpHeader穿一件伪装服
//     */
//
//    private Map<String, String> getHttpHeader() {
//        Map<String, String> header = new HashMap<String, String>();
//
//        // 模拟从Ubuntu的firefox浏览器发出的请求
//        header.put("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) " +
//                "Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
//        // 模拟浏览器请求的前一个触发页面，一般设置成首页域名就可以了
//        header.put("Referer", HttpRequest.getHost(downloadUrl));
//
//        return header;
//    }
//
//    public String getDownloadUrl() {
//        return downloadUrl;
//    }
//
//    public long getFileSize() {
//        return fileSize;
//    }
//
//    public long getDownloadSize() {
//        return downloadSize.get();
//    }
//
//    public File getDownloadFile() {
//        return downloadFile;
//    }
//
//    public int getDownloadState() {
//        return state.get();
//    }
//
//    public void setStateListener(DownloadStateListener stateListener) {
//        this.stateListener = stateListener;
//    }
//
//    /**
//     * 下载之前进行配置
//     */
//
//    public Config config() {
//        return config;
//    }
//
//    public void startDownload() {
//        int state = getDownloadState();
//        if (state == DownloadStateListener.STATE_STARTED
//        ||  state == DownloadStateListener.STATE_DOWNLOADING)
//        {
//            // 正在下载
//            return;
//        }
//        else if (state == DownloadStateListener.STATE_FINISH)
//        {
//            // 文件已下载
//            callbackHandler.notifyStateChanged(state);
//            return;
//        }
//
//        if (notifyStateChanged(DownloadStateListener.STATE_STARTED))
//        {
//            downloadHandler.startDownload();
//        }
//    }
//
//    public void stopDownload() {
//        if (notifyStateChanged(DownloadStateListener.STATE_STOP))
//        {
//            downloadHandler.stopDownload();
//        }
//    }
//
//    /**
//     * 提供一个入口清除断点缓存文件
//     */
//
//    public void clearBreakPointCache() {
//        if (tmpFile.exists())
//        {
//            tmpFile.delete();
//        }
//
//        if (saveFile.exists())
//        {
//            saveFile.delete();
//        }
//    }
//
//    public void close() {
//        stateListener = null;
//        notifyStateChanged(DownloadStateListener.STATE_NOT_STARTED);
//        downloadHandler.quit();
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        try {
//            close();
//        } finally {
//            super.finalize();
//        }
//    }
//
//    /**
//     * 下载更新
//     */
//
//    void downloadUpdate(long size) {
//        if (downloadSize.addAndGet(size) == fileSize)
//        {
//            if (tmpFile.exists())
//            {
//                tmpFile.delete();
//            }
//
//            if (downloadFile.exists())
//            {
//                downloadFile.delete();
//            }
//
//            saveFile.renameTo(downloadFile);
//
//            downloadHandler.stopDownload();
//            notifyStateChanged(DownloadStateListener.STATE_FINISH);
//        }
//    }
//
//    boolean notifyStateChanged(int newState) {
//        if (state.getAndSet(newState) == newState)
//        {
//            return false;
//        }
//
//        callbackHandler.notifyStateChanged(newState);
//        return true;
//    }
//
//    void notifyDownloadError(Throwable t) {
//        if (getDownloadState() == DownloadStateListener.STATE_STOP)
//        {
//            return;
//        }
//
//        if (state.getAndSet(DownloadStateListener.STATE_FAILURE)
//                != DownloadStateListener.STATE_FAILURE)
//        {
//            downloadHandler.stopDownload();
//            callbackHandler.notifyDownloadError(t);
//        }
//    }
//
//    public static class DownloadException extends RuntimeException {
//
//        private static final long serialVersionUID = 1L;
//
//        public DownloadException(String detailMessage) {
//            super(detailMessage);
//        }
//
//        public DownloadException(String detailMessage, Throwable throwable) {
//            super(detailMessage, throwable);
//        }
//    }
//
//    private class DownloadHandler extends Handler {
//
//        private static final int WHAT_NONE              = 0;
//        private static final int WHAT_START_DOWNLOAD    = 1;
//        private static final int WHAT_STOP_DOWNLOAD     = 2;
//        private static final int WHAT_QUIT_DOWNLOAD     = 3;
//
//        private final HandlerThread handlerThread;
//
//        private boolean isStopped = true;               // 下载是否停止
//
//        private boolean isHandlingMessage;              // 是否正在处理事件
//
//        private int lastEvent = WHAT_NONE;              // 保留最后一次事件，以防重复频繁开关下载
//
//        private GetFileSizeThread getFileSizeThread;    // 获取文件大小线程
//
//        private ExecutorService downloadExecutor;       // 文件下载线程池
//
//        private DownloadThread[] downloadThreads;       // 下载线程
//
//        private int downloadThreadNum;                  // 下载线程数量
//
//        private long block;                             // 每条线程下载大小
//
//        private boolean isQuit;                         // 线程已关闭
//
//        public DownloadHandler(HandlerThread handlerThread) {
//            super(handlerThread.getLooper());
//            this.handlerThread = handlerThread;
//        }
//
//        public void startDownload() {
//            giveLastEvent(WHAT_START_DOWNLOAD);
//        }
//
//        public void stopDownload() {
//            giveLastEvent(WHAT_STOP_DOWNLOAD);
//        }
//
//        /**
//         * 文件大小获取之后，开始下载文件
//         */
//
//        public void download() {
//            if (lastEvent == WHAT_NONE)
//            {
//                startDownload();
//            }
//        }
//
//        public void quit() {
//            giveLastEvent(WHAT_QUIT_DOWNLOAD);
//        }
//
//        private void giveLastEvent(int whatIsLastEvent) {
//            synchronized (this) {
//                if (isQuit)
//                {
//                    return;
//                }
//
//                if (isHandlingMessage)
//                {
//                    lastEvent = whatIsLastEvent;
//                }
//                else
//                {
//                    isHandlingMessage = true;
//                    sendEmptyMessage(whatIsLastEvent);
//                }
//
//                if (whatIsLastEvent == WHAT_QUIT_DOWNLOAD)
//                {
//                    isQuit = true;
//                }
//            }
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case WHAT_START_DOWNLOAD:
//                    handleStartDownload();
//                    break;
//                case WHAT_STOP_DOWNLOAD:
//                    handleStopDownload();
//                    break;
//                case WHAT_QUIT_DOWNLOAD:
//                    handleStopDownload();
//                    handlerThread.quit();
//                    return;
//            }
//
//            synchronized (this) {
//                if (lastEvent != WHAT_NONE)
//                {
//                    sendEmptyMessage(lastEvent);
//                    lastEvent = WHAT_NONE;
//                }
//                else
//                {
//                    isHandlingMessage = false;
//                }
//            }
//        }
//
//        private void handleStartDownload() {
//            isStopped = false;
//            if (fileSize > 0)
//            {
//                handleDownloadFile();
//            }
//            else if (getFileSizeThread == null)
//            {
//                (getFileSizeThread = new GetFileSizeThread()).start();
//            }
//        }
//
//        private void handleStopDownload() {
//            if (isStopped)
//            {
//                return;
//            }
//
//            isStopped = true;
//            if (getFileSizeThread != null)
//            {
//                getFileSizeThread.cancel();
//                getFileSizeThread = null;
//            }
//
//            if (downloadThreads != null)
//            {
//                for (DownloadThread thread : downloadThreads)
//                {
//                    if (thread != null)
//                    {
//                        thread.cancel();
//                    }
//                }
//            }
//
//            if (downloadExecutor != null)
//            {
//                downloadExecutor.shutdownNow();
//                downloadExecutor = null;
//            }
//
//            if (config.breakPointResumeEnabled)
//            {
//                try {
//                    save();
//                } catch (DownloadException e) {
//                    callbackHandler.notifyDownloadError(e);
//                }
//            }
//        }
//
//        private void handleDownloadFile() {
//            ensureDownloadThreads();
//            if (notifyStateChanged(DownloadStateListener.STATE_DOWNLOADING))
//            {
//                executeDownload();
//            }
//        }
//
//        private void ensureDownloadThreads() {
//            if (downloadThreads == null)
//            {
//                if (config.breakPointResumeEnabled)
//                {
//                    try {
//                        restore();
//                    } catch (DownloadException e) {
//                        callbackHandler.notifyDownloadError(e);
//                    }
//                }
//
//                if (downloadThreads == null)
//                {
//                    downloadThreadNum = config.downloadThreadNum;
//
//                    block = fileSize % downloadThreadNum == 0
//                            ? fileSize / downloadThreadNum
//                            : fileSize / downloadThreadNum + 1;
//
//                    downloadThreads = new DownloadThread[downloadThreadNum];
//                }
//                else
//                {
//                    downloadThreadNum = downloadThreads.length;
//                }
//            }
//        }
//
//        private void executeDownload() {
//            downloadExecutor = Executors.newFixedThreadPool(downloadThreadNum,
//                    new MyThreadFactory(config.name + "-FileDownloadThread"));
//
//            for (int i = 0; i < downloadThreadNum; i++)
//            {
//                if (downloadThreads[i] == null)
//                {
//                    long startPos = block * i;// 开始位置
//                    long endPos = block * (i + 1) - 1;// 结束位置
//                    long size = block;
//                    if (endPos >= fileSize)
//                    {
//                        endPos = fileSize - 1;
//                        size = fileSize - startPos;
//                    }
//
//                    downloadThreads[i] = new DownloadThread(startPos, endPos, size);
//                }
//                else if (downloadThreads[i].isFinished())
//                {
//                    continue;
//                }
//
//                downloadExecutor.execute(downloadThreads[i]);
//            }
//        }
//
//        /**
//         * 断点存储
//         */
//
//        private void save() throws DownloadException {
//            if (getDownloadState() == DownloadStateListener.STATE_FINISH)
//            {
//                return;
//            }
//
//            try {
//                if (!tmpFile.exists() && !tmpFile.createNewFile())
//                {
//                    throw new IOException("无法创建断点记录缓存文件");
//                }
//
//                DataOutputStream dos = null;
//                try {
//                    dos = new DataOutputStream(new FileOutputStream(tmpFile));
//                    dos.writeInt(0x43902758);
//                    if (downloadThreads == null)
//                    {
//                        dos.writeInt(0);
//                    }
//                    else
//                    {
//                        dos.writeInt(downloadThreads.length);
//                        for (DownloadThread thread : downloadThreads)
//                        {
//                            thread.write(dos);
//                        }
//                    }
//                } finally {
//                    if (dos != null)
//                    {
//                        dos.close();
//                    }
//                }
//            } catch (Exception e) {
//                throw new DownloadException("断点存储失败", e);
//            }
//        }
//
//        /**
//         * 断点恢复
//         */
//
//        private void restore() throws DownloadException {
//            if (saveFile.exists() && tmpFile.exists())
//            {
//                try {
//                    DataInputStream dis = null;
//                    try {
//                        dis = new DataInputStream(new FileInputStream(tmpFile));
//                        if (dis.readInt() == 0x43902758)
//                        {
//                            int size = dis.readInt();
//                            if (size == 0)
//                            {
//                                return;
//                            }
//
//                            downloadThreads = new DownloadThread[size];
//
//                            long downloadSize = 0;
//                            long fileSize = 0;
//                            for (int i = 0; i < size; i++)
//                            {
//                                downloadThreads[i] = new DownloadThread();
//                                downloadThreads[i].read(dis);
//                                downloadSize += downloadThreads[i].getDownloadSize();
//                                fileSize += downloadThreads[i].getSize();
//                            }
//
//                            if (fileSize != getFileSize())
//                            {
//                                throw new IllegalStateException("文件大小不匹配");
//                            }
//
//                            downloadUpdate(downloadSize);
//                        }
//                    } finally {
//                        if (dis != null)
//                        {
//                            dis.close();
//                        }
//                    }
//                } catch (Exception e) {
//                    throw new DownloadException("断点恢复失败", e);
//                }
//            }
//        }
//
//        private class GetFileSizeThread extends Thread {
//
//            private final HttpRequest getFileSize;           // 获取文件大小请求
//
//            public GetFileSizeThread() {
//                super(config.name + "-GetFileSizeThread");
//                getFileSize = newHttpRequest();
//            }
//
//            public void cancel() {
//                getFileSize.cancel();
//            }
//
//            @Override
//            public void run() {
//                try {
//                    HttpURLConnection conn = HttpRequest.connect(getFileSize);
//
//                    fileSize = conn.getContentLength();
//
//                    if (fileSize <= 0)
//                    {
//                        throw new Exception("文件大小未知:" + fileSize);
//                    }
//
//                    DownloadHandler.this.download();
//                } catch (Exception e) {
//                    if (!getFileSize.isCancelled())
//                    {
//                        notifyDownloadError(e);
//                    }
//                } finally {
//                    getFileSize.close();
//                }
//            }
//        }
//
//        private class DownloadThread implements Runnable {
//
//            private static final int RETRY_COUNT = 3;
//
//            private final HttpRequest request;          // 文件下载请求
//
//            private long startPos;                      // 下载起始位置
//
//            private long endPos;                        // 下载结束位置
//
//            private long size;                          // 下载大小
//
//            private long downloadSize;                  // 已下载大小
//
//            private int count;                          // 重试次数
//
//            public DownloadThread() {
//                request = newHttpRequest();
//            }
//
//            public DownloadThread(long startPos, long endPos, long size) {
//                this();
//                this.startPos = startPos;
//                this.endPos = endPos;
//                this.size = size;
//            }
//
//            public void cancel() {
//                request.cancel();
//            }
//
//            @Override
//            public void run() {
//                count = RETRY_COUNT;
//                while (getDownloadState() == DownloadStateListener.STATE_DOWNLOADING)
//                {
//                    try {
//                        RandomAccessFile file = null;
//                        try {
//                            file = new RandomAccessFile(saveFile, "rwd");
//                            file.seek(startPos + downloadSize);
//
//                            request.setHeader("Range",
//                                    String.format("bytes=%d-%d", startPos + downloadSize, endPos));
//                            HttpURLConnection conn = HttpRequest.connect(request);
//
//                            InputStream is = conn.getInputStream();
//
//                            byte[] buffer = new byte[config.downloadBuffer];
//                            int n = 0;
//                            while (getDownloadState() == DownloadStateListener.STATE_DOWNLOADING
//                               && (n = is.read(buffer)) > 0)
//                            {
//                                if (downloadSize + n > size)
//                                {
//                                    n = (int) (size - downloadSize);
//                                }
//
//                                if (n > 0)
//                                {
//                                    file.write(buffer, 0, n);
//                                    downloadSize += n;
//                                    downloadUpdate(n);
//                                }
//
//                                if (isFinished())
//                                {
//                                    return;
//                                }
//                            }
//                        } finally {
//                            request.close();
//
//                            if (file != null)
//                            {
//                                file.close();
//                            }
//                        }
//                    } catch (Exception e) {
//                        if (request.isCancelled())
//                        {
//                            return;
//                        }
//
//                        if (--count == 0)
//                        {
//                            notifyDownloadError(e);
//                        }
//                    }
//                }
//            }
//
//            public boolean isFinished() {
//                if (downloadSize > size)
//                {
//                    downloadSize = 0;
//                }
//
//                return downloadSize == size;
//            }
//
//            public long getDownloadSize() {
//                return downloadSize;
//            }
//
//            public long getSize() {
//                return size;
//            }
//
//            public void write(DataOutputStream dos) throws IOException {
//                dos.writeLong(startPos);
//                dos.writeLong(endPos);
//                dos.writeLong(size);
//                dos.writeLong(downloadSize);
//            }
//
//            public void read(DataInputStream dis) throws IOException {
//                startPos = dis.readLong();
//                endPos = dis.readLong();
//                size = dis.readLong();
//                downloadSize = dis.readLong();
//            }
//        }
//    }
//
//    final CallbackHandler callbackHandler = new CallbackHandler();
//
//    private class CallbackHandler extends Handler {
//
//        public void notifyStateChanged(int newState) {
//            obtainMessage(newState, null).sendToTarget();
//        }
//
//        public void notifyDownloadError(Throwable throwable) {
//            obtainMessage(getDownloadState(), 
//                    new DownloadErrorMessage(Thread.currentThread(), throwable))
//                    .sendToTarget();
//        }
//
//        public void handleMessage(Message msg) {
//            if (stateListener != null)
//            {
//                stateListener.onStateChanged(FileDownloader.this,
//                        msg.what, (DownloadErrorMessage) msg.obj);
//            }
//        }
//    }
//
//    public static interface DownloadStateListener {
//
//        public static final int STATE_NOT_STARTED   = 0;    // 未开始下载
//        public static final int STATE_STARTED       = 1;    // 启动下载，获取文件大小
//        public static final int STATE_DOWNLOADING   = 2;    // 已取得文件大小，开启多线程下载文件
//        public static final int STATE_FINISH        = 3;    // 文件下载成功
//        public static final int STATE_STOP          = 4;    // 手动停止下载
//        public static final int STATE_FAILURE       = 5;    // 文件下载失败
//
//        public void onStateChanged(FileDownloader fileDownloader,
//                int downloadState, DownloadErrorMessage downloadErrorMessage);
//
//        public static final class DownloadErrorMessage {
//
//            private final Thread thread;
//
//            private final Throwable throwable;
//
//            DownloadErrorMessage(Thread thread, Throwable throwable) {
//                this.thread = thread;
//                this.throwable = throwable;
//            }
//
//            public Thread getThread() {
//                return thread;
//            }
//
//            public Throwable getThrowable() {
//                return throwable;
//            }
//
//            @Override
//            public String toString() {
//                return String.format("%s:%s", thread, throwable.toString());
//            }
//        }
//    }
//}