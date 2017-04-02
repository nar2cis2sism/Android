package com.project.ui.module.friend.list;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.project.R;
import com.project.app.bean.FriendListItem;

import engine.android.framework.ui.BaseListFragment;
import engine.android.util.AndroidUtil;
import engine.android.widget.ActionContainer;
import engine.android.widget.ChooseButton;
import engine.android.widget.SearchBox;
import engine.android.widget.TitleBar;

/**
 * 好友列表界面
 * 
 * @author Daimon
 */
public class FriendListFragment extends BaseListFragment {
    
    FriendListPresenter presenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        presenter = addPresenter(FriendListPresenter.class);
        setDataSource(new DataSetSource<FriendListItem>(presenter.adapter, presenter.loader));
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayShowTitleEnabled(false)
        .setDisplayShowCustomEnabled(true)
        .setCustomView(onCreateTitleMiddleView())
        .addAction(R.drawable.friend_add, new OnClickListener() {
            
            @Override
            public void onClick(View v) {
//                startActivity(SinglePaneActivity.buildIntent(
//                        getContext(), AddFriendFragment.class, null));
            }
        })
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
        replaceListView(root, R.layout.friend_list_view);
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
        View header = LayoutInflater.from(getContext()).inflate(R.layout.friend_list_header, null);
        // 搜索框
        SearchBox search_box = (SearchBox) header.findViewById(R.id.search_box);
        
        ActionContainer action_container = (ActionContainer) header.findViewById(R.id.action_container);
        // 群
        action_container.addAction(R.drawable.friend_group, R.string.friend_group);
        // 讨论组
        action_container.addAction(R.drawable.friend_discuss, R.string.friend_discuss);
        // 公众好友
        action_container.addAction(R.drawable.friend_public, R.string.friend_public);
        // 好友推荐
        action_container.addAction(R.drawable.friend_recommend, R.string.friend_recommend);
        
        return header;
        
        
        
//        View headerView = inflater.inflate(R.layout.friend_list_header, null);
//    
//        SearchBox search_box = (SearchBox) headerView.findViewById(R.id.search_box);
//        search_box.getSearchEditText().addTextChangedListener(new MyTextWatcher() {
//            
//            @Override
//            public void changeToEmpty(String before) {
//                showFriendListWithSearch(false);
//            }
//            
//            @Override
//            public void changeFromEmpty(String after) {
//                showFriendListWithSearch(true);
//            }
//            
//            @Override
//            public void afterTextChanged(Editable s) {
//                super.afterTextChanged(s);
//                search(s.toString());
//            }
//        });
//        
//        header_action = headerView.findViewById(R.id.header_action);
//        
//        headerView.findViewById(R.id.group).setOnClickListener(this);
//        headerView.findViewById(R.id.discuss).setOnClickListener(this);
//        headerView.findViewById(R.id.public_friend).setOnClickListener(this);
//        headerView.findViewById(R.id.friend_recommend).setOnClickListener(this);
//        
//        return headerView;
    }
}