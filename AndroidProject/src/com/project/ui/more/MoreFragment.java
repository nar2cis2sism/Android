package com.project.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.R;

import engine.android.core.annotation.InjectView;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.util.ui.UIUtil;
import engine.android.widget.TitleBar;

public class MoreFragment extends BaseInfoFragment {

    @InjectView(R.id.avatar)
    ImageView avatar;
    
    @InjectView(R.id.name)
    TextView name;

    @InjectView(R.id.certification)
    Button certification;
    
    @InjectView(R.id.signature)
    TextView signature;
    
    // 我的二维码
    ViewHolder qrcode;
    
    // 我的钱包
    ViewHolder wallet;
    
    // 订单管理
    ViewHolder order;
    
    // 我的评价
    ViewHolder evaluation;
    
    // 群发消息
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

//        // 个人信息
//        layout.findViewById(R.id.header).setOnClickListener(this);
        
        addCategory(root);
        
        // 我的二维码
        qrcode = addComponent(root, inflater, 
                R.drawable.more_qrcode, R.string.more_qrcode);
//        qrcode.getConvertView().setOnClickListener(this);
        
        // 我的钱包
        wallet = addComponent(root, inflater, 
                R.drawable.more_wallet, R.string.more_wallet);
//        wallet.getConvertView().setOnClickListener(this);
        
        // 订单管理
        order = addComponent(root, inflater, 
                R.drawable.more_order, R.string.more_order);
//        order.getConvertView().setOnClickListener(this);
        
        // 我的评价
        evaluation = addComponent(root, inflater, 
                R.drawable.more_evaluation, R.string.more_evaluation);
//        evaluation.getConvertView().setOnClickListener(this);
        
        // 群发消息
        message = addComponent(root, inflater, 
                R.drawable.more_message, R.string.more_message);
//        message.getConvertView().setOnClickListener(this);
        
        return root;
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