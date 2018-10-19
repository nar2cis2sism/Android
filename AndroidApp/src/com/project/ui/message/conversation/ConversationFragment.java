package com.project.ui.message.conversation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.network.action.socket.SendMessage;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseListFragment;
import engine.android.widget.component.ConversationBar;
import engine.android.widget.component.TitleBar;

/**
 * 聊天界面
 * 
 * @author Daimon
 */
class ConversationFragment extends BaseListFragment {
    
    @InjectView(R.id.conversation_bar)
    ConversationBar conversation_bar;
    
    ConversationPresenter presenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(ConversationPresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .show();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupConversationBar(conversation_bar);
    }
    
    @Override
    protected void setupListView(ListView listView) {
        listView.setDivider(null);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
    }
    
    private void setupConversationBar(ConversationBar conversation_bar) {
        conversation_bar.setCallback(new ConversationBar.Callback() {
            
            @Override
            public void onSendMessage(CharSequence message) {
                Message msg = MessageDAO.sendMessage(presenter.params.account, message.toString());
                getBaseActivity().sendSocketRequest(new SendMessage(msg));
            }
        });
    }
    
    @Override
    public void setListAdapter(ListAdapter adapter) {
        super.setListAdapter(adapter);
        getTitleBar().setTitle(presenter.params.friend.displayName);
    }
}