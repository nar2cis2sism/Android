package com.project.ui.message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.bean.MessageItem;
import com.project.network.action.socket.SendMessage;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.BaseFragment.ParamsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessagePresenter extends Presenter<MessageFragment> {
    
    MessageAdapter adapter;
    MessageLoader loader;
    
    MessageParams params;
    
    @Override
    protected void onCreate(Context context) {
        params = ParamsBuilder.parse(getCallbacks().getArguments(), MessageParams.class);
        if (params == null)
        {
            getCallbacks().finish();
            return;
        }
        
        adapter = new MessageAdapter(context);
        loader = new MessageLoader(context, this);
        getCallbacks().setDataSource(adapter, loader);
    }
    
    public static class MessageParams {
        
        public String title;
        public String account;
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
    protected void bindView(int position, ViewHolder holder, final MessageItem item) {
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
        holder.setTextView(R.id.content, item.getContent());
        // 发送状态
        if (!item.isReceived())
        {
            holder.setVisible(R.id.progress, item.isSending());
            if (item.isSendFail())
            {
                ImageView send_fail = holder.getView(R.id.send_fail);
                send_fail.setVisibility(View.VISIBLE);
                send_fail.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        MessageDAO.resendMessage(item.message);
                        MyApp.global().getSocketManager().sendSocketRequest(new SendMessage(item.message));
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
    
    private final String account;

    public MessageLoader(Context context, MessagePresenter presenter) {
        super(context, MyDAOManager.getDAO());
        listen(Message.class);
        account = presenter.params.account;
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