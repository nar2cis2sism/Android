package com.project.ui.friend.info;

import static com.project.network.action.Actions.GET_FRIEND_INFO;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.network.action.http.GetFriendInfo;
import com.project.storage.db.Friend;

import engine.android.core.annotation.InjectView;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.util.ui.NoUnderlineURL;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.TitleBar;

/**
 * 好友信息界面
 * 
 * @author Daimon
 */
public class FriendInfoFragment extends BaseInfoFragment {
    
    public static class FriendInfoParams {
        
        public final Friend friend;
        
        public FriendInfoParams(Friend friend) {
            this.friend = friend;
        }
    }

    @InjectView(R.id.avatar)
    ImageView avatar;
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.gender)
    ImageView gender;
    @InjectView(R.id.signature)
    TextView signature;
    
    LinearLayout content;
    
    Friend friend;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FriendInfoParams params = ParamsBuilder.parse(getArguments(), FriendInfoParams.class);
        if (params == null || (friend = params.friend) == null)
        {
            friend = new Friend();
            finish();
        }
        else
        {
            registerEventHandler(new EventHandler());
        }
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.friend_info)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(
                R.layout.friend_info_fragment, container, false);
        addCategory(root);
        
        content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);
        
        return root;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
    }
    
    private void setupView() {
        setupHeader();
        setupContent();
    }
    
    private void setupHeader() {
        avatar = AvatarImageView.display(avatar, friend.getAvatarUrl());
        name.setText(friend.displayName);
        gender.setImageResource(friend.isFemale ? R.drawable.icon_female : R.drawable.icon_male);
        UIUtil.setTextVisible(signature, friend.signature);
    }
    
    private void setupContent() {
        content.removeAllViews();
        // 电话号码
        if (!TextUtils.isEmpty(friend.mobile_phone))
        {
            ViewHolder holder = addComponent(content, R.string.friend_phone, friend.mobile_phone);
            // 拨打电话号码
            TextView text = holder.getView(R.id.text);
            Linkify.addLinks(text, Linkify.ALL);
            NoUnderlineURL.replace(text);
        }
        // 地区
        if (!TextUtils.isEmpty(friend.region))
        {
            addComponent(content, R.string.friend_region, friend.region);
        }
    }
    
    private ViewHolder addComponent(ViewGroup root, int titleId, CharSequence text) {
        View component = getBaseActivity().getLayoutInflater().inflate(R.layout.friend_info_item, root, false);
        root.addView(component);
        
        ViewHolder holder = new ViewHolder(component);
        
        if (titleId != NO_TITLE)
        {
            holder.setTextView(R.id.title, titleId);
        }
        
        if (!TextUtils.isEmpty(text))
        {
            holder.setTextView(R.id.text, text);
        }
        
        // Divider
        addDivider(root);
        
        return holder;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getBaseActivity().sendHttpRequest(new GetFriendInfo(friend));
    }
    
    private class EventHandler extends engine.android.framework.ui.BaseFragment.EventHandler {
        
        public EventHandler() {
            super(GET_FRIEND_INFO);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            if (param instanceof Friend)
            {
                friend = (Friend) param;
                setupView();
            }
        }
        
        @Override
        protected void onReceiveFailure(String action, int status, Object param) {
            // Do nothing.
        }
    }
}