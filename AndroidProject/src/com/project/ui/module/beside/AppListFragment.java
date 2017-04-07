package com.project.ui.module.beside;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.project.R;

import engine.android.core.extra.ContextualMode;
import engine.android.framework.ui.BaseListFragment;
import engine.android.widget.TitleBar;

public class AppListFragment extends BaseListFragment {
    
    AppListPresenter presenter;
    
    ContextualMode contextual;
    
    boolean processMode;                // 选择App，禁止后台进程启动
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        presenter = addPresenter(AppListPresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        TextView action = new TextView(getContext());
        action.setText("禁止");
        action.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                setProcessMode(true);
            }
        });
        
        titleBar
        .setTitle("应用程序管理")
        .addAction(action)
        .show();
    }
    
    @Override
    protected void setupListView(ListView listView) {
        contextual = new AppListContextualMode(listView);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (processMode)
        {
            presenter.disable(position);
        }
    }
    
    private void setProcessMode(boolean processMode) {
        if (this.processMode == processMode) return;
        if (this.processMode = processMode)
        {
            TextView action = new TextView(getContext());
            action.setText("退出");
            action.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    setProcessMode(false);
                }
            });
            
            getTitleBar()
            .reset()
            .setTitle("应用程序管理")
            .addAction(action)
            .show();
        }
        else
        {
            setupTitleBar(getTitleBar().reset());
        }
        
        presenter.adapter.setProcessMode(processMode);
    }

    class AppListContextualMode extends ContextualMode {
    
        public AppListContextualMode(AbsListView listView) {
            super(listView, R.menu.app_list);
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            boolean handle = super.onCreateActionMode(mode, menu);
            
            if (handle) setProcessMode(false);
            
            MenuItem shareItem = menu.findItem(R.id.share);
            ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
            shareActionProvider.setShareIntent(getShareIntent());
            
            return handle;
        }
        
        private Intent getShareIntent() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            return intent;
        }
    }
}