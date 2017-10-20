package com.project.ui.launch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.project.R;
import com.project.storage.MySharedPreferences;
import com.project.ui.login.LoginFragment;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseFragment;
import engine.android.widget.PageIndicator;
import engine.android.widget.common.FlingLayout;
import engine.android.widget.common.FlingLayout.OnViewChangeListener;

/**
 * 引导界面
 * 
 * @author Daimon
 */
public class GuideFragment extends BaseFragment implements OnViewChangeListener {
    
    @InjectView(R.id.fling_layout)
    FlingLayout fling_layout;               // 引导页
    
    @InjectView(R.id.experience)
    ImageView experience;                   // 立即体验按钮
    
    @InjectView(R.id.skip)
    ImageView skip;                         // 右上角的跳过按钮

    @InjectView(R.id.page_indicator)
    PageIndicator page_indicator;           // 底部的页面指示器

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
        
        getBaseActivity().startFragment(LoginFragment.class);
        finish();
    }
}