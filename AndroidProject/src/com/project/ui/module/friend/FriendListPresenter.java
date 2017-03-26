package com.project.ui.module.friend;

import android.content.Context;
import android.text.TextUtils;

import com.project.R;
import com.project.app.MyApp;
import com.project.app.bean.Friend;
import com.project.app.storage.dao.FriendDAO;

import java.util.ArrayList;
import java.util.Collection;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanLoader;
import engine.android.dao.DAOTemplate.DAOListener;

/**
 * Created by issuser on 17/3/25.
 */

public class FriendListPresenter {

    public final FriendListAdapter adapter;
    public final FriendListLoader loader;

    public FriendListPresenter(Context context) {
        adapter = new FriendListAdapter(context);
        loader = new FriendListLoader(context);
    }
}

class FriendListLoader extends JavaBeanLoader<Friend> {

    public FriendListLoader(Context context) {
        super(context);
        setDataChangeObserver(new Observer());
    }

    @Override
    public Collection<Friend> loadInBackground() {
        com.project.app.storage.db.Friend[] friends = FriendDAO.getAllFriends();
        ArrayList<Friend> list = new ArrayList<Friend>(friends.length);
        for (com.project.app.storage.db.Friend friend : friends)
        {
            list.add(new Friend(friend));
        }

        return list;
    }

    private class Observer extends DataChangeObserver implements DAOListener {

        @Override
        public void registerObserver(Context context) {
            MyApp.getDAOTemplate().registerListener(com.project.app.storage.db.Friend.class, this);
        }

        @Override
        public void unregisterObserver(Context context) {
            MyApp.getDAOTemplate().unregisterListener(com.project.app.storage.db.Friend.class, this);
        }

        @Override
        public void onChange() {
            refresh();
        }
    }
}

class FriendListAdapter extends JavaBeanAdapter<Friend> {

    public FriendListAdapter(Context context) {
        super(context, R.layout.friend_list_item);
    }

    @Override
    protected void bindView(int position, ViewHolder holder, Friend item) {
        // 分类
        String category = item.category;
        String previous_category = null;
        if (position > 0)
        {
            Friend previous_item = getItem(position - 1);
            previous_category = previous_item.category;
        }

        if (TextUtils.equals(category, previous_category))
        {
            holder.setVisible(R.id.category, false);
        }
        else
        {
            holder.setVisible(R.id.category, true);
            holder.setTextView(R.id.category, category);
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