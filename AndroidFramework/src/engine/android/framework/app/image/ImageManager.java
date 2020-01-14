package engine.android.framework.app.image;

import static engine.android.core.util.LogFactory.LOG.log;

import engine.android.core.util.LogFactory;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.http.HttpConnector;
import engine.android.util.image.AsyncImageLoader;
import engine.android.util.image.AsyncImageLoader.ImageCallback;
import engine.android.util.image.AsyncImageLoader.ImageDownloader;
import engine.android.util.image.AsyncImageLoader.ImageUrl;
import engine.android.util.image.ImageStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * 图片统一管理
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class ImageManager {
    
    private final AppConfig config;

    private final AsyncImageLoader loader;
    
    private final ImageDownloader downloader;

    private final WeakHashMap<View, ImageUrl> displayViewMap;
    
    private final HashMap<ImageUrl, Bitmap> savedImageMap;
    
    private final ImageStorage storage;
    
    private final Transformer transformer;

    private boolean printLog = true;
    
    public ImageManager(Context context) {
        config = AppGlobal.get(context).getConfig();
        loader = new AsyncImageLoader();
        downloader = new MyImageDownloader(context);
        displayViewMap = new WeakHashMap<View, ImageUrl>();
        savedImageMap = new HashMap<ImageUrl, Bitmap>();
        storage = new ImageStorage(config.getImageDir());
        transformer = config.getImageTransformer();
    }
    
    /**
     * 默认打印图片下载日志，如有困扰可关闭
     */
    public void disablePrintLog(boolean disable) {
        printLog = !disable;
    }

    public void display(ImageView view, ImageUrl url, Drawable defaultDrawable) {
        ImageUrl originUrl = displayViewMap.put(view, url);
        if (url == null || TextUtils.isEmpty(url.getDownloadUrl()))
        {
            if (defaultDrawable != null) view.setImageDrawable(defaultDrawable);
            return;
        }
        
        if (url.equals(originUrl))
        {
            return;
        }

        Bitmap image = loader.loadImage(url, downloader, new ImageViewCallback(view));
        if (image != null)
        {
            view.setImageBitmap(image);
        }
        else if (defaultDrawable != null)
        {
            view.setImageDrawable(defaultDrawable);
        }
    }
    
    public void save(ImageUrl url, Bitmap image) {
        savedImageMap.put(url, image);
        loader.updateImage(url, null);
        loader.loadImage(url, downloader, null);
    }
    
    public AsyncImageLoader getLoader() {
        return loader;
    }
    
    public ImageStorage getStorage() {
        return storage;
    }

    private class ImageViewCallback implements ImageCallback {

        private final WeakReference<ImageView> callback;

        public ImageViewCallback(ImageView view) {
            callback = new WeakReference<ImageView>(view);
        }

        @Override
        public void imageLoaded(ImageUrl url, Bitmap image) {
            ImageView view = callback.get();
            if (view != null && url.equals(displayViewMap.get(view)))
            {
                if (image != null)
                {
                    view.setImageBitmap(image);
                }
                else
                {
                    displayViewMap.remove(view);
                }
            }
        }
    }
    
    private class MyImageDownloader implements ImageDownloader {
        
        private final SharedPreferences sp;
        
        public MyImageDownloader(Context context) {
            sp = context.getSharedPreferences("image", 0);
        }

        @Override
        public Bitmap imageLoading(ImageUrl url) {
            String downloadUrl = url.getDownloadUrl();
            String fileKey = url.getType() + downloadUrl;
            String crc = url.getCrc();
            
            Bitmap image = savedImageMap.remove(url);
            if (image == null)
            {
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
                    image = downloadImage(downloadUrl, fileKey, crc);
                }
            }
            else if (!TextUtils.isEmpty(crc) && storage.put(fileKey, image))
            {
                updateCrc(fileKey, crc);
            }
            
            if (transformer != null) image = transformer.transform(url, image);
            return image;
        }

        private String getCrcKey(String fileKey) {
            return fileKey;
        }

        /**
         * 比对图片版本号
         * 
         * @return 图片是否有更新
         */
        private boolean checkCrc(String fileKey, String crc) {
            if (TextUtils.isEmpty(crc))
            {
                return true;
            }
            
            return !crc.equals(sp.getString(getCrcKey(fileKey), ""));
        }

        /**
         * 更新图片版本号
         */
        private void updateCrc(String fileKey, String crc) {
            sp.edit().putString(getCrcKey(fileKey), crc).commit();
        }
        
        private Bitmap downloadImage(String downloadUrl, String fileKey, String crc) {
            String tag = "图片下载-" + downloadUrl.hashCode();
            if (printLog) log(tag, downloadUrl);
            try {
                HttpConnector conn = new HttpConnector(downloadUrl);
                conn.getRequest().setHeader("Accept", "*/*");
                
                byte[] bs = conn.connect().getContent();
                Bitmap image = BitmapFactory.decodeByteArray(bs, 0, bs.length);
                if (printLog)
                {
                    log(tag, image == null ? "无图片" : image.getWidth() + "*" + image.getHeight());
                }
                
                if (image != null && !TextUtils.isEmpty(crc) && storage.put(fileKey, bs))
                {
                    updateCrc(fileKey, crc);
                }
                
                return image;
            } catch (Throwable e) {
                // 下载出错
                if (printLog) log(tag, e);
            }
            
            return null;
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
        LogFactory.addLogFile(MyImageDownloader.class, ImageManager.class);
    }
}