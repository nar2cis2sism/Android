package com.project.ui.friend;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanAdapter.FilterMatcher;
import engine.android.framework.ui.BaseFragment.ParamsBuilder;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.util.image.AsyncImageLoader.ImageUrl;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.input.SearchBox.SearchProvider;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter.FilterListener;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;
import com.project.app.bean.ServerUrl;
import com.project.app.config.ImageTransformer;
import com.project.storage.dao.FriendDAO;
import com.project.storage.db.Friend;
import com.project.ui.friend.info.FriendInfoFragment;
import com.project.ui.friend.info.FriendInfoFragment.FriendInfoParams;

import protocol.http.SearchContactData.ContactData;

class SearchPresenter extends Presenter<FriendListFragment> implements SearchProvider, FilterListener {
    
    SearchAdapter adapter;
    GlobalSearchAdapter globalAdapter;
    boolean isSearching;
    
    @Override
    protected void onCreate(Context context) {
        adapter = new SearchAdapter(context);
        globalAdapter = new GlobalSearchAdapter(context, this);
    }

    @Override
    public void search(String key, boolean imeAction) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(key = key.trim()))
        {
            if (isSearching)
            {
                getCallbacks().list_header.hideSoftInput();
                getCallbacks().switchSearchMode(isSearching = false);
                getCallbacks().showSearchEmpty(false);
            }
        }
        else
        {
            isSearching = true;
            if (imeAction)
            {
                // 全局搜索
                getCallbacks().searchContact(key);
            }
            else
            {
                // 本地搜索
                adapter.getFilter().filter(key.toLowerCase(), this);
            }
        }
    }

    @Override
    public void onFilterComplete(int count) {
        if (isSearching)
        {
            getCallbacks().switchSearchMode(isSearching);
            getCallbacks().showSearchEmpty(count == 0);
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
        AvatarImageView.display(holder, R.id.icon, item.avatarUrl);
        // 名称
        holder.setTextView(R.id.subject, item.friend.displayName);
        //
        holder.setVisible(R.id.note, false);
    }

    @Override
    public boolean match(FriendListItem item, CharSequence constraint) {
        return match(item.friend.pinyin, constraint) || match(item.friend.displayName, constraint);
    }
    
    private static boolean match(String text, CharSequence constraint) {
        return !TextUtils.isEmpty(text) && text.contains(constraint);
    }
}

class GlobalSearchAdapter extends JavaBeanAdapter<ContactData> {
    
    SearchPresenter presenter;

    public GlobalSearchAdapter(Context context, SearchPresenter presenter) {
        super(context, R.layout.base_list_item_1);
        this.presenter = presenter;
    }
    
    @Override
    protected View newView(int position, LayoutInflater inflater, ViewGroup parent) {
        View root = super.newView(position, inflater, parent);
        // 加为好友
        Button btn = new Button(getContext());
        btn.setText(R.string.friend_add);

        View note = root.findViewById(R.id.note);
        UIUtil.replace(note, btn, note.getLayoutParams());
        return root;
    }

    @Override
    protected void bindView(int position, ViewHolder holder, final ContactData item) {
        // 好友头像
        AvatarImageView.display(holder, R.id.icon, getAvatarUrl(item));
        // 名称
        holder.setTextView(R.id.subject, getDisplayName(item));
        // 加为好友
        final Friend friend = FriendDAO.getFriendByAccount(item.account);
        if (friend != null)
        {
            holder.setVisible(R.id.note, false);
            holder.getConvertView().setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    presenter.getCallbacks().startFragment(FriendInfoFragment.class, 
                            ParamsBuilder.build(new FriendInfoParams(friend)));
                }
            });
        }
        else
        {
            holder.setVisible(R.id.note, true);
            holder.getView(R.id.note).setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    presenter.getCallbacks().addFriend(item.account);
                }
            });
            holder.getConvertView().setOnClickListener(null);
        }
    }
    
    private ImageUrl getAvatarUrl(ContactData item) {
        String avatarUrl = item.avatar_url;
        if (TextUtils.isEmpty(avatarUrl))
        {
            return null;
        }
        
        return new ImageUrl(ImageTransformer.TYPE_AVATAR,
                ServerUrl.getDownloadUrl(avatarUrl), null);
    }
    
    /**
     * 获取显示名称
     */
    private String getDisplayName(ContactData item) {
        String displayName = item.nickname;
        if (!TextUtils.isEmpty(displayName) && displayName.trim().length() > 0)
        {
            return displayName;
        }
        
        return item.account;
    }
}