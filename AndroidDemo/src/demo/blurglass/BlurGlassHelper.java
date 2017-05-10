package demo.blurglass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class BlurGlassHelper {
    
    private final ImageView blurGlass;
    private View content;
    
    private Bitmap blurBuffer;
    
    private int blurRadius = BlurGlassUtil.DEFAULT_BLUR_RADIUS;
    private int downSampling = BlurGlassUtil.DEFAULT_DOWNSAMPLING;
    
    private Drawable windowBackground;
    
    public BlurGlassHelper(ImageView blurGlass) {
        this.blurGlass = blurGlass;
    }
    
    public void setContentView(final View contentView) {
        Context context = contentView.getContext();
        
        int[] attrs = {android.R.attr.windowBackground};
        
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attrs[0], outValue, true);
        
        TypedArray a = context.obtainStyledAttributes(outValue.resourceId, attrs);
        windowBackground = a.getDrawable(0);
        a.recycle();
        
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
            
            @Override
            public void onGlobalLayout() {
                contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                
                if (contentView == content)
                {
                    invalidate();
                }
            }
        });
        
        content = contentView;
        
        delayInvalidate();
    }

    public void setBlurRadius(int blurRadius) {
        if (!BlurGlassUtil.isBlurRadiusValid(blurRadius))
        {
            throw new IllegalArgumentException("Invalid blur radius.");
        }
        
        if (blurRadius == this.blurRadius)
        {
            return;
        }
        
        this.blurRadius = blurRadius;
        delayInvalidate();
    }
    
    public int getBlurRadius() {
        return blurRadius;
    }
    
    public void setDownSampling(int downSampling) {
        if (!BlurGlassUtil.isDownSamplingValid(downSampling))
        {
            throw new IllegalArgumentException("Invalid downSampling.");
        }
        
        if (downSampling == this.downSampling)
        {
            return;
        }
        
        this.downSampling = downSampling;
        delayInvalidate();
    }
    
    public int getDownSampling() {
        return downSampling;
    }
    
    private void delayInvalidate() {
        if (blurBuffer != null)
        {
            invalidate();
        }
    }
    
    public void invalidate() {
        if (content == null)
        {
            throw new NullPointerException("Please set a content view to make blurred.");
        }
        
        blurBuffer = null;
        computeBlurGlass();
        updateBlurGlass();
    }

    private void computeBlurGlass() {
        final int width = blurGlass.getWidth();
        final int height = blurGlass.getHeight();
        
        if (width == 0 || height == 0)
        {
            return;
        }
        
        final int left = blurGlass.getLeft() - content.getLeft() + content.getScrollX();
        final int top = blurGlass.getTop() - content.getTop() + content.getScrollY();
        
        Bitmap image = drawView2Bitmap(content, downSampling, windowBackground, left, top, width, height);
        blurBuffer = BlurGlass.apply(content.getContext(), image, blurRadius);
        image.recycle();
        
        if (downSampling == 1)
        {
            return;
        }
        
        image = Bitmap.createBitmap(blurBuffer, 0, 0, width / downSampling, height / downSampling);
        blurBuffer.recycle();
        blurBuffer = Bitmap.createScaledBitmap(image, width, height, false);
        image.recycle();
    }
    
    private static Bitmap drawView2Bitmap(View v, int downSampling, Drawable background, int x, int y, int w, int h)
    {
        float scale = 1f / downSampling;
        
        Bitmap image = Bitmap.createBitmap((int) (w * scale), (int) (h * scale), Bitmap.Config.ARGB_8888);
        
        Canvas c = new Canvas(image);
        
        c.scale(scale, scale);
        c.translate(-x, -y);
        
        if (background != null)
        {
            if (background.getBounds().isEmpty())
            {
                background.setBounds(0, 0, w, h);
            }
            
            background.draw(c);
        }

        v.draw(c);
        
        return image;
    }

    private void updateBlurGlass() {
        if (blurBuffer == null)
        {
            return;
        }

        blurGlass.setImageBitmap(blurBuffer);
    }
}