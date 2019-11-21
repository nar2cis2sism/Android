package com.project.ui.message.conversation;

import static com.project.logic.MessageLogic.currentConversation;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.BaseFragment.ParamsBuilder;
import engine.android.framework.ui.activity.SinglePaneActivity;
import engine.android.framework.ui.widget.AvatarImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.app.bean.MessageItem;
import com.project.app.service.AppService;
import com.project.logic.MessageLogic;
import com.project.network.action.socket.SendMessage;
import com.project.storage.dao.FriendDAO;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Friend;
import com.project.storage.db.Message;
import com.project.ui.friend.info.FriendInfoFragment;
import com.project.ui.friend.info.FriendInfoFragment.FriendInfoParams;
import com.project.ui.message.conversation.ConversationActivity.ConversationParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class MessagePresenter extends Presenter<MessageFragment> {
    
    MessageAdapter adapter;
    MessageLoader loader;
    
    ConversationParams params;
    
    @Override
    protected void onCreate(Context context) {
        params = ParamsBuilder.parse(getCallbacks().getArguments(), ConversationParams.class);
        if (params == null)
        {
            getCallbacks().finish();
            return;
        }
        
        currentConversation = params.account;
        
        getCallbacks().setDataSource(adapter = new MessageAdapter(context, params),
                loader = new MessageLoader(context, params));
        // 暂停背景音乐
        AppService.getService().play(false);
    }
    
    @Override
    protected void onDestroy() {
        currentConversation = null;
        // 恢复背景音乐
        AppService.getService().play(true);
    }
}

class MessageAdapter extends JavaBeanAdapter<MessageItem> {
    
    private static final int VIEW_TYPE_RECEIVE  = 0;
    private static final int VIEW_TYPE_SEND     = 1;
    private static final int VIEW_TYPE_COUNT    = 2;
    
    private final ConversationParams params;

    public MessageAdapter(Context context, ConversationParams params) {
        super(context, 0);
        this.params = params;
    }
    
    @Override
    public boolean isEnabled(int position) {
        return false;
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
    protected void bindView(int position, ViewHolder holder, MessageItem item) {
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
            holder.getView(R.id.avatar).setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    getContext().startActivity(SinglePaneActivity.buildIntent(getContext(), 
                            FriendInfoFragment.class, ParamsBuilder.build(new FriendInfoParams(params.friend))));
                }
            });
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

class MessageLoader extends JavaBeanLoader<MessageItem> {
    
    private final ConversationParams params;

    public MessageLoader(Context context, ConversationParams params) {
        super(context, MessageDAO.dao);
        listen(Message.class);
        this.params = params;
    }

    @Override
    public Collection<MessageItem> loadInBackground() {
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
            ArrayList<MessageItem> list = new ArrayList<MessageItem>(messages.size());
            for (Message message : messages)
            {
                list.add(new MessageItem(message));
            }
            
            return list;
        }
        
        return null;
    }
}