package com.project.ui.more.authentication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimon.yueba.R;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseFragment;
import engine.android.widget.component.TitleBar;

/**
 * 实名认证完成
 * 
 * @author Daimon
 */
public class AuthenticationFinishFragment extends BaseFragment implements Runnable {
    
    @InjectView(R.id.tip)
    TextView tip;
    
    // 计时3秒后关闭页面
    int seconds = 3;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.authentication_title)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authentication_finish_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTip();
    }
    
    private void setupTip() {
        String authentication_finish = getString(R.string.authentication_finish);
        int index = authentication_finish.indexOf("%");
        
        SpannableString span = new SpannableString(String.format(authentication_finish, seconds));
        span.setSpan(new ForegroundColorSpan(Color.RED), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        tip.setText(span);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getBaseActivity().postSchedule(this, 1000);
    }

    @Override
    public void run() {
        if (--seconds == 0)
        {
            finish();
        }
        else
        {
            setupTip();
        }
    }
}