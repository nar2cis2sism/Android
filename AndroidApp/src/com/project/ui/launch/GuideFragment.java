package com.project.ui.launch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimon.yueba.R;
import com.project.storage.MySharedPreferences;
import com.project.ui.login.LoginFragment;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseFragment;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.widget.common.layout.FlingLayout;
import engine.android.widget.common.layout.FlingLayout.OnViewChangeListener;
import engine.android.widget.common.layout.PageIndicator;

/**
 * 引导界面
 * 
 * @author Daimon
 */
public class GuideFragment extends BaseFragment implements OnViewChangeListener {
    
    @InjectView(R.id.fling_layout)
    FlingLayout fling_layout;
    @InjectView(R.id.experience)
    ImageView experience;
    @InjectView(R.id.skip)
    ImageView skip;
    @InjectView(R.id.page_indicator)
    PageIndicator page_indicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.guide_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        fling_layout.setOnViewChangeListener(this);
        page_indicator.check(0);
    }

    @Override
    public void OnViewChanged(int childIndex) {
        page_indicator.check(childIndex);
    }
    
    @OnClick({R.id.experience, R.id.skip})
    void finishGuide() {
        MySharedPreferences.getInstance().finishGuide();
        ((SinglePaneActivity) getBaseActivity()).replaceFragment(new LoginFragment());
    }
}