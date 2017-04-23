package com.project.beside.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.project.beside.R;

import engine.android.core.ApplicationManager;
import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseFragment;
import engine.android.plugin.Plugin;
import engine.android.util.AndroidUtil;
import engine.android.widget.ActionContainer;
import engine.android.widget.TitleBar;

/**
 * 身边界面
 * 
 * @author Daimon
 */
public class BesideFragment extends BaseFragment {
    
    /**
     * 由于此界面是作为插件存在的，需注意绑定的Activity存在于宿主，故无法通过常规方法获取资源
     */
    private Context context;
    @Override
    public Context getContext() {
        if (context == null)
        {
            context = Plugin.getPlugin("com.project.beside").getApplication();
        }
        
        return context;
    }
    
    @InjectView(R.id.action_container)
    ActionContainer action_container;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar.setTitle("身边").show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(
                R.layout.beside_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupActionContainer(action_container);
    }
    
    private void setupActionContainer(ActionContainer action_container) {
        int paddingVertical = AndroidUtil.dp2px(getContext(), 36);
        action_container.addAction(null, "监测预警").setPadding(0, paddingVertical, 0, paddingVertical);
        action_container.addAction(null, "图文资讯");
        action_container.addAction(null, "电话资讯");
        
        action_container.setDividerDrawable(getContext().getResources()
                .getDrawable(R.color.divider_horizontal));
        action_container.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    }
}