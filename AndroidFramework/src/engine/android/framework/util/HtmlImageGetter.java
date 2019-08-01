package engine.android.framework.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.R;
import engine.android.util.image.AsyncImageLoader;
import engine.android.util.image.ImageSize;
import engine.android.util.image.AsyncImageLoader.ImageCallback;
import engine.android.util.image.AsyncImageLoader.ImageDownloader;
import engine.android.util.io.IOUtil;

import java.io.InputStream;
import java.net.URL;

/**
 * 自定义HTML图片替换
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2016
 */
public class HtmlImageGetter implements ImageGetter {
    
    private static final AsyncImageLoader loader = new AsyncImageLoader();
    
    private static final ImageDownloader downloader = new MyImageDownloader();
    
    private final Context context;
    
    private final TextView text;

    public HtmlImageGetter(TextView text) {
        context = (this.text = text).getContext();
    }

    @Override
    public Drawable getDrawable(String source) {
        final LevelListDrawable drawable = new LevelListDrawable();
        
        Bitmap image = loader.loadImage(source, downloader, new ImageCallback() {
            
            @Override
            public void imageLoaded(Object url, Bitmap image) {
                if (image != null)
                {
                    setLevelDrawable(drawable, image, 1);
                    text.setText(text.getText());
                }
            }
        });
        
        if (image != null)
        {
            setLevelDrawable(drawable, image, 1);
        }
        else
        {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.html_image_loading);
            setLevelDrawable(drawable, image, 0);
        }
        
        return drawable;
    }
    
    private void setLevelDrawable(LevelListDrawable drawable, Bitmap image, int level) {
        drawable.addLevel(level, level, new BitmapDrawable(context.getResources(), image));
        drawable.setLevel(level);
        // 调节图片显示大小
        int textH = text.getHeight();
        if (textH <= 0)
        {
            textH = text.getMeasuredHeight();
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (height < textH)
        {
            ImageSize size = new ImageSize(0, textH);
            size.setAspectRatio(width, height);
            
            width = size.getWidth();
            height = size.getHeight();
        }

        drawable.setBounds(0, 0, width, height);
    }
    
    private static class MyImageDownloader implements ImageDownloader {

        @Override
        public Bitmap imageLoading(Object url) {
            InputStream is = null;
            try {
                is = new URL((String) url).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                LOG.log("HtmlImageGetter", url + "|" + e);
            } finally {
                IOUtil.closeSilently(is);
            }
            
            return null;
        }
    }
}