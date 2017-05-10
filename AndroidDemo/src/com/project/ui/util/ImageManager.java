package com.project.ui.util;

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

import com.project.MyConfiguration.MyConfiguration_APP;
import com.project.MyConfiguration.MyConfiguration_NET;
import com.project.MyContext;

import demo.android.R;
import engine.android.core.ApplicationManager;
import engine.android.http.HttpRequest;
import engine.android.util.Singleton;
import engine.android.util.image.AsyncImageLoader;
import engine.android.util.image.AsyncImageLoader.ImageCallback;
import engine.android.util.image.AsyncImageLoader.ImageDownloader;
import engine.android.util.image.ImageStorage;
import engine.android.util.io.IOUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.WeakHashMap;

/**
 * 图片统一管理
 * 
 * @author Daimon
 */

public class ImageManager {
    
    private static final Singleton<ImageManager> instance
    = new Singleton<ImageManager>() {
        
        @Override
        protected ImageManager create() {
            return new ImageManager(MyContext.getContext());
        }
    };
    
    public static final ImageManager getInstance() {
        return instance.get();
    }

    /******************************** 华丽丽的分割线 ********************************/

    private final AsyncImageLoader loader;
    
    private final ImageDownloader downloader;

    final WeakHashMap<View, ImageUrl> displayViewMap;
    
    final ImageStorage storage;
    
    ImageManager(Context context) {
        loader = new AsyncImageLoader();
        downloader = new MyImageDownloader(context);
        displayViewMap = new WeakHashMap<View, ImageUrl>();
        storage = new ImageStorage(context.getDir("image", 0));

        if (MyConfiguration_APP.APP_TESTING)
        {
            storage.clear();
        }
    }

    public void display(ImageView view, ImageUrl url, Drawable defaultDrawable) {
        if (url.equals(displayViewMap.put(view, url)))
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
            if (view.getDrawable() == null)
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

        private final boolean printLog
        = !ApplicationManager.isDebuggable() ? false :
                                               true; // Modify it for test
        
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
                if (MyConfiguration_NET.NET_OFF)
                {
                    SystemClock.sleep(2000);
                    image = ApplicationManager.loadImage(R.drawable.ic_launcher);
                }
                else
                {
                    String downloadUrl = imageUrl.getDownloadUrl();
                    if (printLog)
                        log("图片网络下载-" + fileKey, downloadUrl);

                    HttpRequest request = new HttpRequest(downloadUrl);
                    request.setHeader("Accept", "*/*");
                    try {
                        HttpURLConnection conn = HttpRequest.connect(request);
                        InputStream is = conn.getInputStream();
                        byte[] bs = IOUtil.readStream(is);
                        image = BitmapFactory.decodeByteArray(bs, 0, bs.length);
                        if (image != null)
                        {
                            if (printLog)
                                log("图片网络下载-" + fileKey,  "成功下载图片:" + 
                                    image.getWidth() + "*" + image.getHeight());

                            if (storage.put(fileKey, bs))
                            {
                                sp.edit().putString(getCrcKey(fileKey), crc).commit();
                            }
                        }
                    } catch (Exception e) {
                        if (printLog)
                            log("图片下载出错-" + fileKey, e);
                    } finally {
                        request.close();
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
                log("图片版本校验-" + fileKey, nativeCrc + "->" + crc + 
                        "=change:" + change);

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
}