package com.project.ui.more.authentication;

import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.widget.component.TitleBar;

import android.view.View;
import android.view.View.OnClickListener;

import com.project.app.MyContext;

public class ViewImageFragment extends engine.android.framework.ui.fragment.ViewImageFragment {

    private PhotoInfo photoInfo;
    private Callback callback;

    public interface Callback {

        void deleteImage(PhotoInfo photoInfo);
    }

    public void setCallback(PhotoInfo photoInfo, Callback callback) {
        setImage((this.photoInfo = photoInfo).getPhoto(MyContext.getContentResolver()));
        this.callback = callback;
    }

    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .addAction(newTextAction("删除", new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                {
                    callback.deleteImage(photoInfo);
                    finish();
                }
            }
        }))
        .show();
    }
}
