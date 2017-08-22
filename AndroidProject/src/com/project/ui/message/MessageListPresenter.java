package com.project.ui.message;

import android.content.Context;

import com.project.R;
import com.project.app.bean.FriendListItem;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;
import com.project.storage.provider.ProviderContract.FriendColumns;

import java.util.ArrayList;
import java.util.Collection;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;

public class MessageListPresenter extends Presenter<MessageListFragment> {
    
    MessageListLoader loader;
    MessageListAdapter adapter;
    
    @Override
    protected void onCreate(Context context) {
        loader = new MessageListLoader(context);
        adapter = new MessageListAdapter(context);
        getCallbacks().setDataSource(adapter, loader);
    }
}

class MessageListLoader extends JavaBeanLoader<FriendListItem> implements FriendColumns {

    public MessageListLoader(Context context) {
        super(context, MyDAOManager.getDAO());
        listen(Friend.class);
    }

    @Override
    public Collection<FriendListItem> loadInBackground() {
        Collection<Friend> friends = dao.find(Friend.class).orderBy(SORT_ORDER).getAll();
        if (friends != null)
        {
            ArrayList<FriendListItem> list = new ArrayList<FriendListItem>(friends.size());
            for (Friend friend : friends)
            {
                list.add(new FriendListItem(friend));
            }
            
            return list;
        }
        
        return null;
    }
}

class MessageListAdapter extends JavaBeanAdapter<FriendListItem> {

    public MessageListAdapter(Context context) {
        super(context, R.layout.message_list_item);
    }

    @Override
    protected void bindView(int position, ViewHolder holder, FriendListItem item) {
//        // 日期分类
//        String category = item.category;
//        
//        if (TextUtils.isEmpty(category) 
//        || (position > 0 && category.equals(getItem(position - 1).category)))
//        {
//            holder.setVisibility(R.id.category_container, View.GONE);
//        }
//        else
//        {
//            ((TextView) holder.getView(R.id.category)).setText(category);
//            holder.setVisibility(R.id.category_container, View.VISIBLE);
//        }
//        
//        // 日期显示
//        ((TextView) holder.getView(R.id.date)).setText(object.date);
//
//        // 标题
//        ((TextView) holder.getView(R.id.title)).setText(object.title);
//
//        // 内容
//        ((TextView) holder.getView(R.id.content)).setText(object.content);
//    
//        
//        
//        
//        // 分类
//        String category = item.category;
//        String previous_category = null;
//        if (position > 0)
//        {
//            previous_category = getItem(position - 1).category;
//        }
//
//        if (TextUtils.equals(category, previous_category))
//        {
//            holder.setVisible(R.id.category, false);
//        }
//        else
//        {
//            holder.setVisible(R.id.category, true);
//            holder.setTextView(R.id.category_text, category);
//        }
//
//        // 好友头像
//        AvatarImageView.display(holder, R.id.icon, item.avatarUrl);
//        // 名称
//        holder.setTextView(R.id.title, item.friend.displayName);
//        // 签名
//        holder.setTextView(R.id.content, item.friend.signature);
//        //
//        holder.setVisible(R.id.note, false);
    }
}