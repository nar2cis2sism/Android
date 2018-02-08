package com.project.ui.friend;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanLoader;
import engine.android.util.StringUtil.AlphaComparator;
import engine.android.widget.helper.LetterBarHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        holder.setTextView(R.id.title, item.name);
        // 签名
        holder.setTextView(R.id.content, item.signature);
        //
        holder.setVisible(R.id.note, false);
    }
}

class FriendListLoader extends JavaBeanLoader<FriendListItem> {

    public FriendListLoader(Context context) {
        super(context);
    }

    @Override
    public Collection<FriendListItem> loadInBackground() {
        List<FriendListItem> list = new ArrayList<FriendListItem>();
        
        // 1
        list.add(new FriendListItem("闫昊", null));
        list.add(new FriendListItem("王晓庆", "一切都会好起来"));
        list.add(new FriendListItem("Jane", "加油哦"));
        list.add(new FriendListItem("范永利", null));
        list.add(new FriendListItem("李冰涛", "fire in the hole"));
        list.add(new FriendListItem("*658了*", "分享图片"));
        list.add(new FriendListItem("Num2", "stranger"));
        list.add(new FriendListItem("于美珍", ""));
        list.add(new FriendListItem("陶生", ""));
        list.add(new FriendListItem("乌托邦", ""));
        list.add(new FriendListItem("Jess 杨姐", null));
        // 11
        
        Collections.sort(list, new FriendComparator());
        return list;
    }
    
    private static class FriendComparator extends AlphaComparator<FriendListItem> {

        @Override
        public String toString(FriendListItem item) {
            return item.sortOrder;
        }
    }
}