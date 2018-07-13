package com.project.ui.more;

import android.content.Context;

import com.project.app.MySession;
import com.project.storage.db.User;

import engine.android.core.BaseFragment.Presenter;

public class MorePresenter extends Presenter<MoreFragment> {
    
    User user;
    
    @Override
    protected void onCreate(Context context) {
        super.onCreate(context);
        user = MySession.getUser();
    }
    
    public String getAuthenticatedText() {
        return user.isAuthenticated ? "已认证" : "未认证";
    }
}
    
//    SearchAdapter adapter;
//    boolean isSearching;
//    
//    @Override
//    protected void onCreate(Context context) {
//        adapter = new SearchAdapter(context);
//    }
//
//    @Override
//    public void search(String key) {
//        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(key = key.trim()))
//        {
//            if (isSearching)
//            {
//                getCallbacks().switchSearchMode(isSearching = false);
//                getCallbacks().search_empty.setVisibility(View.GONE);
//            }
//        }
//        else
//        {
//            isSearching = true;
//            adapter.getFilter().filter(key.toLowerCase(), this);
//        }
//    }
//
//    @Override
//    public void onFilterComplete(int count) {
//        if (isSearching)
//        {
//            getCallbacks().switchSearchMode(isSearching);
//            getCallbacks().search_empty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
//        }
//    }
//}
//
//class SearchAdapter extends JavaBeanAdapter<FriendListItem> implements FilterMatcher<FriendListItem> {
//
//    public SearchAdapter(Context context) {
//        super(context, R.layout.base_list_item_1);
//        setFilterMatcher(this);
//    }
//
//    @Override
//    protected void bindView(int position, ViewHolder holder, FriendListItem item) {
//        // 好友头像
//        holder.setImageView(R.id.icon, R.drawable.avatar_default);
//        // 名称
//        holder.setTextView(R.id.subject, item.getName());
//        //
//        holder.setVisible(R.id.note, false);
//    }
//
//    @Override
//    public boolean match(FriendListItem item, CharSequence constraint) {
//        return match(item.getPinyin(), constraint) || match(item.getName(), constraint);
//    }
//    
//    private boolean match(String text, CharSequence constraint) {
//        return !TextUtils.isEmpty(text) && text.contains(constraint);
//    }
//}