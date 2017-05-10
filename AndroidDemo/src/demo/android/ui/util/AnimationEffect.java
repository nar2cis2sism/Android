package demo.android.ui.util;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.dk.animation.folding.FoldingLayout;

public abstract class AnimationEffect {
    
    private int mTop;
    private Bitmap mBitmap;
    private View mView;
    
    public void prepareBitmap(Activity activity) {
        //当前显示的View根（是一个FrameLayout对象，不包括标题栏）
        View root = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        mTop = root.getTop();
        root.setDrawingCacheEnabled(true);
        mBitmap = root.getDrawingCache();
    }
    
    public void prepareTopView(Activity activity) {
        WindowManager.LayoutParams wl = new WindowManager.LayoutParams();
        
        wl.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        wl.format = PixelFormat.TRANSLUCENT;
        
        wl.gravity = Gravity.LEFT | Gravity.TOP;
        wl.x = 0;
        wl.y = mTop;
        wl.width = mBitmap.getWidth();
        wl.height = mBitmap.getHeight();
        
        activity.getWindowManager().addView(mView = createTopView(activity, mBitmap), wl);
    }
    
    public abstract View createTopView(Context context, Bitmap bitmap);
    
    public void animate(Activity activity) {
        animate(activity, mView);
    }
    
    public abstract void animate(Activity activity, View view);
    
    public void cancel(Activity activity) {
        clean(activity);
    }
    
    public void clean(Activity activity) {
        if (mView != null)
        {
            mView.setLayerType(View.LAYER_TYPE_NONE, null);
            // If we use the regular removeView() we'll get a small UI glitch
            activity.getWindowManager().removeViewImmediate(mView);
            mView = null;
        }
        
        mBitmap = null;
    }
    
    public static class FolderAnimationEffect extends AnimationEffect {

        @Override
        public View createTopView(Context context, Bitmap bitmap) {
            FoldingLayout layout = new FoldingLayout(context);
            
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setImageBitmap(bitmap);
            
            layout.addView(imageView);
            
            return layout;
        }

        @Override
        public void animate(final Activity activity, View view) {
            FoldingLayout layout = (FoldingLayout) view;
            
            layout.setNumberOfFolds(8);
            
            ObjectAnimator anim = ObjectAnimator.ofFloat(layout, "foldFactor", 0, 1).setDuration(1000);
            anim.setInterpolator(new AccelerateInterpolator());
            
            anim.addListener(new AnimatorListener() {
                
                @Override
                public void onAnimationStart(Animator animation) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onAnimationRepeat(Animator animation) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    clean(activity);
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {
                    // TODO Auto-generated method stub
                    
                }
            });
            
            anim.start();
        }
    }
}