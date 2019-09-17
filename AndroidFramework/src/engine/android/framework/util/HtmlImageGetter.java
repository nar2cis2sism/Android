package engine.android.framework.util;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.R;
import engine.android.util.image.AsyncImageLoader;
import engine.android.util.image.AsyncImageLoader.ImageCallback;
import engine.android.util.image.AsyncImageLoader.ImageDownloader;
import engine.android.util.image.AsyncImageLoader.ImageUrl;
import engine.android.util.image.ImageSize;
import engine.android.util.io.IOUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

/**
 * 自定义HTML图片替换
 * 
 * @author Daimon
 * @since 6/6/2016
 */
public class HtmlImageGetter implements ImageGetter {
    
    private static final AsyncImageLoader loader = new AsyncImageLoader();
    private static final ImageDownloader downloader = new MyImageDownloader();
    
    private final TextView text;
    private final Resources res;

    public HtmlImageGetter(TextView text) {
        res = (this.text = text).getResources();
    }

    @Override
    public Drawable getDrawable(String source) {
        final LevelListDrawable drawable = new LevelListDrawable();
        
        Bitmap image = loader.loadImage(new ImageUrl(0, source, null), downloader, new ImageCallback() {

            @Override
            public void imageLoaded(ImageUrl url, Bitmap image) {
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
            image = BitmapFactory.decodeResource(res, R.drawable.html_image_loading);
            setLevelDrawable(drawable, image, 0);
        }
        
        return drawable;
    }
    
    @SuppressWarnings("null")
    private void setLevelDrawable(LevelListDrawable drawable, Bitmap image, int level) {
        int width = image.getWidth();
        int height = image.getHeight();
        // 调节图片显示大小
        Integer textH = (Integer) text.getTag(R.id.view_tag);
        if (textH == null)
        {
            if ((textH = (int) (text.getLineHeight() * 1.5f)) > 0)
            {
                // 保存高度避免多次计算不一致
                text.setTag(R.id.view_tag, textH);
            }
        }
        
        if (height < textH)
        {
            ImageSize size = new ImageSize(0, textH);
            size.setAspectRatio(width, height);
            
            width = size.getWidth();
            height = size.getHeight();
        }
        //
        drawable.setBounds(0, 0, width, height);
        drawable.addLevel(level, level, new BitmapDrawable(res, image));
        drawable.setLevel(level);
    }
    
    private static class MyImageDownloader implements ImageDownloader {

        @Override
        public Bitmap imageLoading(ImageUrl url) {
            InputStream is = null;
            try {
                is = new URL(url.getDownloadUrl()).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (Throwable e) {
                LOG.log(e);
            } finally {
                IOUtil.closeSilently(is);
            }
            
            return null;
        }
    }
}