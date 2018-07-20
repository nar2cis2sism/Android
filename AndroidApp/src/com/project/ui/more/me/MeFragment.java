package com.project.ui.more.me;

import static com.project.network.action.Actions.AVATAR;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.file.UploadAvatar;
import com.project.storage.db.User;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.framework.ui.extra.TextEditFragment;
import engine.android.framework.ui.presenter.PhotoPresenter;
import engine.android.framework.ui.presenter.PhotoPresenter.CropAttribute;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoCallback;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.util.Util;
import engine.android.widget.component.TitleBar;

/**
 * 个人信息界面
 * 
 * @author Daimon
 */
public class MeFragment extends BaseInfoFragment implements PhotoCallback, OnClickListener {

    @InjectView(R.id.avatar)
    ImageView avatar;
    
    ViewHolder nickname;
    ViewHolder gender;
    ViewHolder birthday;
    ViewHolder area;
    ViewHolder signature;
    
    User user;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableReceiveEvent(AVATAR);
        addPresenter(new PhotoPresenter(this));
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
                R.string.me_gender, user.getGenderText(), false);
        // 生日
        birthday = addComponent(root, inflater, 
                R.string.me_birthday, user.getBirthdayText(), false);
        // 地区
        area = addComponent(root, inflater, 
                R.string.me_area, user.city, false);
        // 个性签名
        signature = addComponent(root, inflater, 
                R.string.me_signature, Util.getString(user.signature, getString(R.string.me_no_value)), false);
        signature.getConvertView().setOnClickListener(this);
        
        
        return root;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAvatar();
    }
    
    private void setupAvatar() {
        avatar = AvatarImageView.display(avatar, user.getAvatarUrl());
    }
    
    @OnClick(R.id.header)
    void toMe() {
//        ((SinglePaneActivity) getBaseActivity()).addFragment(new ViewImageFragment());
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setItems(R.array.pick_image, 
        new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CropAttribute attr = new CropAttribute();
                attr.saveToFile();
                
                switch (which) {
                    case 0:
                        getPresenter(PhotoPresenter.class).takePhoto(true, attr);
                        break;
                    case 1:
                        getPresenter(PhotoPresenter.class).pickPhoto(attr);
                        break;
                }
            }
        })
        .create();

        getBaseActivity().showDialog("pick_image", dialog);
    }

    @Override
    public void onPhotoCapture(PhotoInfo info) {
        getBaseActivity().showProgress(ProgressSetting.getDefault()
        .setMessage(getString(R.string.progress_upload_avatar)));
        
        getBaseActivity().sendHttpRequest(new UploadAvatar(info));
    }
    
    @Override
    protected void onReceiveSuccess(String action, Object param) {
        if (AVATAR.equals(action))
        {
            // 头像上传成功
            getBaseActivity().hideProgress();
            MyApp.showMessage(getString(R.string.toast_upload_avatar_success));
            setupAvatar();
        }
    }
    
    @Override
    protected void onReceiveFailure(String action, int status, Object param) {
        if (AVATAR.equals(action))
        {
            // 头像上传失败
            getBaseActivity().hideProgress();
            MyApp.showMessage(getString(R.string.toast_upload_avatar_failure));
        }
        else
        {
            super.onReceiveFailure(action, status, param);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == signature.getConvertView())
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
}