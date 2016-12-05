package com.project.ui.launch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.project.R;
import com.project.ui.login.LoginFragment;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseFragment;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.widget.FlingLayout;
import engine.android.widget.FlingLayout.OnViewChangeListener;
import engine.android.widget.PageIndicator;

/**
 * 引导界面
 * 
 * @author Daimon
 */
public class GuideFragment extends BaseFragment implements OnViewChangeListener, OnClickListener {
    
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
        experience.setOnClickListener(this);
        skip.setOnClickListener(this);
        page_indicator.check(0);
    }

    @Override
    public void OnViewChanged(int childIndex) {
        page_indicator.check(childIndex);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.experience:
                // 立即体验
            case R.id.skip:
                // 跳过
                finishGuide();
                break;
        }
    }
    
    private void finishGuide() {
//        MySharedPreferences.getInstance().finishGuide();
        
        startActivity(SinglePaneActivity.buildIntent(
                getContext(), LoginFragment.class, null));
        finish();
    }
}