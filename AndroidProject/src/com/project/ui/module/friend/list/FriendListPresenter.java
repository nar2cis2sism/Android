package com.project.ui.module.friend.list;

import android.content.Context;
import android.text.TextUtils;

import com.project.R;
import com.project.app.bean.FriendListItem;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;
import com.project.storage.provider.ProviderContract.FriendColumns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.BaseFragment.Presenter;
import engine.android.framework.ui.widget.AvatarImageView;

import static com.project.app.bean.FriendListItem.CATEGORY;

public class FriendListPresenter extends Presenter {
    
    FriendListLoader loader;
    FriendListAdapter adapter;
    
    private final HashMap<String, Integer> letterMap
    = new HashMap<String, Integer>(CATEGORY.length);
    
    @Override
    public void onCreate(Context context) {
        loader = new FriendListLoader(context);
        adapter = new FriendListAdapter(context);
        getCallbacks().setDataSource(adapter, loader);
    }
    
    @Override
    public FriendListFragment getCallbacks() {
        return (FriendListFragment) super.getCallbacks();
    }
    
    public void updateLetterMap() {
        letterMap.clear();
        letterMap.put(CATEGORY[0], -1);
        
        List<FriendListItem> list = adapter.getItems();
        for (int i = 0, size = list.size(); i < size; i++)
        {
            String category = list.get(i).category;
            if (!letterMap.containsKey(category))
            {
                letterMap.put(category, i);
            }
        }
    }
    
    public Integer getPositionByLetter(String letter) {
        return letterMap.get(letter);
    }
}

class FriendListLoader extends JavaBeanLoader<FriendListItem> implements FriendColumns {

    public FriendListLoader(Context context) {
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