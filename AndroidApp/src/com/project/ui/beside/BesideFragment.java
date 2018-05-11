package com.project.ui.beside;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daimon.yueba.R;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.AndroidUtil;
import engine.android.widget.common.layout.ActionContainer;
import engine.android.widget.component.TitleBar;

/**
 * 身边界面
 * 
 * @author Daimon
 */
public class BesideFragment extends BaseFragment {
    
    @InjectView(R.id.action_container)
    ActionContainer action_container;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar.setTitle("身边").show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beside_fragment, container, false);
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