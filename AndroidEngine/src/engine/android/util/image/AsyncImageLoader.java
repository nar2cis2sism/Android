package engine.android.util.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import engine.android.util.extra.MyThreadFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步图片加载器
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class AsyncImageLoader {

    private static final int MAX_REQUEST_NUM
    = Math.max(3, Runtime.getRuntime().availableProcessors() - 1);

    private final ThreadPoolExecutor requestPool;                       // 图片下载线程池

    private final ImageCache<Object> imageCache;                        // 图片缓存

    private final ConcurrentHashMap<Object, ImageRequest> lockMap;      // 图片锁库(防止重复加载)

    private final HashMap<Object, Set<ImageCallback>> callbackMap;      // 回调查询表

    private final ImageHandler handler;                                 // 图片回调处理器

    public AsyncImageLoader() {
        this(MAX_REQUEST_NUM);
    }

    /**
     * @param maxRequestNum 最大请求数
     */
    public AsyncImageLoader(int maxRequestNum) {
        requestPool = new ThreadPoolExecutor(
                maxRequestNum, 
                maxRequestNum,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory("图片下载"));
        requestPool.allowCoreThreadTimeOut(true);

        imageCache = new ImageCache<Object>();
        lockMap = new ConcurrentHashMap<Object, ImageRequest>();
        callbackMap = new HashMap<Object, Set<ImageCallback>>();
        handler = new ImageHandler();
    }

    public void setMaxRequestNum(int maxRequestNum) {
        requestPool.setCorePoolSize(maxRequestNum);
        requestPool.setMaximumPoolSize(maxRequestNum);
    }

    /**
     * 加载图片
     * 
     * @param url 图片下载地址
     * @param downloader 图片下载实现
     * @param callback 图片下载回调接口
     */
    public Bitmap loadImage(Object url, ImageDownloader downloader, ImageCallback callback) {
        Bitmap image = imageCache.get(url);
        if (image != null || downloader == null)
        {
            return image;
        }
        
        if (callback != null || lockMap.get(url) == null)
        {
            // 启动线程下载
            requestPool.execute(new ImageRequest(url, downloader, callback));
        }

        return image;
    }

    /**
     * 更新缓存中图片
     * 
     * @param url 图片下载地址
     * @param image 如为Null表示重新下载图片
     */
    public void updateImage(Object url, Bitmap image) {
        lockMap.remove(url);
        if (image == null)
        {
            imageCache.remove(url);
        }
        else
        {
            imageCache.put(url, image);
        }
    }

    /**
     * 释放图片缓存并停止加载
     */
    public void release() {
        requestPool.shutdownNow();
        lockMap.clear();
        imageCache.clear();
        callbackMap.clear();
        handler.removeCallbacksAndMessages(null);
    }
    
    private class ImageRequest implements Runnable {

        private final Object url;

        private final ImageDownloader downloader;

        private final ImageCallback callback;

        private boolean isCancelled;

        private boolean isDone;

        public ImageRequest(Object url, ImageDownloader downloader, ImageCallback callback) {
            this.url = url;
            this.downloader = downloader;
            this.callback = callback;
        }

        @Override
        public void run() {
            ImageRequest request = lockMap.putIfAbsent(url, this);
            if (request == null)
            {
                Bitmap image = downloader.imageLoading(url);

                synchronized (this) {
                    if (this != lockMap.get(url))
                    {
                        // 以防止在此期间取消图片下载
                        cancel();
                        callbackMap.remove(url);
                        return;
                    }

                    // 图片下载完毕即放入缓存
                    imageCache.put(url, image);
                    done();
                    lockMap.remove(url);

                    // 通知回调
                    Set<ImageCallback> callbacks = callbackMap.remove(url);
                    if (callbacks == null || callbacks.isEmpty())
                    {
                        if (callback != null) handler.notifyCallback(url, image, callback);
                    }
                    else
                    {
                        if (callback != null) callbacks.add(callback);
                        handler.notifyCallback(url, image,
                                callbacks.toArray(new ImageCallback[callbacks.size()]));
                    }
                }

                return;
            }

            synchronized (request) {
                if (request.isCancelled() || callback == null)
                {
                    return;
                }

                Bitmap image = imageCache.get(url);
                if (image != null || request.isDone())
                {
                    handler.notifyCallback(url, image, callback);
                    return;
                }

                Set<ImageCallback> callbacks = callbackMap.get(url);
                if (callbacks == null)
                {
                    callbackMap.put(url, callbacks = new HashSet<ImageCallback>());
                }

                callbacks.add(callback);
            }
        }

        public void cancel() {
            isCancelled = true;
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public void done() {
            isDone = true;
        }

        public boolean isDone() {
            return isDone;
        }
    }

    private static class ImageHandler extends Handler {
        
        public ImageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            ImageObj obj = (ImageObj) msg.obj;
            ImageCallback[] callbacks = obj.callbacks;
            for (ImageCallback callback : callbacks)
            {
                callback.imageLoaded(obj.url, obj.image);
            }
        }

        private static class ImageObj {

            public final Object url;

            public final Bitmap image;

            public final ImageCallback[] callbacks;

            public ImageObj(Object url, Bitmap image, ImageCallback[] callbacks) {
                this.url = url;
                this.image = image;
                this.callbacks = callbacks;
            }
        }

        public void notifyCallback(Object url, Bitmap image, ImageCallback... callbacks) {
            obtainMessage(0, new ImageObj(url, image, callbacks)).sendToTarget();
        }
    }

    /**
     * 图片下载实现
     */
    public interface ImageDownloader {

        /**
         * 图片下载方法
         * 
         * @param url 图片下载地址
         * @return 下载的图片
         */
        Bitmap imageLoading(Object url);
    }

    /**
     * 图片下载回调接口
     */
    public interface ImageCallback {

        /**
         * 图片下载回调方法
         * 
         * @param url 图片下载地址
         * @param image 下载的图片
         */
        void imageLoaded(Object url, Bitmap image);
    }
}