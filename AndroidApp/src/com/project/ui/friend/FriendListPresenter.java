package com.project.ui.friend;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.widget.extra.MyExpandableListView.BaseExpandableListAdapter;
import engine.android.widget.helper.LetterBarHelper;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.app.bean.FriendGroupItem;
import com.project.app.bean.FriendListItem;
import com.project.storage.dao.FriendDAO;
import com.project.storage.db.Friend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class FriendListPresenter extends Presenter<FriendListFragment> {
    
    FriendGroupAdapter groupAdapter;
    FriendListAdapter adapter;
    FriendListLoader loader;
    
    @Override
    protected void onCreate(Context context) {
        groupAdapter = new FriendGroupAdapter(context);
        getCallbacks().setDataSource(adapter = new FriendListAdapter(context),
                loader = new FriendListLoader(context));
    }
    
    public void updateLetterIndex(LetterBarHelper helper, ListView listView) {
        helper.resetIndex();
        if (!adapter.isEmpty())
        {
            // 搜索
            helper.setIndex(FriendListItem.CATEGORY[0], 0);
            
            List<FriendListItem> list = adapter.getItems();
            for (int i = 0, size = list.size(), headerCount = listView.getHeaderViewsCount(); i < size; i++)
            {
                String category = list.get(i).category;
                if (helper.getIndex(category) == null)
                {
                    helper.setIndex(category, i + headerCount);
                }
            }
        }
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

        if (TextUtils.equals(category, previous_category))
        {
            holder.setVisible(R.id.category, false);
        }
        else
        {
            holder.setVisible(R.id.category, true);
            holder.setTextView(R.id.category_text, category);
        }
        // 好友头像
        AvatarImageView.display(holder, R.id.icon, item.avatarUrl);
        // 名称
        holder.setTextView(R.id.title, item.friend.displayName);
        // 签名
        holder.setTextView(R.id.content, item.friend.signature);
        //
        holder.setVisible(R.id.note, false);
    }
}

class FriendGroupAdapter extends BaseExpandableListAdapter<FriendGroupItem, FriendListItem> {

    public FriendGroupAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newGroupView(int groupPosition, ViewGroup parent) {
        return getLayoutInflater().inflate(R.layout.friend_group_item, parent, false);
    }

    @Override
    protected void bindGroupView(int groupPosition, boolean isExpanded, ViewHolder holder,
            FriendGroupItem item) {
        holder.setImageView(R.id.icon, isExpanded
                ? R.drawable.group_indicator_expanded
                : R.drawable.group_indicator_collapsed);
        holder.setTextView(R.id.number, String.valueOf(item.getChildrenCount()));
        holder.setTextView(R.id.name, item.name);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null)
        {
            (convertView = getLayoutInflater().inflate(R.layout.base_list_item_2, parent, false))
            .setTag(holder = new ViewHolder(convertView));
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
    
        if (holder != null)
        {
            FriendListItem item = getChild(groupPosition, childPosition);
            // 好友头像
            AvatarImageView.display(holder, R.id.icon, item.avatarUrl);
            // 名称
            holder.setTextView(R.id.title, item.friend.displayName);
            // 签名
            holder.setTextView(R.id.content, item.friend.signature);
            //
            holder.setVisible(R.id.note, false);
        }
    
        return convertView;
    }
}

class FriendListLoader extends JavaBeanLoader<FriendListItem> {
    
    List<FriendGroupItem> groups;

    public FriendListLoader(Context context) {
        super(context, FriendDAO.dao);
        listen(Friend.class);
    }

    @Override
    public Collection<FriendListItem> loadInBackground() {
        List<FriendListItem> list = null;
        List<Friend> friends = FriendDAO.getFriendList();
        if (friends != null)
        {
            list = new ArrayList<FriendListItem>(friends.size());
            for (Friend friend : friends)
            {
                list.add(new FriendListItem(friend));
            }
        }

        filterByGroup(list);
        return list;
    }
    
    private void filterByGroup(List<FriendListItem> list) {
        groups = new ArrayList<FriendGroupItem>(2);
        FriendGroupItem group1 = new FriendGroupItem("未分组好友");
        FriendGroupItem group2 = new FriendGroupItem("我的好友");
        groups.add(group1);
        groups.add(group2);
        
        if (list != null)
        {
            int index = 4;
            for (FriendListItem item : list)
            {
                if (index == 0)
                {
                    group2.addChild(item);
                }
                else
                {
                    group1.addChild(item);
                    index--;
                }
            }
        }
    }
}