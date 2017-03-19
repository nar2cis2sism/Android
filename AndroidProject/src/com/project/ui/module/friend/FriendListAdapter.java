package com.project.ui.module.friend;

import android.content.Context;

import com.project.R;
import com.project.app.bean.Friend;

import engine.android.core.extra.JavaBeanAdapter;

public class FriendListAdapter extends JavaBeanAdapter<Friend> {

    public FriendListAdapter(Context context) {
        super(context, R.layout.friend_list_item);
        add(new Friend());
        add(new Friend());
        add(new Friend());
        add(new Friend());
    }

    @Override
    protected void bindView(int position, ViewHolder holder, Friend item) {
        // 头像
        holder.setImageView(R.id.icon, R.drawable.ic_launcher);
        // 分类
//        holder.setTextView(R.id.category, "分类");
        holder.setVisible(R.id.category, false);
        // 名称
        holder.setTextView(R.id.title, "好友名称");
        // 签名
        holder.setTextView(R.id.content, "好友签名");
    }
}

//public class FriendListOfAllAdapter extends JavaBeanCursorAdapter {
//
//    public FriendListOfAllAdapter(Context context) {
//        super(context, R.layout.friend_list_of_all_item);
//    }
//
//    @Override
//    protected void bindView(ViewHolder holder, Cursor cursor) {
//        Friend item = new Friend(cursor);
//        
//        String category = item.category;
//        String previous_category = null;
//        if (cursor.moveToPrevious())
//        {
//            Friend previous_item = new Friend(cursor);
//            previous_category = previous_item.category;
//        }
//        
//        if (TextUtils.isEmpty(category) 
//        || category.equals(previous_category))
//        {
//            holder.setVisibility(R.id.list_category, View.GONE);
//        }
//        else
//        {
//            ((TextView) holder.getView(R.id.category)).setText(category);
//            holder.setVisibility(R.id.list_category, View.VISIBLE);
//        }
//
//        // 好友头像
//        AvatarImageView.display(holder, R.id.icon, item.avatarUrl, R.drawable.default_avatar);
//        
//        // 标题
//        ((TextView) holder.getView(R.id.title)).setText(item.title);
//
//        // 内容
//        ((TextView) holder.getView(R.id.content)).setText(item.content);
//
//        // 注释
//        holder.setVisibility(R.id.note, View.GONE);
//    }
//}