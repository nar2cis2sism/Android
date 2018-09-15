package com.project.ui.message.conversation;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import com.project.storage.db.Friend;
import com.project.ui.MainActivity;

import engine.android.framework.ui.BaseFragment.ParamsBuilder;
import engine.android.framework.ui.extra.SinglePaneActivity;

/**
 * 聊天界面
 * 
 * @author Daimon
 */
public class ConversationActivity extends SinglePaneActivity {
    
    public static class ConversationParams {
        
        public final String account;            // 好友账号
        Friend friend;                          // 好友信息
        
        public ConversationParams(String account) {
            this.account = account;
        }
    }
    
    public static Intent buildIntent(Context context, ConversationParams params) {
        return new Intent(context, ConversationActivity.class)
        .putExtras(ParamsBuilder.build(params));
    }
    
    @Override
    protected Fragment onCreateFragment() {
        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        replaceFragment(onCreateFragment());
    }
    
    @Override
    public void onBackPressed() {
        navigateUpTo(MainActivity.class);
    }
}