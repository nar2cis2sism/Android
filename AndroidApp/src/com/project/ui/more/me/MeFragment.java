package com.project.ui.more.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MySession;
import com.project.storage.db.User;

import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.core.util.CalendarFormat;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.widget.component.TitleBar;

/**
 * 个人信息界面
 * 
 * @author Daimon
 */
public class MeFragment extends BaseInfoFragment {

//    @InjectView(R.id.avatar)
//    ImageView avatar;
//    
//    @InjectView(R.id.name)
//    TextView name;
//
//    @InjectView(R.id.certification)
//    TextView certification;
//    
//    @InjectView(R.id.signature)
//    TextView signature;
//    
    ViewHolder nickname;
    ViewHolder gender;
    ViewHolder birthday;
    ViewHolder area;
    ViewHolder signature;
    
    User user;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = MySession.getUser();
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.me_title)
        .setDisplayUpEnabled(true)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(
                R.layout.me_fragment, container, false);
        addDivider(root);
        
        // 昵称
        nickname = addComponent(root, inflater, 
                R.string.me_nickname, user.nickname, false);
        // 性别
        gender = addComponent(root, inflater, 
                R.string.me_gender, user.isFemale ? "女" : "男", false);
        // 生日
        birthday = addComponent(root, inflater, 
                R.string.me_birthday, CalendarFormat.formatDateByLocale(user.birthday, 0), false);
        // 地区
        area = addComponent(root, inflater, 
                R.string.me_area, user.city, false);
        // 个性签名
        signature = addComponent(root, inflater, 
                R.string.me_signature, user.signature, false);
        
        return root;
    }
//    
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        // 个人信息
//        setupHeader();
//    }
//    
//    private void setupHeader() {
//        User user = presenter.user;
//        
//        avatar.setImageResource(R.drawable.avatar_default);
//        name.setText(user.nickname);
//        certification.setText(presenter.getAuthenticatedText());
//        
//        if (TextUtils.isEmpty(user.signature))
//        {
//            signature.setVisibility(View.GONE);
//        }
//        else
//        {
//            signature.setVisibility(View.VISIBLE);
//            signature.setText(user.signature);
//        }
//    }
//    
//    private ViewHolder addComponent(ViewGroup root, LayoutInflater inflater, 
//            int iconRes, int textId) {
//        View component = inflater.inflate(R.layout.base_list_item_1, root, false);
//        root.addView(component);
//        
//        ViewHolder holder = new ViewHolder(component);
//        holder.setImageView(R.id.icon, iconRes);
//        holder.setTextView(R.id.subject, textId);
//    
//        ImageView arrow = new ImageView(getContext());
//        arrow.setImageResource(R.drawable.arrow_right);
//        
//        View note = holder.getView(R.id.note);
//        UIUtil.replace(note, arrow, note.getLayoutParams());
//        holder.removeView(R.id.note);
//        
//        // Divider
//        addDivider(root, getResources().getColor(R.color.divider_horizontal), 1);
//        
//        return holder;
//    }
}