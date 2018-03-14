//package com.project.ui.module.message;
//
//import android.os.Bundle;
//import android.os.SystemClock;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//
//import com.project.R;
//import com.project.ui.BaseListFragment;
//import com.project.widget.PinnedHeaderListView;
//import com.project.widget.RefreshListView;
//import com.project.widget.RefreshListView.OnRefreshListener;
//import com.project.widget.TitleBar;
//
//import engine.android.util.UIUtil;
//
///**
// * 会话列表
// */
//
//public class ConversationListFragment extends BaseListFragment {
//    
//    @Override
//    protected void setupTitleBar(TitleBar titleBar) {
//        titleBar
//        .setTitle("消息")
//        .show();
//    }
//    
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(android.R.layout.list_content,
//                container, false);
//        
//        PinnedHeaderListView listView = new PinnedHeaderListView(getContext());
//        listView.setPinnedHeaderView(R.layout.base_list_category);
//        listView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
//        
//        UIUtil.replace(root.findViewById(android.R.id.list), listView, null);
//        
//        return root;
//    }
//    
//    @Override
//    protected void setupListView(ListView listView) {
//        super.setupListView(listView);
//        
//        // Set a listener to be invoked when the list should be refreshed.
//        ((RefreshListView) listView).setOnRefreshListener(new OnRefreshListener() {
//            
//            @Override
//            public boolean doRefresh() {
//                SystemClock.sleep(1000);
//                return true;
//            }
//        });
//    }
//    
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        
//        setListAdapter(new ConversationListAdapter(getContext()));
//    }
//}