package com.project.ui.friend;

import static com.project.network.action.Actions.SEARCH_CONTACT;
import static com.project.network.action.Actions.ADD_FRIEND;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.project.network.action.http.AddFriend;
import com.project.network.action.http.SearchContact;
import com.project.ui.friend.info.FriendInfoFragment;
import com.project.ui.friend.info.FriendInfoFragment.FriendInfoParams;

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
import protocol.http.SearchContactData.ContactData;

import java.util.List;

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
    
    ChooseButton list_switcher;

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
        registerEventHandler(new EventHandler());
        presenter = addPresenter(FriendListPresenter.class);
        searchPresenter = addPresenter(SearchPresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.friend_title) // 搜索时显示
        .setCustomView(getListSwitcher())
        .addAction(R.drawable.friend_add, new OnClickListener() {
            
            int i;
            
            @Override
            public void onClick(View v) {
                if (i == 0)
                {
                    list_header.search_box.getSearchEditText().setText("13872530618");
                }
                else if (i == 1)
                {
                    searchPresenter.search(list_header.search_box.getSearchEditText().getText().toString(), true);
                }
                else
                {
                    i = 0;
                    return;
                }
                
                i++;
            }
        })
        .show();
        updateTitleBar(searchPresenter.isSearching);
    }
    
    private ChooseButton getListSwitcher() {
        if (list_switcher == null)
        {
            ChooseButton button = list_switcher = new ChooseButton(getContext());
            button.setPositiveButton(R.string.friend_by_group, null);
            button.setNegativeButton(R.string.friend_all, null);
            button.setOnCheckedChangeListener(this);
            button.choosePositiveButton();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            button.setLayoutParams(params);
        }

        return list_switcher;
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        setListViewVisible(showAllFriends = checkedId == R.id.button_negative);
        updateLetterBar();
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
    protected void notifyDataSetChanged() {
        presenter.groupAdapter.update(presenter.loader.groups);
        searchPresenter.adapter.update(presenter.adapter.getItems());
        presenter.updateLetterIndex(letterBarHelper, getListView());
        updateLetterBar();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        toFriendInfo((FriendListItem) getListAdapter().getItem(position));
    }
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        toFriendInfo(presenter.groupAdapter.getChild(groupPosition, childPosition));
        return true;
    }
    
    private void toFriendInfo(FriendListItem item) {
        startFragment(FriendInfoFragment.class, ParamsBuilder.build(new FriendInfoParams(item.friend)));
    }

    /**
     * @param inSearch True:进入搜索模式,False:退出搜索模式
     */
    void switchSearchMode(boolean inSearch) {
        ListAdapter adapter = inSearch ? searchPresenter.adapter : presenter.adapter;
        if (getListAdapter() == adapter)
        {
            // 此方法会调用多次，故加此判断避免冗余
            return;
        }
        
        updateTitleBar(inSearch);
        setActionVisible(!inSearch);
        if (!showAllFriends)
        {
            setListViewVisible(inSearch);
        }
        
        setListAdapter(adapter);
        updateLetterBar();
    }
    
    private void updateTitleBar(boolean showTitle) {
        getTitleBar()
        .setDisplayShowTitleEnabled(showTitle)
        .setDisplayShowCustomEnabled(!showTitle);
    }
    
    private void setActionVisible(boolean shown) {
        list_header.action_container.setVisibility(shown ? View.VISIBLE : View.GONE);
    }
    
    private void setListViewVisible(boolean shown) {
        getListView().setVisibility(shown ? View.VISIBLE : View.GONE);
        expandable_list.setVisibility(shown ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public void setListAdapter(ListAdapter adapter) {
        super.setListAdapter(adapter);
        // ListView will get focus when update the adapter so request focus manually.
        list_header.search_box.requestFocus();
    }

    private void updateLetterBar() {
        letter_bar.setVisibility(!searchPresenter.isSearching
                && showAllFriends && !presenter.adapter.isEmpty()
                ? View.VISIBLE : View.GONE);
    }
    
    /******************************* 搜索联系人 *******************************/
    
    void searchContact(String key) {
        showProgress(getString(R.string.progress_search_contact));
        getBaseActivity().sendHttpRequest(new SearchContact(key));
    }
    
    /******************************* 加为好友 *******************************/
    
    void addFriend(String account) {
        showProgress(getString(R.string.progress_waiting));
        getBaseActivity().sendHttpRequest(new AddFriend(account, false));
    }
    
    private class EventHandler extends engine.android.framework.ui.BaseFragment.EventHandler {
        
        public EventHandler() {
            super(SEARCH_CONTACT, ADD_FRIEND);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            if (SEARCH_CONTACT.equals(action))
            {
                hideProgress();
                @SuppressWarnings("unchecked")
                List<ContactData> list = (List<ContactData>) param;
                if (list == null || list.isEmpty())
                {
                    search_empty.setVisibility(View.VISIBLE);
                }
                else
                {
                    search_empty.setVisibility(View.GONE);
                    searchPresenter.globalAdapter.update(list);
                    setListAdapter(searchPresenter.globalAdapter);
                }
            }
            else if (ADD_FRIEND.equals(action))
            {
                hideProgress();
                list_header.search_box.getSearchEditText().setText(null);
            }
        }
    }
}