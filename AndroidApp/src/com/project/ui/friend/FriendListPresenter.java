package com.project.ui.friend;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;
import com.project.storage.provider.ProviderContract.FriendColumns;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.widget.helper.LetterBarHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FriendListPresenter extends Presenter<FriendListFragment> {
    
    FriendListAdapter adapter;
    FriendListLoader  loader;
    
    @Override
    protected void onCreate(Context context) {
        adapter = new FriendListAdapter(context);
        loader  = new FriendListLoader(context);
        getCallbacks().setDataSource(adapter, loader);
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
        holder.setImageView(R.id.icon, R.drawable.avatar_default);
        // 名称
        holder.setTextView(R.id.title, item.getName());
        // 签名
        holder.setTextView(R.id.content, item.getSignature());
        //
        holder.setVisible(R.id.note, false);
    }
}

class FriendListLoader extends JavaBeanLoader<FriendListItem> {

    public FriendListLoader(Context context) {
        super(context, MyDAOManager.getDAO());
        listen(Friend.class);
    }

    @Override
    public Collection<FriendListItem> loadInBackground() {
        List<Friend> friends = dao.find(Friend.class).orderBy(FriendColumns.SORTING).getAll();
        if (friends != null)
        {
            List<FriendListItem> list = new ArrayList<FriendListItem>(friends.size());
            for (Friend friend : friends)
            {
                list.add(new FriendListItem(friend));
            }
            
            return list;
        }
        
        return null;
    }
}