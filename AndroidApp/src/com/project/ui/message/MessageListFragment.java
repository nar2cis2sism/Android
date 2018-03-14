package com.project.ui.message;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

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
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        getBaseActivity().startFragment(MessageFragment.class);
    }
}