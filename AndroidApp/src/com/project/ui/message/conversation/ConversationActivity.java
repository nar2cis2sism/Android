package com.project.ui.message.conversation;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import com.project.ui.MainActivity;
import com.project.ui.message.conversation.ConversationPresenter.ConversationParams;

import engine.android.framework.ui.extra.SinglePaneActivity;

/**
 * 聊天界面
 * 
 * @author Daimon
 */
public class ConversationActivity extends SinglePaneActivity {
    
    public static Intent buildIntent(Context context, ConversationParams params) {
        return new Intent(context, ConversationActivity.class)
        .putExtras(ConversationFragment.buildParams(params));
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