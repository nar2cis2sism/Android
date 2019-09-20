package com.project.ui.message;

import static engine.android.framework.app.event.Events.CONNECTIVITY_CHANGE;

import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.BaseListFragment;
import engine.android.http.HttpConnector;
import engine.android.util.ui.UIUtil;
import engine.android.widget.common.list.PinnedHeaderListView;
import engine.android.widget.component.TitleBar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.storage.db.Friend;
import com.project.ui.message.conversation.ConversationActivity;
import com.project.ui.message.conversation.ConversationActivity.ConversationParams;

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
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.message_title)
        .addAction(R.drawable.message_add)
        .show();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        
        PinnedHeaderListView listView = new PinnedHeaderListView(getContext());
        listView.setPinnedHeaderView(R.layout.base_list_category);
        
        UIUtil.replace(root.findViewById(android.R.id.list), listView, null);
        return root;
    }
    
    @Override
    protected void setupListView(ListView listView) {
        listView.addHeaderView(onCreateListHeader());
    }
    
    private View onCreateListHeader() {
        list_header = new FrameLayout(getContext());
        list_header.addView(tip_no_connection = (TextView) LayoutInflater.from(getContext())
                .inflate(R.layout.tip_no_connection, list_header, false));
        showConnectionTip(!HttpConnector.isAccessible(getContext()));
        return list_header;
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Friend friend = presenter.adapter.getItem(position).friend;
        if (friend != null)
        {
            startActivity(ConversationActivity.buildIntent(
                    getContext(), new ConversationParams(friend.account)));
        }
    }
    
    /**
     * 显示/隐藏无网络提示
     */
    void showConnectionTip(boolean noNetwork) {
        tip_no_connection.setVisibility(noNetwork ? View.VISIBLE : View.GONE);
    }

    @Override
    protected EventHandler registerEventHandler() {
        return new EventHandler();
    }
    
    private class EventHandler extends BaseActivity.EventHandler {
        
        public EventHandler() {
            super(CONNECTIVITY_CHANGE);
        }
        
        @Override
        protected void onReceive(String action, int status, Object param) {
            showConnectionTip((Boolean) param);
        }
    }
}