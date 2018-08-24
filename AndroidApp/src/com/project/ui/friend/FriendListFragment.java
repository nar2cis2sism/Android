package com.project.ui.friend;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;
import com.project.ui.message.MessageFragment;
import com.project.ui.message.MessagePresenter.MessageParams;

import engine.android.core.Injector;
import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseListFragment;
import engine.android.util.AndroidUtil;
import engine.android.util.ui.ViewSize;
import engine.android.util.ui.ViewSize.ViewSizeObserver;
import engine.android.widget.common.LetterBar;
import engine.android.widget.common.layout.ActionContainer;
import engine.android.widget.component.ChooseButton;
import engine.android.widget.component.SearchBox;
import engine.android.widget.component.TitleBar;
import engine.android.widget.extra.MyExpandableListView;
import engine.android.widget.helper.LetterBarHelper;

/**
 * 好友列表界面
 * 
 * @author Daimon
 */
public class FriendListFragment extends BaseListFragment implements OnCheckedChangeListener, OnChildClickListener {
    
    private class ListHeader {
        
        View header;
        
        @InjectView(R.id.search_box)
        SearchBox search_box;
    
        @InjectView(R.id.action_container)
        ActionContainer action_container;
        
        public ListHeader(View header) {
            Injector.inject(this, this.header = header);
            setupView();
        }
        
        private void setupView() {
            // 搜索框
            search_box.setSearchProvider(searchPresenter);
            
            // 群
            action_container.addAction(R.drawable.friend_group, R.string.friend_group);
            // 讨论组
            action_container.addAction(R.drawable.friend_discuss, R.string.friend_discuss);
            // 公众好友
            action_container.addAction(R.drawable.friend_public, R.string.friend_public);
            // 好友推荐
            action_container.addAction(R.drawable.friend_recommend, R.string.friend_recommend);
        }
    }

    ListHeader list_header;
    
    @InjectView(R.id.expandable_list)
    MyExpandableListView expandable_list;
    
    @InjectView(R.id.letter_bar)
    LetterBar letter_bar;
    LetterBarHelper letterBarHelper;

    @InjectView(R.id.search_empty)
    ImageView search_empty;
    
    FriendListPresenter presenter;
    SearchPresenter searchPresenter;
    
    boolean showAllFriends;                 // True:显示所有好友,False:显示分组列表
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(FriendListPresenter.class);
        searchPresenter = addPresenter(SearchPresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.friend_title) // 搜索时显示
        .setDisplayShowTitleEnabled(false)
        .setDisplayShowCustomEnabled(true)
        .setCustomView(onCreateListSwitcher())
        .addAction(R.drawable.friend_add)
        .show();
    }
    
    private View onCreateListSwitcher() {
        ChooseButton button = new ChooseButton(getContext());
        button.setPositiveButton(R.string.friend_by_group, null);
        button.setNegativeButton(R.string.friend_all, null);
        button.setOnCheckedChangeListener(this);
        button.choosePositiveButton();
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        button.setLayoutParams(params);
        
        return button;
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        showAllFriends = checkedId == R.id.button_negative;
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        injectListContainer(root, R.layout.friend_list_fragment);
        list_header = onCreateListHeader(inflater);
        return root;
    }
    
    private ListHeader onCreateListHeader(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.friend_list_header, null);
        ViewSize.observeViewSize(view, new ViewSizeObserver() {
            
            @Override
            public void onSizeChanged(View view, ViewSize size) {
                letter_bar.getLayoutParams().height = getView().getHeight() - size.height;
                letter_bar.requestLayout();
            }
        });
        return new ListHeader(view);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupExpandableListView(expandable_list);
        setupLetterBar(letter_bar);
    }

    @Override
    protected void setupListView(ListView listView) {
        listView.setDivider(new InsetDrawable(getResources().getDrawable(R.color.divider_horizontal), 
                AndroidUtil.dp2px(getContext(), 35), 0, 0, 0));
        listView.setDividerHeight(1);
        
        setupListHeader(listView);
    }
    
    private void setupListHeader(ListView listView) {
        listView.addHeaderView(list_header.header);
        listView.setHeaderDividersEnabled(false);
    }
    
    private void setupExpandableListView(MyExpandableListView expandable_list) {
        expandable_list.setFlags(MyExpandableListView.FLAG_COLLAPSE_OTHER | MyExpandableListView.FLAG_NO_SCROLL);
        setupListHeader(expandable_list);
        expandable_list.setAdapter(presenter.groupAdapter);
        expandable_list.setOnChildClickListener(this);
    }
    
    private void setupLetterBar(LetterBar letter_bar) {
        letter_bar.setLetters(FriendListItem.CATEGORY);
        letter_bar.replaceLetter(0, getResources().getDrawable(R.drawable.letter_bar_search));
        letterBarHelper = new LetterBarHelper(letter_bar);
        letterBarHelper.bindListView(getListView());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        toMessageFragment((FriendListItem) getListAdapter().getItem(position));
    }
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        toMessageFragment(presenter.groupAdapter.getChild(groupPosition, childPosition));
        return true;
    }
    
    private void toMessageFragment(FriendListItem item) {
        MessageParams params = new MessageParams();
        params.title = item.friend.displayName;
        params.account = item.friend.account;
        
        startFragment(MessageFragment.class, MessageFragment.buildParams(params));
    }

    @Override
    protected void notifyDataSetChanged() {
        presenter.groupAdapter.update(presenter.loader.groups);
        searchPresenter.adapter.update(presenter.adapter.getItems());
        presenter.updateLetterIndex(letterBarHelper, getListView());
        if (!searchPresenter.isSearching && showAllFriends)
        {
            setLetterBarVisible(true);
        }
    }
    
    void updateView() {
        if (searchPresenter.isSearching)
        {
            // 搜索模式
            updateTitleBar(true);
            setActionVisible(false);
            setViewVisible(true, false);
            setListAdapter(searchPresenter.adapter);
        }
        else
        {
            updateTitleBar(false);
            setActionVisible(true);
            if (showAllFriends)
            {
                setViewVisible(true, true);
                setListAdapter(presenter.adapter);
            }
            else
            {
                // 分组模式
                setViewVisible(false, false);
            }
        }
    }
    
    private void updateTitleBar(boolean showTitle) {
        getTitleBar().setDisplayShowTitleEnabled(showTitle).setDisplayShowCustomEnabled(!showTitle);
    }
    
    private void setActionVisible(boolean shown) {
        list_header.action_container.setVisibility(shown ? View.VISIBLE : View.GONE);
    }
    
    private void setViewVisible(boolean showListView, boolean showLetterBar) {
        getListView().setVisibility(showListView ? View.VISIBLE : View.GONE);
        expandable_list.setVisibility(showListView ? View.GONE : View.VISIBLE);
        setLetterBarVisible(showLetterBar);
    }
    
    private void setLetterBarVisible(boolean showLetterBar) {
        letter_bar.setVisibility(showLetterBar && !presenter.adapter.isEmpty() ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (getListAdapter() != adapter)
        {
            super.setListAdapter(adapter);
            // ListView will get focus when update the adapter so request focus manually.
            list_header.search_box.requestFocus();
        }
    }
}