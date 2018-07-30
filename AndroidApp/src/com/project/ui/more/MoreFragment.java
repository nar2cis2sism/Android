package com.project.ui.more;

import static com.project.network.action.Actions.AVATAR;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.app.MySession;
import com.project.storage.db.User;
import com.project.ui.more.authentication.AuthenticationFragment;
import com.project.ui.more.me.MeFragment;
import com.project.ui.more.setting.SettingFragment;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.network.ConnectionStatus;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.TitleBar;

/**
 * 更多界面
 * 
 * @author Daimon
 */
public class MoreFragment extends BaseInfoFragment {

    @InjectView(R.id.avatar)
    ImageView avatar;
    
    @InjectView(R.id.name)
    TextView name;

    @InjectView(R.id.authentication)
    TextView authentication;
    
    @InjectView(R.id.signature)
    TextView signature;
    
    ViewHolder qrcode;
    ViewHolder wallet;
    ViewHolder order;
    ViewHolder evaluation;
    ViewHolder message;
    
    User user;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableReceiveEvent(AVATAR);
        user = MySession.getUser();
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.more_title)
        .addAction(R.drawable.more_setting, new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                getBaseActivity().startFragment(SettingFragment.class);
            }
        })
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(
                R.layout.more_fragment, container, false);
        addCategory(root);
        
        // 我的二维码
        qrcode = addComponent(root, inflater, 
                R.drawable.more_qrcode, R.string.more_qrcode);
        // 我的钱包
        wallet = addComponent(root, inflater, 
                R.drawable.more_wallet, R.string.more_wallet);
        // 订单状态
        order = addComponent(root, inflater, 
                R.drawable.more_order, R.string.more_order);
        // 我的评价
        evaluation = addComponent(root, inflater, 
                R.drawable.more_evaluation, R.string.more_evaluation);
        // 消息中心
        message = addComponent(root, inflater, 
                R.drawable.more_message, R.string.more_message);
        
        return root;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 个人信息
        setupHeader();
    }
    
    private void setupHeader() {
        setupAvatar();
        name.setText(user.nickname);
        authentication.setText(user.getAuthenticationText());
        if (TextUtils.isEmpty(user.signature))
        {
            signature.setVisibility(View.GONE);
        }
        else
        {
            signature.setVisibility(View.VISIBLE);
            signature.setText(user.signature);
        }
    }
    
    private void setupAvatar() {
        avatar = AvatarImageView.display(avatar, user.getAvatarUrl());
    }
    
    private ViewHolder addComponent(ViewGroup root, LayoutInflater inflater, 
            int iconRes, int textId) {
        View component = inflater.inflate(R.layout.base_list_item_1, root, false);
        root.addView(component);
        
        ViewHolder holder = new ViewHolder(component);
        holder.setImageView(R.id.icon, iconRes);
        holder.setTextView(R.id.subject, textId);
    
        ImageView arrow = new ImageView(getContext());
        arrow.setImageResource(R.drawable.arrow_right);
        
        View note = holder.getView(R.id.note);
        UIUtil.replace(note, arrow, note.getLayoutParams());
        holder.removeView(R.id.note);
        
        // Divider
        addDivider(root, getResources().getColor(R.color.divider_horizontal), 1);
        
        return holder;
    }
    
    @OnClick(R.id.header)
    void toMe() {
        getBaseActivity().startFragment(MeFragment.class);
    }
    
    @OnClick(R.id.authentication)
    void authentication() {
        getBaseActivity().startFragment(AuthenticationFragment.class);
    }
    
    @Override
    protected void onReceive(String action, int status, Object param) {
        if (AVATAR.equals(action))
        {
            if (status == ConnectionStatus.SUCCESS)
            {
                // 头像上传成功
                setupAvatar();
            }
        }
        else
        {
            super.onReceive(action, status, param);
        }
    }
}