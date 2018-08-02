package com.project.ui.login.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daimon.yueba.R;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.extra.BaseInfoFragment;

/**
 * 注册流程公共基类
 * 
 * @author Daimon
 */
public abstract class RegisterInfoFragment extends BaseInfoFragment {

    @InjectView(R.id.introduction)
    TextView introduction;
    
    @InjectView(R.id.content)
    FrameLayout content;
    
    @InjectView(R.id.next)
    Button next;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_info_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        int resid = getIntroductionResId();
        if (resid == 0)
        {
            introduction.setVisibility(View.GONE);
        }
        else
        {
            introduction.setText(resid);
        }
    
        setupContent(content);
        
        OnClickListener listener = getNextListener();
        if (listener != null)
        {
            next.setOnClickListener(listener);
        }
        else
        {
            next.setVisibility(View.GONE);
        }
    }
    
    protected int getIntroductionResId() {
        return 0;
    }
    
    protected abstract void setupContent(FrameLayout content);

    protected OnClickListener getNextListener() {
        return null;
    }
}