package com.project.ui.module.friend.list;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.project.R;
import com.project.app.bean.FriendListItem;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.BaseFragment.Presenter;

import java.util.ArrayList;
import java.util.Collection;

public class FriendListPresenter extends Presenter {
    
    FriendListLoader loader;
    FriendListAdapter adapter;
    
    @Override
    public void onCreate(Context context) {
        loader = new FriendListLoader(context);
        adapter = new FriendListAdapter(context);
    }
}

class FriendListLoader extends JavaBeanLoader<FriendListItem> {

    public FriendListLoader(Context context) {
        super(context, MyDAOManager.getDAO());
        listen(Friend.class);
    }

    @Override
    public Collection<FriendListItem> loadInBackground() {
        Collection<Friend> friends = dao.find(Friend.class).getAll();
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

class FriendListAdapter extends JavaBeanAdapter<FriendListItem> {

    public FriendListAdapter(Context context) {
        super(context, R.layout.friend_list_item);
    }

    @Override
    protected void bindView(int position, ViewHolder holder, FriendListItem item) {
        // 分类
        String category = item.category;
        String previous_category = null;
        if (position > 0)
        {
            previous_category = getItem(position - 1).category;
        }

        TextView categoryTextView = holder.getView(R.id.category);
        if (TextUtils.equals(category, previous_category))
        {
            categoryTextView.setVisibility(View.GONE);
        }
        else
        {
            categoryTextView.setVisibility(View.VISIBLE);
            categoryTextView.setText(category);
        }

        // 好友头像
        //        AvatarImageView.display(holder, R.id.icon, item.avatarUrl, R.drawable.default_avatar);
        holder.setImageView(R.id.icon, R.drawable.ic_launcher);
        // 名称
        holder.setTextView(R.id.title, item.friend.displayName);
        // 签名
        holder.setTextView(R.id.content, item.friend.signature);
        //
        holder.setVisible(R.id.note, false);
    }
}