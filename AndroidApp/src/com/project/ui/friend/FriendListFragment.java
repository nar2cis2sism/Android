package com.project.ui.friend;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.daimon.yueba.R;
import com.project.app.bean.FriendListItem;

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
import engine.android.widget.helper.LetterBarHelper;

/**
 * 好友列表界面
 * 
 * @author Daimon
 */
public class FriendListFragment extends BaseListFragment {
    
    ListHeader list_header = new ListHeader();
    
    @InjectView(R.id.letter_bar)
    LetterBar letter_bar;
    LetterBarHelper letterBarHelper;

    @InjectView(R.id.search_empty)
    ImageView search_empty;
    
    FriendListPresenter presenter;
    SearchPresenter searchPresenter;
    
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
        .setCustomView(onCreateTitleMiddleView())
        .addAction(R.drawable.friend_add, null)
        .show();
    }
    
    private View onCreateTitleMiddleView() {
        ChooseButton button = new ChooseButton(getContext());
        button.setPositiveButton(R.string.friend_by_group, null);
        button.setNegativeButton(R.string.friend_all, null);
        
        button.chooseNegativeButton();
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        button.setLayoutParams(params);
        
        return button;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        injectListContainer(root, R.layout.friend_list_fragment);
        return root;
    }
    
    @Override
    protected void setupListView(ListView listView) {
        listView.setDivider(new InsetDrawable(getResources().getDrawable(R.color.divider_horizontal), 
                AndroidUtil.dp2px(getContext(), 35), 0, 0, 0));
        listView.setDividerHeight(1);
        
        listView.addHeaderView(onCreateListHeader());
        listView.setHeaderDividersEnabled(false);
    }
    
    private View onCreateListHeader() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.friend_list_header, (ViewGroup) null);
        Injector.inject(list_header, view);
        list_header.setupView();
        
        ViewSize.observeViewSize(view, new ViewSizeObserver() {
            
            @Override
            public void onSizeChanged(View view, ViewSize size) {
                letter_bar.getLayoutParams().height = getView().getHeight() - size.height;
                letter_bar.requestLayout();
            }
        });
        return view;
    }
    
    class ListHeader {
        
        @InjectView(R.id.search_box)
        SearchBox search_box;

        @InjectView(R.id.action_container)
        ActionContainer action_container;
        
        public void setupView() {
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
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupLetterBar(letter_bar);
    }
    
    private void setupLetterBar(LetterBar letter_bar) {
        letter_bar.setLetters(FriendListItem.CATEGORY);
        letter_bar.replaceLetter(0, getResources().getDrawable(R.drawable.letter_bar_search));
        letterBarHelper = new LetterBarHelper(letter_bar);
        letterBarHelper.bindListView(getListView());
    }
    
    private void updateLetterBar(ListAdapter adapter) {
        letter_bar.setVisibility(adapter == searchPresenter.adapter || adapter.isEmpty() ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void notifyDataSetChanged() {
        presenter.updateLetterIndex(letterBarHelper, getListView());
        searchPresenter.adapter.update(presenter.adapter.getItems());
        updateLetterBar(getListAdapter());
    }
    
    /**
     * 切换搜索模式
     * 
     * @param inSearch True:进入搜索模式,False:退出搜索模式
     */
    void switchSearchMode(boolean inSearch) {
        ListAdapter adapter = inSearch ? searchPresenter.adapter : presenter.adapter;
        if (getListAdapter() != adapter)
        {
            setListAdapter(adapter);
            getTitleBar().setDisplayShowTitleEnabled(inSearch).setDisplayShowCustomEnabled(!inSearch);
            list_header.action_container.setVisibility(inSearch ? View.GONE : View.VISIBLE);
            updateLetterBar(adapter);
            // ListView will get focus when update the adapter so request focus manually.
            list_header.search_box.requestFocus();
        }
    }
}