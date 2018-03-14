package com.project.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimon.yueba.R;

import engine.android.core.annotation.InjectView;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.util.ui.UIUtil;
import engine.android.widget.TitleBar;

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

    @InjectView(R.id.certification)
    Button certification;
    
    @InjectView(R.id.signature)
    TextView signature;
    
    ViewHolder qrcode;
    ViewHolder wallet;
    ViewHolder order;
    ViewHolder evaluation;
    ViewHolder message;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.more_title)
        .addAction(R.drawable.more_setting)
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
        avatar.setImageResource(R.drawable.avatar_default);
        name.setText("闫昊");
        certification.setText("未认证");
        signature.setText("签名");
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
}