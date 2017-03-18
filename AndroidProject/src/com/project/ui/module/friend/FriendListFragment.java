package com.project.ui.module.friend;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.project.R;

import engine.android.framework.ui.BaseListFragment;
import engine.android.widget.ChooseButton;
import engine.android.widget.TitleBar;

/**
 * 好友列表界面
 * 
 * @author Daimon
 */
public class FriendListFragment extends BaseListFragment {
    
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
        button.setPositiveButton(R.string.by_group, null);
        button.setNegativeButton(R.string.All, null);
        
        button.choosePositiveButton();
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        button.setLayoutParams(params);
        
        return button;
    }
}