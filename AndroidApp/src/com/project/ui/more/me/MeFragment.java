package com.project.ui.more.me;

import static com.project.network.action.Actions.AVATAR;
import static com.project.network.action.Actions.EDIT_USER_INFO;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.core.util.CalendarFormat;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.activity.SinglePaneActivity;
import engine.android.framework.ui.activity.SinglePaneActivity.OnBackListener;
import engine.android.framework.ui.dialog.DatePickerDialog;
import engine.android.framework.ui.dialog.DatePickerDialog.OnDateSetListener;
import engine.android.framework.ui.dialog.OptionDialog;
import engine.android.framework.ui.fragment.BaseInfoFragment;
import engine.android.framework.ui.fragment.TextEditFragment;
import engine.android.framework.ui.fragment.ViewImageFragment;
import engine.android.framework.ui.fragment.region.Region;
import engine.android.framework.ui.fragment.region.RegionFragment;
import engine.android.framework.ui.presenter.PhotoPresenter;
import engine.android.framework.ui.presenter.PhotoPresenter.CropAttribute;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoCallback;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.util.Util;
import engine.android.widget.component.TitleBar;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.file.UploadAvatar;
import com.project.network.action.http.EditUserInfo;
import com.project.storage.db.User;

import java.util.Calendar;

/**
 * 个人信息界面
 * 
 * @author Daimon
 */
public class MeFragment extends BaseInfoFragment implements PhotoCallback, OnClickListener, OnBackListener {

    @InjectView(R.id.avatar)
    AvatarImageView avatar;
    
    ViewHolder nickname;
    ViewHolder gender;
    ViewHolder birthday;
    ViewHolder region;
    ViewHolder signature;
    
    User user;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPresenter(new PhotoPresenter(this));
        
        user = Util.clone(MySession.getUser());
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.me_title)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(
                R.layout.me_fragment, container, false);
        addDivider(root);
        // 昵称
        setupNickName(root, inflater);
        // 性别
        gender = addComponent(root, inflater, R.string.me_gender, user.getGenderText(), false);
        gender.getConvertView().setOnClickListener(this);
        // 生日
        birthday = addComponent(root, inflater, R.string.me_birthday, 
                user.birthday != 0 ? user.getBirthdayText() : getString(R.string.me_no_value), false);
        birthday.getConvertView().setOnClickListener(this);
        // 地区
        region = addComponent(root, inflater, R.string.me_region, user.region, false);
        region.getConvertView().setOnClickListener(this);
        // 个性签名
        signature = addComponent(root, inflater, R.string.me_signature, 
                Util.getString(user.signature, getString(R.string.me_no_value)), false);
        signature.getConvertView().setOnClickListener(this);
        
        return root;
    }
    
    @SuppressWarnings("deprecation")
    private void setupNickName(ViewGroup root, LayoutInflater inflater) {
        EditText input = new EditText(getContext());
        input.setBackgroundDrawable(null);
        input.setPadding(0, 0, 0, 0);
        input.setSingleLine();
        input.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
        input.setText(user.nickname);
        
        nickname = addComponent(root, inflater, R.string.me_nickname, input, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatar.display(user.getAvatarUrl());
    }
    
    @OnClick(R.id.header)
    void header() {
        OptionDialog dialog = new OptionDialog(getContext());
        dialog.setItems(R.array.pick_image, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CropAttribute attr = new CropAttribute().saveToFile();
                switch (which) {
                    case 0:
                        getPresenter(PhotoPresenter.class).takePhoto(true, attr);
                        break;
                    case 1:
                        getPresenter(PhotoPresenter.class).pickPhoto(attr);
                        break;
                }
            }
        });

        getBaseActivity().showDialog("pick_image", dialog);
    }

    @Override
    public void onPhotoCapture(PhotoInfo info) {
        showProgress(R.string.progress_upload_avatar);
        getBaseActivity().sendHttpRequest(new UploadAvatar(info));
    }

    @OnClick(R.id.avatar)
    void avatar() {
        startFragment(ViewImageFragment.class);
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
            chooseRegion();
        }
        else if (v == signature.getConvertView())
        {
            editSignature();
        }
    }
    
    private void chooseGender() {
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setTitle(R.string.dialog_gender_title)
        .setItems(R.array.gender, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.gender = which;
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
    
    private void chooseRegion() {
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
                MeFragment.this.region.setTextView(R.id.text, user.region);
            }
        });
        
        ((SinglePaneActivity) getBaseActivity()).addFragment(fragment);
    }
    
    private void editSignature() {
        TextEditFragment.Params params = new TextEditFragment.Params();
        params.title = getString(R.string.me_signature);
        params.maxEms = 30;

        TextEditFragment fragment = new TextEditFragment();
        fragment.setArguments(ParamsBuilder.build(params));
        fragment.setListener(user.signature, new Listener<String>() {
            
            @Override
            public void update(String data) {
                signature.setTextView(R.id.text, user.signature = data);
            }
        });
        
        ((SinglePaneActivity) getBaseActivity()).addFragment(fragment);
    }

    @Override
    public boolean onBackPressed() {
        user.nickname = ((TextView) nickname.getView(R.id.text)).getText().toString();
        
        final EditUserInfo action = new EditUserInfo(user);
        if (!action.status.isChanged())
        {
            return false;
        }
        
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setTitle(R.string.dialog_tip_title)
        .setMessage(R.string.dialog_edit_info)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress(R.string.progress_waiting);
                getBaseActivity().sendHttpRequest(action);
            }
        })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
        .create();

        getBaseActivity().showDialog("edit_info", dialog);
        return true;
    }
    
    @Override
    protected EventHandler registerEventHandler() {
        return new EventHandler();
    }
    
    private class EventHandler extends BaseActivity.EventHandler {
        
        public EventHandler() {
            super(AVATAR, EDIT_USER_INFO);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            if (AVATAR.equals(action))
            {
                // 头像上传成功
                hideProgress();
                MyApp.showMessage(getString(R.string.toast_upload_avatar_success));
                avatar.display(MySession.getUser().getAvatarUrl());
            }
            else if (EDIT_USER_INFO.equals(action))
            {
                finish();
            }
        }
        
        @Override
        protected void onReceiveFailure(String action, int status, Object param) {
            if (AVATAR.equals(action))
            {
                // 头像上传失败
                hideProgress();
                MyApp.showMessage(getString(R.string.toast_upload_avatar_failure));
            }
            else
            {
                super.onReceiveFailure(action, status, param);
            }
        }
    }
}