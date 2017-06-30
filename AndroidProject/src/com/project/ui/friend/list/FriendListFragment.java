package com.project.ui.friend.list;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.project.R;
import com.project.app.bean.FriendListItem;

import engine.android.core.Injector;
import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseListFragment;
import engine.android.util.AndroidUtil;
import engine.android.util.ui.ViewSize;
import engine.android.util.ui.ViewSize.ViewSizeObserver;
import engine.android.widget.ChooseButton;
import engine.android.widget.SearchBox;
import engine.android.widget.SearchBox.SearchProvider;
import engine.android.widget.TitleBar;
import engine.android.widget.common.ActionContainer;
import engine.android.widget.common.LetterBar;
import engine.android.widget.common.LetterBar.OnLetterChangedListener;

/**
 * 好友列表界面
 * 
 * @author Daimon
 */
public class FriendListFragment extends BaseListFragment implements OnLetterChangedListener {
    
    ListHeader list_header = new ListHeader();
    
    @InjectView(R.id.letter_bar)
    LetterBar letter_bar;

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
        .setTitle(R.string.search) // 搜索时显示
        .setDisplayShowTitleEnabled(false)
        .setDisplayShowCustomEnabled(true)
        .setCustomView(onCreateTitleMiddleView())
        .addAction(R.drawable.friend_add)
        .show();
    }
    
    private View onCreateTitleMiddleView() {
        ChooseButton button = new ChooseButton(getContext());
        button.setPositiveButton(R.string.friend_by_group, null);
        button.setNegativeButton(R.string.All, null);
        
        button.choosePositiveButton();
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        button.setLayoutParams(params);
        
        return button;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        injectListContainer(root, R.layout.friend_list_view);
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
            }
        });
        return view;
    }
    
    class ListHeader implements SearchProvider {
        
        @InjectView(R.id.search_box)
        SearchBox search_box;

        @InjectView(R.id.action_container)
        ActionContainer action_container;
        
        public void setupView() {
            // 搜索框
            search_box.setSearchProvider(this);
            
            // 群
            action_container.addAction(R.drawable.friend_group, R.string.friend_group);
            // 讨论组
            action_container.addAction(R.drawable.friend_discuss, R.string.friend_discuss);
            // 公众好友
            action_container.addAction(R.drawable.friend_public, R.string.friend_public);
            // 好友推荐
            action_container.addAction(R.drawable.friend_recommend, R.string.friend_recommend);
        }

        @Override
        public void search(CharSequence constraint) {
            searchPresenter.search(constraint);
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
        letter_bar.setOnLetterChangedListener(this);
    }

    @Override
    public void onLetterChanged(String letter) {
        presenter.onLetterChanged(letter);
    }
    
    private void updateLetterBar() {
        letter_bar.setVisibility(searchPresenter.inSearch || getListAdapter().isEmpty() ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void notifyDataSetChanged() {
        presenter.updateLetterMap();
        searchPresenter.adapter.update(presenter.adapter.getItems());
        updateLetterBar();
    }
    
    public void inSearch(boolean inSearch) {
        setListAdapter(inSearch ? searchPresenter.adapter : presenter.adapter);
        // ListView will get focus when update the adapter so request focus manually.
        list_header.search_box.requestFocus();

        list_header.action_container.setVisibility(inSearch ? View.GONE : View.VISIBLE);
        getTitleBar().setDisplayShowTitleEnabled(inSearch).setDisplayShowCustomEnabled(!inSearch);
        updateLetterBar();
    }
}