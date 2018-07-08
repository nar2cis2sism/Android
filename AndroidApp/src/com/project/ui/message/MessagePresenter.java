package com.project.ui.message;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;
import com.project.app.bean.MessageItem;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.util.GsonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessagePresenter extends Presenter<MessageFragment> {
    
    MessageAdapter adapter;
    MessageLoader loader;
    
    MessageParam param;
    
    @Override
    protected void onCreate(Context context) {
        param = MessageParam.parse(getCallbacks().getArguments());
        if (param == null)
        {
            getCallbacks().finish();
            return;
        }
        
        adapter = new MessageAdapter(context);
        loader = new MessageLoader(context, this);
        getCallbacks().setDataSource(adapter, loader);
    }
}

class MessageAdapter extends JavaBeanAdapter<MessageItem> {
    
    private static final int VIEW_TYPE_RECEIVE  = 0;
    private static final int VIEW_TYPE_SEND     = 1;
    private static final int VIEW_TYPE_COUNT    = 2;

    public MessageAdapter(Context context) {
        super(context, 0);
    }
    
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
    
    @Override
    public int getItemViewType(int position) {
        return getItem(position).isReceived() ? VIEW_TYPE_RECEIVE : VIEW_TYPE_SEND;
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
        // 时间
        if (position > 0 && item.inFiveMinutes(getItem(position - 1)))
        {
            // 五分钟之内的消息不用显示时间
            holder.setVisible(R.id.time, false);
        }
        else
        {
            holder.setTextView(R.id.time, item.time);
            holder.setVisible(R.id.time, true);
        }
        // 头像
        holder.setImageView(R.id.avatar, R.drawable.avatar_default);
        // 消息内容
        holder.setTextView(R.id.content, item.getMessage());
        // 发送中进度条
        if (!item.isReceived())
        {
            holder.setVisible(R.id.progress, !item.isSendOut());
        }
    }
}

class MessageLoader extends JavaBeanLoader<MessageItem> {
    
    private final String account;

    public MessageLoader(Context context, MessagePresenter presenter) {
        super(context, MyDAOManager.getDAO());
        listen(Message.class);
        account = presenter.param.friend.getAccount();
    }

    @Override
    public Collection<MessageItem> loadInBackground() {
        List<Message> messages = MessageDAO.getMessageList(account);
        if (messages != null)
        {
            List<MessageItem> list = new ArrayList<MessageItem>(messages.size());
            for (Message message : messages)
            {
                list.add(new MessageItem(message));
            }
            
            return list;
        }
        
        return null;
    }
}

class MessageParam {
    
    private static final String EXTRA_FRIEND = "friend";
    
    public FriendListItem friend;
    
    public static Bundle build(FriendListItem item) {
        Bundle bunlde = new Bundle();
        bunlde.putString(EXTRA_FRIEND, GsonUtil.toJson(item));
        return bunlde;
    }
    
    public static MessageParam parse(Bundle bundle) {
        if (bundle == null)
        {
            return null;
        }
        
        String json = bundle.getString(EXTRA_FRIEND);
        if (json == null)
        {
            return null;
        }
        
        MessageParam param = new MessageParam();
        param.friend = GsonUtil.parseJson(json, FriendListItem.class);
        return param;
    }
}