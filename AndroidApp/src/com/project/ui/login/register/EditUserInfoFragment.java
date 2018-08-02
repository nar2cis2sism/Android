package com.project.ui.login.register;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.storage.db.Region;
import com.project.storage.db.User;
import com.project.ui.more.region.RegionFragment;

import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.core.util.CalendarFormat;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.framework.ui.extra.TextEditFragment;
import engine.android.framework.ui.widget.DatePickerDialog;
import engine.android.framework.ui.widget.DatePickerDialog.OnDateSetListener;
import engine.android.util.Util;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.TitleBar;

import java.util.Calendar;

/**
 * 完善个人信息界面
 * 
 * @author Daimon
 */
public class EditUserInfoFragment extends RegisterInfoFragment implements OnClickListener {
    
    ViewHolder nickname;
    ViewHolder gender;
    ViewHolder birthday;
    ViewHolder region;
    ViewHolder signature;
    
    User user;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        user = Util.clone(MySession.getUser());
        user = new User();
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.me_title)
        .setDisplayUpEnabled(true)
        .show();
    }
    
    @Override
    protected int getIntroductionResId() {
        return R.string.me_introduction;
    }

    @Override
    protected void setupContent(FrameLayout content) {
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        
        // 昵称
        setupNickName(inflater, root);
        // 性别
        gender = addComponent(root, inflater, 
                R.string.me_gender, user.getGenderText(), false);
        gender.getConvertView().setOnClickListener(this);
        // 生日
        birthday = addComponent(root, inflater, 
                R.string.me_birthday, user.birthday != 0 ? user.getBirthdayText() : getString(R.string.me_no_value), false);
        birthday.getConvertView().setOnClickListener(this);
        // 地区
        region = addComponent(root, inflater, 
                R.string.me_region, user.region, false);
        region.getConvertView().setOnClickListener(this);
        // 个性签名
        signature = addComponent(root, inflater, 
                R.string.me_signature, Util.getString(user.signature, getString(R.string.me_no_value)), false);
        signature.getConvertView().setOnClickListener(this);
        
        UIUtil.replace(content, root, content.getLayoutParams());
    }
    
    private void setupNickName(LayoutInflater inflater, ViewGroup root) {
        EditText input = new EditText(getContext());
        input.setBackgroundDrawable(null);
        input.setPadding(0, 0, 0, 0);
        input.setText(user.nickname);
        input.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
        input.setSingleLine();
        
        nickname = addComponent(root, inflater, 
                R.string.me_nickname, input, false);
    }
    
    @Override
    protected OnClickListener getNextListener() {
        return new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                next();
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v == gender.getConvertView())
        {
            chooseGender();
        }
        else if (v == birthday.getConvertView())
        {
            chooseBirthday();
        }
        else if (v == region.getConvertView())
        {
            Region region = null;
            if (!TextUtils.isEmpty(user.region_code))
            {
                region = new Region();
                region.code = user.region_code;
            }
            
            RegionFragment fragment = new RegionFragment();
            fragment.setListener(region, new Listener<Region>() {
                
                @Override
                public void update(Region data) {
                    user.setRegion(data);
                    EditUserInfoFragment.this.region.setTextView(R.id.text, user.region);
                }
            });
            
            ((SinglePaneActivity) getBaseActivity()).addFragment(fragment);
        }
        else if (v == signature.getConvertView())
        {
            TextEditFragment.Params params = new TextEditFragment.Params();
            params.title = getString(R.string.me_signature);
            params.maxEms = 30;

            TextEditFragment fragment = new TextEditFragment();
            fragment.setListener(user.signature, new Listener<CharSequence>() {
                
                @Override
                public void update(CharSequence data) {
                    signature.setTextView(R.id.text, user.signature = data.toString());
                }
            });
            fragment.setArguments(TextEditFragment.buildParams(params));
            
            ((SinglePaneActivity) getBaseActivity()).addFragment(fragment);
        }
    }
    
    private void chooseGender() {
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setTitle(R.string.dialog_gender_title)
        .setItems(R.array.gender, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.isFemale = which == 1;
                gender.setTextView(R.id.text, user.getGenderText());
            }
        })
        .create();

        getBaseActivity().showDialog("gender", dialog);
    }
    
    private void chooseBirthday() {
        Calendar date = null;
        if (user.birthday != 0)
        {
            date = CalendarFormat.getCalendar(user.birthday);
        }
        
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new OnDateSetListener() {
            
            @Override
            public void onDateSet(Calendar date) {
                user.birthday = date.getTimeInMillis();
                birthday.setTextView(R.id.text, user.getBirthdayText());
            }
        }, date);
        
        getBaseActivity().showDialog("birthday", dialog);
    }
    
    void next() {
        
    }
}