package engine.android.widget;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;

import engine.android.core.extra.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * 自定义可滑动控制
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class ViewPager extends android.support.v4.view.ViewPager {
    
    private boolean isSlidable = true;

    public ViewPager(Context context) {
        super(context);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setSlidable(boolean isSlidable) {
        this.isSlidable = isSlidable;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (isSlidable) return super.onTouchEvent(arg0);
        return false;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isSlidable) return super.onInterceptTouchEvent(arg0);
        return false;
    }
    
    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        
        private class PageInfo {
            
            public final String tag;
            public final Class<?> c;
            public final Bundle args;
            
            public PageInfo(String tag, Class<?> c, Bundle args) {
                this.tag = tag;
                this.c = c;
                this.args = args;
            }
        }
        
        private final Context context;
        
        private final ArrayList<PageInfo> pages;

        public ViewPagerAdapter(Activity context) {
            this(context, 0);
        }

        public ViewPagerAdapter(Activity context, int count) {
            super(context.getFragmentManager());
            this.context = context;
            pages = new ArrayList<PageInfo>(count);
        }

        public void addPage(String tag, Class<?> c, Bundle args) {
            pages.add(new PageInfo(tag, c, args));
        }
        
        public String getTag(int position) {
            return pages.get(position).tag;
        }

        @Override
        public Fragment getItem(int position) {
            PageInfo info = pages.get(position);
            return Fragment.instantiate(context, info.c.getName(), info.args);
        }

        @Override
        public int getCount() {
            return pages.size();
        }
    }
}