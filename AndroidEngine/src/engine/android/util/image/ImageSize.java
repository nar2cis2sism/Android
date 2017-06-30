package engine.android.util.image;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import engine.android.util.ui.ViewSize;
import engine.android.util.ui.ViewSize.ViewHeightAdjuster;
import engine.android.util.ui.ViewSize.ViewWidthAdjuster;

/**
 * A convenient utility to manager image's size.
 * 
 * @author Daimon
 * @version N
 * @since 5/28/2016
 */
public final class ImageSize {
    
    private Pair<Integer, Integer> aspect_ratio;
    
    private int width, height;
    
    public ImageSize() {}
    
    public ImageSize(int width, int height) {
        if ((this.width = width) < 0 || (this.height = height) < 0)
        {
            throw new IllegalArgumentException("什么鬼，图片尺寸哪有负数");
        }
    }

    /**
     * 设置图片宽高比，例如16:9
     */
    public void setAspectRatio(int w, int h) {
        aspect_ratio = Pair.create(w, h);
        
        int width = this.width;
        int height = this.height;
        if (width == 0 && height == 0)
        {
            return;
        }
        
        if (width != 0 && height != 0 && width * h != height * w)
        {
            // 已有图片尺寸
            this.width = this.height = 0;
            return;
        }
        
        if (width == 0)
        {
            setHeight(height);
        }
        else if (height == 0)
        {
            setWidth(width);
        }
    }
    
    public void clearAspectRatio() {
        aspect_ratio = null;
    }
    
    public void setWidth(int width) {
        this.width = width;
        if (aspect_ratio != null)
        {
            height = width * aspect_ratio.second / aspect_ratio.first;
        }
    }
    
    public void setHeight(int height) {
        this.height = height;
        if (aspect_ratio != null)
        {
            width = height * aspect_ratio.first / aspect_ratio.second;
        }
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    /**
     * 根据图片尺寸调整视图大小（视图宽高必需有一个是确定的）
     */
    public static void adjustViewSize(View view, ImageSize size) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) return;
        
        final boolean unknownWidth = params.width == LayoutParams.WRAP_CONTENT;
        final boolean unknownHeight = params.height == LayoutParams.WRAP_CONTENT;
        
        if (unknownWidth && unknownHeight)
        {
            return;
        }
        
        if (unknownWidth)
        {
            ViewSize.adjustViewWidth(view, size.widthAdjuster);
        }
        else if (unknownHeight)
        {
            ViewSize.adjustViewHeight(view, size.heightAdjuster);
        }
    }
    
    final ViewWidthAdjuster widthAdjuster = new ViewWidthAdjuster() {
        
        @Override
        public int adjustWidthByHeight(int height) {
            setHeight(height);
            return getWidth();
        }
    };
    final ViewHeightAdjuster heightAdjuster = new ViewHeightAdjuster() {
        
        @Override
        public int adjustHeightByWidth(int width) {
            setWidth(width);
            return getHeight();
        }
    };
}