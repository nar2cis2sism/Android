package com.project.ui.message.conversation;

import static com.project.logic.MessageLogic.currentConversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.app.bean.ConversationItem;
import com.project.logic.MessageLogic;
import com.project.network.action.socket.SendMessage;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.FriendDAO;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Friend;
import com.project.storage.db.Message;
import com.project.ui.message.conversation.ConversationActivity.ConversationParams;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.BaseFragment.ParamsBuilder;
import engine.android.framework.ui.widget.AvatarImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ConversationPresenter extends Presenter<ConversationFragment> {
    
    ConversationAdapter adapter;
    ConversationLoader  loader;
    
    ConversationParams  params;
    
    @Override
    protected void onCreate(Context context) {
        params = ParamsBuilder.parse(getCallbacks().getArguments(), ConversationParams.class);
        if (params == null)
        {
            getCallbacks().finish();
            return;
        }
        
        currentConversation = params.account;
        
        adapter = new ConversationAdapter(context, this);
        loader  = new ConversationLoader(context, this);
        getCallbacks().setDataSource(adapter, loader);
    }
    
    @Override
    protected void onDestroy() {
        currentConversation = null;
    }
}

class ConversationAdapter extends JavaBeanAdapter<ConversationItem> {
    
    private static final int VIEW_TYPE_RECEIVE  = 0;
    private static final int VIEW_TYPE_SEND     = 1;
    private static final int VIEW_TYPE_COUNT    = 2;
    
    private final ConversationParams params;

    public ConversationAdapter(Context context, ConversationPresenter presenter) {
        super(context, 0);
        params = presenter.params;
    }
    
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
    
    @Override
    public int getItemViewType(int position) {
        return getItem(position).message.isReceived ? VIEW_TYPE_RECEIVE : VIEW_TYPE_SEND;
    }
    
    @Override
    protected View newView(int position, LayoutInflater inflater, ViewGroup parent) {
        int resource = 0;
        switch (getItemViewType(position)) {
            case VIEW_TYPE_RECEIVE:
                resource = R.layout.message_item_receive;
                break;
            case VIEW_TYPE_SEND:
                resource = R.layout.message_item_send;
                break;
        }
        
        return inflater.inflate(resource, parent, false);
    }

    @Override
    protected void bindView(int position, ViewHolder holder, ConversationItem item) {
        final Message message = item.message;
        // 时间
        if (position > 0 && item.inFiveMinutes(getItem(position - 1)))
        {
            // 五分钟之内的消息不用显示时间
            holder.setVisible(R.id.time, false);
        }
        else
        {
            holder.setVisible(R.id.time, true);
            holder.setTextView(R.id.time, item.time);
        }
        // 头像
        if (message.isReceived)
        {
            AvatarImageView.display(holder, R.id.avatar, params.friend.getAvatarUrl());
        }
        else
        {
            AvatarImageView.display(holder, R.id.avatar, MySession.getUser().getAvatarUrl());
        }
        // 消息内容
        holder.setTextView(R.id.content, message.content);
        // 发送状态
        if (!message.isReceived)
        {
            holder.setVisible(R.id.progress, item.isSending());
            if (item.isSendFail())
            {
                holder.setVisible(R.id.send_fail, true);
                holder.getView(R.id.send_fail).setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        MessageDAO.resendMessage(message);
                        MyApp.global().getSocketManager().sendSocketRequest(new SendMessage(message));
                    }
                });
            }
            else
            {
                holder.setVisible(R.id.send_fail, false);
            }
        }
    }
}

class ConversationLoader extends JavaBeanLoader<ConversationItem> {
    
    private final ConversationParams params;

    public ConversationLoader(Context context, ConversationPresenter presenter) {
        super(context, MyDAOManager.getDAO());
        listen(Message.class);
        params = presenter.params;
    }

    @Override
    public Collection<ConversationItem> loadInBackground() {
        String account = params.account;
        Friend friend = params.friend;
        
        if (friend == null)
        {
            params.friend = friend = FriendDAO.getFriendByAccount(account);
            if (friend == null) throw new RuntimeException("Can not find friend:" + account);
            
            MessageLogic.setMessageRead(account);
        }
        
        List<Message> messages = MessageDAO.getMessageList(account);
        if (messages != null)
        {
            List<ConversationItem> list = new ArrayList<ConversationItem>(messages.size());
            for (Message message : messages)
            {
                list.add(new ConversationItem(message));
            }
            
            return list;
        }
        
        return null;
    }
}