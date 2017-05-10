package engine.android.framework.ui.util;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import engine.android.core.util.LogFactory;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.http.HttpConnector;
import engine.android.http.HttpResponse;
import engine.android.util.image.AsyncImageLoader;
import engine.android.util.image.AsyncImageLoader.ImageCallback;
import engine.android.util.image.AsyncImageLoader.ImageDownloader;
import engine.android.util.image.ImageStorage;

/**
 * 图片统一管理
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class ImageManager {
    
    private final AppConfig config;

    private final AsyncImageLoader loader;
    
    private final ImageDownloader downloader;

    final WeakHashMap<View, ImageUrl> displayViewMap;
    
    final ImageStorage storage;
    
    private final Transformer transformer;

    private boolean printLog = true;
    
    public ImageManager(Context context) {
        config = AppGlobal.get(context).getConfig();
        loader = new AsyncImageLoader();
        downloader = new MyImageDownloader(context);
        displayViewMap = new WeakHashMap<View, ImageUrl>();
        storage = new ImageStorage(config.getImageDir());
        transformer = config.getTransformer();
    }
    
    /**
     * 默认打印图片下载日志，如有困扰可关闭
     */
    public void disablePrintLog(boolean disable) {
        printLog = !disable;
    }

    public void display(ImageView view, ImageUrl url, Drawable defaultDrawable) {
        if (url == null || url.equals(displayViewMap.put(view, url)))
        {
            return;
        }

        Bitmap image = loader.getImage(url);
        if (image != null)
        {
            view.setImageBitmap(image);
        }
        else
        {
            if (view.getDrawable() == null && defaultDrawable != null)
            {
                view.setImageDrawable(defaultDrawable);
            }

            loader.loadImage(url, downloader, new ImageViewCallback(view));
        }
    }

    private class ImageViewCallback implements ImageCallback {

        private final WeakReference<ImageView> callback;

        public ImageViewCallback(ImageView view) {
            callback = new WeakReference<ImageView>(view);
        }

        @Override
        public void imageLoaded(Object url, Bitmap image) {
            ImageView view = callback.get();
            if (view != null && url.equals(displayViewMap.get(view)))
            {
                showImage(view, image);
            }
        }

        private void showImage(ImageView view, Bitmap image) {
            if (image != null)
            {
                view.setImageBitmap(image);
            }
            else
            {
                // 下载出错
            }
        }
    }
    
    private class MyImageDownloader implements ImageDownloader {
        
        private final SharedPreferences sp;
        
        public MyImageDownloader(Context context) {
            sp = context.getSharedPreferences("image", 0);
        }

        @Override
        public Bitmap imageLoading(Object url) {
            ImageUrl imageUrl = (ImageUrl) url;
            String fileKey = imageUrl.getFileKey();
            String crc = imageUrl.crc;
            
            Bitmap image = null;

            if (checkCrc(fileKey, crc))
            {
                storage.remove(fileKey);
            }
            else
            {
                image = storage.get(fileKey);
            }

            if (image == null)
            {
                if (config.isOffline())
                {
                    SystemClock.sleep(1000);
                }
                else
                {
                    String downloadUrl = imageUrl.getDownloadUrl();
                    if (printLog) log("图片下载-" + fileKey, downloadUrl);
                    
                    HttpConnector conn = new HttpConnector(downloadUrl);
                    conn.getRequest().setHeader("Accept", "*/*");
                    try {
                        HttpResponse resp = conn.connect();
                        byte[] bs = resp.getContent();
                        image = BitmapFactory.decodeByteArray(bs, 0, bs.length);
                        if (printLog)
                        {
                            log("图片下载-" + fileKey, image == null ?
                                "无图片" : image.getWidth() + "*" + image.getHeight());
                        }
                        
                        if (transformer != null) image = transformer.transform(imageUrl, image);
                        if (image != null && storage.put(fileKey, bs))
                        {
                            sp.edit().putString(getCrcKey(fileKey), crc).commit();
                        }
                    } catch (Exception e) {
                        if (printLog) log("图片下载-" + fileKey, e);
                    }
                }
            }
            
            return image;
        }

        /**
         * 比对图片版本号
         * 
         * @return 图片是否有更新
         */
        private boolean checkCrc(String fileKey, String crc) {
            boolean change = false;

            String nativeCrc = "";
            if (!TextUtils.isEmpty(crc))
            {
                nativeCrc = sp.getString(getCrcKey(fileKey), nativeCrc);
                if (!crc.equals(nativeCrc))
                {
                    change = true;
                }
            }

            if (printLog)
                log("图片版本校验-" + fileKey, nativeCrc + "->" + crc + "=change:" + change);

            return change;
        }
        
        private String getCrcKey(String fileKey) {
            return fileKey;
        }
    }

    /**
     * 拼装图片URL
     */
    public static class ImageUrl {
        
        public final int type;              // 图片类型

        public final String url;            // 下载地址

        public final String crc;            // 版本校验
        
        public ImageUrl(int type, String url, String crc) {
            this.type = type;
            this.url = url;
            this.crc = crc;
        }
        
        /**
         * 下载地址
         */
        public String getDownloadUrl() {
            return url;
        }

        /**
         * 文件存储唯一性
         */
        String getFileKey() {
            return type + url;
        }

        /**
         * 缓存唯一性
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
            {
                return true;
            }
            
            if (o instanceof ImageUrl)
            {
                ImageUrl imageUrl = (ImageUrl) o;
                return imageUrl.type == type 
                    && TextUtils.equals(imageUrl.url, url)
                    && TextUtils.equals(imageUrl.crc, crc);
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hashCode = type;
            if (!TextUtils.isEmpty(url))
            {
                hashCode += url.hashCode();
            }
            
            if (!TextUtils.isEmpty(crc))
            {
                hashCode += crc.hashCode();
            }
            
            return hashCode;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder()
            .append("[")
            .append("type=").append(type).append(",")
            .append("url=").append(url).append(",")
            .append("crc=").append(crc)
            .append("]");
            return sb.toString();
        }
    }
    
    /**
     * 图片转换器
     */
    public interface Transformer {
        
        Bitmap transform(ImageUrl url, Bitmap image);
    }
    
    static
    {
        LogFactory.addLogFile(ImageManager.class, "image.txt");
    }
}