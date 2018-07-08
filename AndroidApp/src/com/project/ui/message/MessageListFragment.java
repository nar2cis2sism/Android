package com.project.ui.message;

import static com.project.app.event.Events.CONNECTIVITY_CHANGE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimon.yueba.R;

import engine.android.framework.ui.BaseListFragment;
import engine.android.http.HttpConnector;
import engine.android.widget.component.TitleBar;

/**
 * 消息列表界面
 * 
 * @author Daimon
 */
public class MessageListFragment extends BaseListFragment {
    
    FrameLayout list_header;
    TextView tip_no_connection;
    
    MessageListPresenter presenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(MessageListPresenter.class);
        
        enableReceiveEvent(CONNECTIVITY_CHANGE);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.message_title)
        .addAction(R.drawable.message_add)
        .show();
    }
    
    @Override
    protected void setupListView(ListView listView) {
        listView.addHeaderView(onCreateListHeader());
    }
    
    private View onCreateListHeader() {
        list_header = new FrameLayout(getContext());
        tip_no_connection = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tip_no_connection, null);
        showConnectionTip(!HttpConnector.isAccessible(getContext()));
        return list_header;
    }
    
    /**
     * 显示/隐藏无网络提示
     */
    private void showConnectionTip(boolean noConnection) {
        list_header.removeAllViews();
        if (noConnection)
        {
            list_header.addView(tip_no_connection);
        }
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        getBaseActivity().startFragment(MessageFragment.class);
    }
    
    @Override
    protected void onReceiveSuccess(String action, Object param) {
        if (CONNECTIVITY_CHANGE.equals(action))
        {
            showConnectionTip(!(Boolean) param);
        }
    }
}