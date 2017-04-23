package com.project.ui.module.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.project.R;
import com.project.ui.module.friend.list.FriendListFragment;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.BaseListFragment;
import engine.android.plugin.Plugin;
import engine.android.widget.ViewPager;

/**
 * 主界面
 * 
 * @author Daimon
 */
public class MainActivity extends BaseActivity {
    
    /** 外界可以通过传递标签tag设置当前显示页面 **/
    public static final String EXTRA_TAB_TAG        = "tab_tag";
    
    private static final int TAB_COUNT = 4;
    
    public static final String EXTRA_TAB_MESSAGE    = "message";         // 消息
    public static final String EXTRA_TAB_FRIEND     = "friend";          // 好友
    public static final String EXTRA_TAB_BESIDE     = "beside";          // 身边
    public static final String EXTRA_TAB_MORE       = "more";            // 更多
    
    private static final String DEFAULT_TAB_TAG     = EXTRA_TAB_MESSAGE; // 默认显示标签
    
    private static final String SAVED_TAB_TAG       = EXTRA_TAB_TAG;
    
    @InjectView(android.R.id.tabhost)
    TabHost tabHost;

    @InjectView(R.id.pager)
    ViewPager pager;
    
    private final TabContentFactory emptyContent = new TabContentFactory() {
        
        @Override
        public View createTabContent(String tag) {
            View v = new View(MainActivity.this);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        setupView();
        
        String tag = null;
        
        if (savedInstanceState != null)
        {
            tag = savedInstanceState.getString(SAVED_TAB_TAG);
        }
        else
        {
            tag = getIntent().getStringExtra(EXTRA_TAB_TAG);
        }
        
        if (tag == null)
        {
            tag = DEFAULT_TAB_TAG;
        }
        
        tabHost.setCurrentTabByTag(tag);
    }
    
    private void setupView() {
        tabHost.setup();
        
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager(), TAB_COUNT);
        // 消息
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(EXTRA_TAB_MESSAGE)
                .setIndicator(new TabView(this, R.drawable.tab_message))
                .setContent(emptyContent), FriendListFragment.class, null));
        // 好友
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(EXTRA_TAB_FRIEND)
                .setIndicator(new TabView(this, R.drawable.tab_friend))
                .setContent(emptyContent), BaseListFragment.class, null));
        // 身边
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(EXTRA_TAB_BESIDE)
                .setIndicator(new TabView(this, R.drawable.tab_beside))
                .setContent(emptyContent), loadBesideClass(), null));
        // 更多
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(EXTRA_TAB_MORE)
                .setIndicator(new TabView(this, R.drawable.tab_more))
                .setContent(emptyContent), BaseListFragment.class, null));
        
        pager.setSlidable(false);
        pager.setOffscreenPageLimit(adapter.getCount() - 1);
        pager.setAdapter(adapter);
        
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            
            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
            }
        });
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                pager.setCurrentItem(tabHost.getCurrentTab());
            }
        });
    }
    
    private Class<? extends Fragment> loadBesideClass() {
        try {
            return (Class<? extends Fragment>) Class.forName("com.project.beside.ui.BesideFragment", 
                    true, Plugin.getPluginLoader("com.project.beside").getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_TAB_TAG, tabHost.getCurrentTabTag());
    }
    
    private static class TabView extends RelativeLayout {

        public TabView(Context context, int iconId) {
            super(context);
            
            ImageView iv = new ImageView(context);
            iv.setImageResource(iconId);
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            
            addView(iv, params);
        }
    }
    
    private static class ViewPagerAdapter extends engine.android.widget.ViewPager.ViewPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm, int count) {
            super(fm, count);
        }

        public TabSpec addTab(TabSpec tabSpec, Class<? extends Fragment> c, Bundle args) {
            addPage(tabSpec.getTag(), c, args);
            return tabSpec;
        }
    }
}