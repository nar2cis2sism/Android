package demo.fragment.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

import demo.android.R;
import demo.fragment.AppListFragment;
import demo.fragment.TitlesFragment;

import java.util.LinkedList;

public class ViewPagerActivity extends FragmentActivity {
    
    private static final String SAVE_TAB_INDEX = "tab_index";
    
    TabHost tabHost;
    
    ImageView select_tab;
    int select_index = 1;
    boolean initTab;
    
    int tabStart;
    int tabTop;
    
    private TabContentFactory emptyContent = new TabContentFactory() {
        
        @Override
        public View createTabContent(String tag) {
            View v = new View(ViewPagerActivity.this);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    };
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.viewpager_activity);
        
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec("1")
                .setIndicator(new TabView(this, R.drawable.viewpager_tab1))
                .setContent(emptyContent), TitlesFragment.class, null));
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec("2")
                .setIndicator(new TabView(this, R.drawable.viewpager_tab2))
                .setContent(emptyContent), AppListFragment.class, null));
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec("3")
                .setIndicator(new TabView(this, R.drawable.viewpager_tab3))
                .setContent(emptyContent), TitlesFragment.class, null));
        
        pager.setAdapter(adapter);
        
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            
            @Override
            public void onPageSelected(int position) {
                //通过阻止TabWidget作用焦点使焦点始终停留在ViewPager上
                TabWidget widget = tabHost.getTabWidget();
                int focusability = widget.getDescendantFocusability();
                widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                tabHost.setCurrentTab(position);
                widget.setDescendantFocusability(focusability);
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                pager.setCurrentItem(selectTab(tabHost.getCurrentTab()));
            }
        });
        
        if (arg0 != null)
        {
            select_index = arg0.getInt(SAVE_TAB_INDEX);
        }
        
        tabHost.setCurrentTab(select_index);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_TAB_INDEX, select_index);
    }
    
    private void initTab()
    {
        if (initTab)
        {
            return;
        }
        
        initTab = true;
        TabWidget tabWidget = tabHost.getTabWidget();
        TabView tabView = (TabView) tabWidget.getChildTabViewAt(select_index);
        
        select_tab = new ImageView(this);
        select_tab.setImageResource(R.drawable.viewpager_tab_select);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        
        tabView.addView(select_tab, params);
    }
    
    /**
     * Tab动画
     */
    
    private int selectTab(int index)
    {
        if (select_tab == null)
        {
            initTab();
        }
        else
        {
            TabWidget tabWidget = tabHost.getTabWidget();
            if (initTab)
            {
                TabView tabView = (TabView) tabWidget.getChildTabViewAt(select_index);
                tabStart = tabView.getLeft() + select_tab.getLeft();
                tabTop = select_tab.getTop();
                
                tabView.removeView(select_tab);
                initTab = false;
                
                tabHost.addView(select_tab, 0/*故意的*/, new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }

            View tabView = tabWidget.getChildTabViewAt(index);
            int end = tabView.getLeft() + (tabView.getWidth() - select_tab.getWidth()) / 2;
            
            TranslateAnimation anim = new TranslateAnimation(tabStart, tabStart = end, tabTop, tabTop);
            anim.setDuration(400);
            anim.setFillAfter(true);
            
            select_tab.bringToFront();/*故意的*/
            select_tab.startAnimation(anim);
        }
        
        return select_index = index;
    }
    
    private static class TabView extends RelativeLayout {

        public TabView(Context context, int iconId) {
            super(context);
            
            ImageView iv = new ImageView(context);
            iv.setImageResource(iconId);
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            
            addView(iv, params);
        }
    }
    
    private static final class TabInfo {
        
        public String tag;
        public Class<?> c;
        public Bundle args;
        
        public TabInfo(String tag, Class<?> c, Bundle args) {
            this.tag = tag;
            this.c = c;
            this.args = args;
        }
    }
    
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        
        private Context context;
        
        private LinkedList<TabInfo> tabs = new LinkedList<TabInfo>();
        
        public ViewPagerAdapter(FragmentActivity context) {
            super(context.getSupportFragmentManager());
            this.context = context;
        }

        public TabSpec addTab(TabSpec tabSpec, Class<?> c, Bundle args)
        {
            tabs.add(new TabInfo(tabSpec.getTag(), c, args));
            return tabSpec;
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = tabs.get(position);
            return Fragment.instantiate(context, info.c.getName(), info.args);
        }

        @Override
        public int getCount() {
            return tabs.size();
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            if (position < select_index)
            {
                return "Prev";
            }
            else if (position > select_index)
            {
                return "Next";
            }
            else
            {
                TabInfo info = tabs.get(position);
                return info.tag;
            }
        }
    }
}