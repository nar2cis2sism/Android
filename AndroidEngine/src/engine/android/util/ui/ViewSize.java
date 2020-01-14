package engine.android.util.ui;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * A convenient utility to manage view's size.
 * 
 * @author Daimon
 * @since 5/28/2016
 */
public class ViewSize {
    
    public final int width;
    public final int height;
    
    public ViewSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public interface ViewSizeObserver {
        
        void onSizeChanged(View view, ViewSize size);
    }
    
    public static abstract class ViewWidthAdjuster implements ViewSizeObserver {
        
        public abstract int adjustWidthByHeight(int height);

        @Override
        public void onSizeChanged(View view, ViewSize size) {
            LayoutParams params = view.getLayoutParams();
            params.width = adjustWidthByHeight(size.height);
            view.setLayoutParams(params);
        }
    }
    
    public static abstract class ViewHeightAdjuster implements ViewSizeObserver {
        
        public abstract int adjustHeightByWidth(int width);

        @Override
        public void onSizeChanged(View view, ViewSize size) {
            LayoutParams params = view.getLayoutParams();
            params.height = adjustHeightByWidth(size.width);
            view.setLayoutParams(params);
        }
    }
    
    public static void adjustViewWidth(View view, ViewWidthAdjuster adjuster) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewSizeListener(
                view, adjuster, false, true));
    }
    
    public static void adjustViewHeight(View view, ViewHeightAdjuster adjuster) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewSizeListener(
                view, adjuster, true, false));
    }
    
    public static void observeViewSize(View view, ViewSizeObserver observer) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewSizeListener(
                view, observer, true, true));
    }
    
    private static class ViewSizeListener implements OnGlobalLayoutListener {
        
        private final View view;
        private final ViewSizeObserver observer;
        
        private final boolean listenWidth;
        private final boolean listenHeight;
        
        private int width = -1;
        private int height = -1;
        
        /**
         * @param listenWidth,listenHeight 决不能都为false
         */
        public ViewSizeListener(View view, ViewSizeObserver observer, 
                boolean listenWidth, boolean listenHeight) {
            this.view = view;
            this.observer = observer;
            this.listenWidth = listenWidth;
            this.listenHeight = listenHeight;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onGlobalLayout() {
            int width = view.getWidth();
            int height = view.getHeight();
            
            if (listenWidth && listenHeight)
            {
                if (width == this.width && height == this.height)
                {
                    observer.onSizeChanged(view, new ViewSize(width, height));
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                else
                {
                    this.width = width;
                    this.height = height;
                }
            }
            else
            {
                if ((listenWidth && width == this.width) || (listenHeight && height == this.height))
                {
                    observer.onSizeChanged(view, new ViewSize(width, height));
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                else
                {
                    this.width = width;
                    this.height = height;
                }
            }
        }
    }
}