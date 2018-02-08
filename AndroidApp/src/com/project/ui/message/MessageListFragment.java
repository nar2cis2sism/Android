package com.project.ui.message;

import android.os.Bundle;

import com.daimon.yueba.R;

import engine.android.framework.ui.BaseListFragment;
import engine.android.widget.TitleBar;

/**
 * 消息列表界面
 * 
 * @author Daimon
 */
public class MessageListFragment extends BaseListFragment {
    
    MessageListPresenter presenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(MessageListPresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.message_title)
        .addAction(R.drawable.message_add)
        .show();
    }
}