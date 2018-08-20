package engine.android.widget.extra;

import android.app.Fragment;
import android.app.Fragment.InstantiationException;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

import engine.android.core.extra.FragmentPagerAdapter;

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
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isSlidable) return super.onInterceptTouchEvent(arg0);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (isSlidable) return super.onTouchEvent(arg0);
        return false;
    }
    
    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        
        private class PageInfo {
            
            public final String tag;
            public final Class<? extends Fragment> c;
            public final Bundle args;
            
            public PageInfo(String tag, Class<? extends Fragment> c, Bundle args) {
                this.tag = tag;
                this.c = c;
                this.args = args;
            }
        }
        
        private final ArrayList<PageInfo> pages;

        public ViewPagerAdapter(FragmentManager fm) {
            this(fm, 0);
        }

        public ViewPagerAdapter(FragmentManager fm, int count) {
            super(fm);
            pages = new ArrayList<PageInfo>(count);
        }

        public void addPage(String tag, Class<? extends Fragment> c, Bundle args) {
            pages.add(new PageInfo(tag, c, args));
        }
        
        public String getTag(int position) {
            return pages.get(position).tag;
        }

        @Override
        public Fragment getItem(int position) {
            PageInfo info = pages.get(position);
            return instantiate(info.c, info.args);
        }

        @Override
        public int getCount() {
            return pages.size();
        }
        
        private Fragment instantiate(Class<? extends Fragment> clazz, Bundle args) {
            try {
                Fragment f = clazz.newInstance();
                if (args != null)
                {
                    args.setClassLoader(f.getClass().getClassLoader());
                    f.setArguments(args);
                }
                
                return f;
            } catch (Exception e) {
                throw new InstantiationException("Unable to instantiate fragment " + clazz.getName()
                        + ": make sure class name exists, is public, and has an"
                        + " empty constructor that is public", e);
            }
        }
    }
}