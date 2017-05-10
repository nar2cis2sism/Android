package com.project.ui.friend.list;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Filter.FilterListener;

import com.project.R;
import com.project.app.bean.FriendListItem;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanAdapter.FilterMatcher;
import engine.android.framework.ui.BaseFragment.Presenter;
import engine.android.framework.ui.widget.AvatarImageView;

public class SearchPresenter extends Presenter<FriendListFragment> implements FilterListener {
    
    SearchAdapter adapter;
    boolean inSearch;
    
    @Override
    protected void onCreate(Context context) {
        adapter = new SearchAdapter(context);
    }
    
    public void search(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint))
        {
            if (inSearch)
            {
                getCallbacks().inSearch(inSearch = false);
                getCallbacks().search_empty.setVisibility(View.GONE);
            }
        }
        else
        {
            adapter.getFilter().filter(constraint, this);
        }
    }

    @Override
    public void onFilterComplete(int count) {
        if (!inSearch)
        {
            getCallbacks().inSearch(inSearch = true);
        }
        
        getCallbacks().search_empty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }
}

class SearchAdapter extends JavaBeanAdapter<FriendListItem> implements FilterMatcher<FriendListItem> {

    public SearchAdapter(Context context) {
        super(context, R.layout.base_list_item_1);
        setFilterMatcher(this);
    }

    @Override
    protected void bindView(int position, ViewHolder holder, FriendListItem item) {
        // 好友头像
        AvatarImageView.display(holder, R.id.icon, item.avatarUrl);
        // 名称
        holder.setTextView(R.id.subject, item.friend.displayName);
        //
        holder.setVisible(R.id.note, false);
    }

    @Override
    public boolean match(FriendListItem item, CharSequence constraint) {
        String pinyin = item.friend.pinyin;
        if (!TextUtils.isEmpty(pinyin) && pinyin.contains(constraint))
            return true;
        String displayName = item.friend.displayName;
        if (!TextUtils.isEmpty(displayName) && displayName.contains(constraint))
            return true;
        
        return false;
    }
}