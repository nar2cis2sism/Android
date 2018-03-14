package com.project.ui.friend;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Filter.FilterListener;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanAdapter.FilterMatcher;
import engine.android.widget.SearchBox.SearchProvider;

public class SearchPresenter extends Presenter<FriendListFragment> implements SearchProvider, FilterListener {
    
    SearchAdapter adapter;
    boolean isSearching;
    
    @Override
    protected void onCreate(Context context) {
        adapter = new SearchAdapter(context);
    }

    @Override
    public void search(String key) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(key = key.trim()))
        {
            if (isSearching)
            {
                getCallbacks().switchSearchMode(isSearching = false);
                getCallbacks().search_empty.setVisibility(View.GONE);
            }
        }
        else
        {
            isSearching = true;
            adapter.getFilter().filter(key.toLowerCase(), this);
        }
    }

    @Override
    public void onFilterComplete(int count) {
        if (isSearching)
        {
            getCallbacks().switchSearchMode(isSearching);
            getCallbacks().search_empty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        }
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
        holder.setImageView(R.id.icon, R.drawable.avatar_default);
        // 名称
        holder.setTextView(R.id.subject, item.getName());
        //
        holder.setVisible(R.id.note, false);
    }

    @Override
    public boolean match(FriendListItem item, CharSequence constraint) {
        return match(item.getPinyin(), constraint) || match(item.getName(), constraint);
    }
    
    private boolean match(String text, CharSequence constraint) {
        return !TextUtils.isEmpty(text) && text.contains(constraint);
    }
}