package com.project.ui;

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

import com.daimon.yueba.R;
import com.project.app.MySession;
import com.project.ui.beside.BesideFragment;
import com.project.ui.friend.FriendListFragment;
import com.project.ui.message.MessageListFragment;
import com.project.ui.more.MoreFragment;
import com.project.util.AppUpgradeUtil;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseActivity;
import engine.android.widget.extra.ViewPager;
import protocol.java.json.AppUpgradeInfo;

/**
 * 主界面
 * 
 * @author Daimon
 */
public class MainActivity extends BaseActivity {
    
    /** 外界可以通过传递标签tag设置当前显示页面 **/
    public static final String EXTRA_TAB_TAG        = "tab_tag";
    
    public static final String TAB_TAG_MESSAGE      = "message";         // 消息
    public static final String TAB_TAG_FRIEND       = "friend";          // 好友
    public static final String TAB_TAG_BESIDE       = "beside";          // 身边
    public static final String TAB_TAG_MORE         = "more";            // 更多
    
    private static final int TAB_COUNT              = 4;
    
    private static final String DEFAULT_TAB_TAG     = TAB_TAG_MESSAGE;   // 默认显示标签
    
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
        setupTag(savedInstanceState);
    }
    
    private void setupView() {
        tabHost.setup();
        
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager(), TAB_COUNT);
        // 消息
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(TAB_TAG_MESSAGE)
                .setIndicator(new TabView(this, R.drawable.main_tab_message))
                .setContent(emptyContent), MessageListFragment.class, null));
        // 好友
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(TAB_TAG_FRIEND)
                .setIndicator(new TabView(this, R.drawable.main_tab_friend))
                .setContent(emptyContent), FriendListFragment.class, null));
        // 身边
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(TAB_TAG_BESIDE)
                .setIndicator(new TabView(this, R.drawable.main_tab_beside))
                .setContent(emptyContent), BesideFragment.class, null));
        // 更多
        tabHost.addTab(adapter.addTab(tabHost.newTabSpec(TAB_TAG_MORE)
                .setIndicator(new TabView(this, R.drawable.main_tab_more))
                .setContent(emptyContent), MoreFragment.class, null));
        
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
    
    private void setupTag(Bundle savedInstanceState) {
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
    
    private static class ViewPagerAdapter extends engine.android.widget.extra.ViewPager.ViewPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm, int count) {
            super(fm, count);
        }

        public TabSpec addTab(TabSpec tabSpec, Class<? extends Fragment> c, Bundle args) {
            addPage(tabSpec.getTag(), c, args);
            return tabSpec;
        }
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        AppUpgradeInfo info = MySession.getUpgradeInfo();
        if (info != null)
        {
            AppUpgradeUtil.upgradeApp(this, info, false);
            MySession.setUpgradeInfo(null);
        }
    }
}