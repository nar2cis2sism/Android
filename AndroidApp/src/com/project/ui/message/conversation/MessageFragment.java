package com.project.ui.message.conversation;

import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseListFragment;
import engine.android.util.os.AudioUtil;
import engine.android.util.os.AudioUtil.AudioPlayer;
import engine.android.util.ui.UIUtil;
import engine.android.widget.common.layout.KeyboardLayout;
import engine.android.widget.component.TitleBar;
import engine.android.widget.component.input.ConversationBar;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.network.action.socket.SendMessage;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;

import java.io.File;

/**
 * 聊天界面
 * 
 * @author Daimon
 * @see ConversationActivity
 */
public class MessageFragment extends BaseListFragment {
    
    @InjectView(R.id.title_bar)
    TitleBar title_bar;
    
    @InjectView(R.id.conversation_bar)
    ConversationBar conversation_bar;
    
    MessagePresenter presenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(MessagePresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .show();
    }
    
    @Override
    public TitleBar getTitleBar() {
        return title_bar;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        KeyboardLayout root = new KeyboardLayout(getContext());
        inflater.inflate(R.layout.message_fragment, root);
        return root;
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
        listView.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        conversation_bar.clearFocus();
                        UIUtil.hideSoftInput(conversation_bar);
                        break;
                }
                
                return false;
            }
        });
    }
    
    private void setupConversationBar(ConversationBar conversation_bar) {
        conversation_bar.setCallback(new ConversationBar.Callback() {
            
            private final AudioPlayer player = AudioUtil.play();
            
            @Override
            public void onSendMessage(String message) {
                Message msg = MessageDAO.sendMessage(presenter.params.account, message);
                getBaseActivity().sendSocketRequest(new SendMessage(msg));
            }
            
            @Override
            public void onRecordVoice(final File recordFile) {
                if (recordFile == null) return;
                
                MediaPlayer mp = player.start(recordFile);
                mp.setOnCompletionListener(new OnCompletionListener() {
                    
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        player.start(null);
                        recordFile.delete();
                    }
                });
            }
        });
    }
    
    @Override
    public void setListAdapter(ListAdapter adapter) {
        super.setListAdapter(adapter);
        getTitleBar().setTitle(presenter.params.friend.displayName);
    }
}