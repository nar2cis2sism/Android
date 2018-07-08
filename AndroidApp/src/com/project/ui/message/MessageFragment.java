package com.project.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;
import com.project.network.action.socket.SendMessage;
import com.project.storage.dao.MessageDAO;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseListFragment;
import engine.android.widget.component.ConversationBar;
import engine.android.widget.component.TitleBar;

/**
 * 聊天界面
 * 
 * @author Daimon
 */
public class MessageFragment extends BaseListFragment {
    
    @InjectView(R.id.conversation_bar)
    ConversationBar conversation_bar;
    
    MessagePresenter presenter;
    
    public static Bundle buildParam(FriendListItem item) {
        return MessageParam.build(item);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(MessagePresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(presenter.param.friend.getName())
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
        listView.setStackFromBottom(true);
    }
    
    private void setupConversationBar(ConversationBar conversation_bar) {
        conversation_bar.setCallback(new ConversationBar.Callback() {
            
            @Override
            public void onSendMessage(CharSequence message) {
                sendMessageAction(message.toString());
            }
        });
    }

    /******************************* 发送消息 *******************************/
    
    private void sendMessageAction(String message) {
        getBaseActivity().sendSocketRequest(new SendMessage(MessageDAO.sendMessage(message)));
    }
}