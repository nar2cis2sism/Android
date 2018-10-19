package com.project.ui.more.authentication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.daimon.yueba.R;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoCallback;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.util.image.ImageSize;

class AuthenticationAdapter extends JavaBeanAdapter<PhotoInfo> implements PhotoCallback {
    
    private final ImageSize size;

    public AuthenticationAdapter(Context context) {
        super(context, 0);
        size = new ImageSize();
        size.setAspectRatio(5, 4);
        // Place add_image.
        add(null);
    }
    
    @Override
    protected View newView(int position, LayoutInflater inflater, ViewGroup parent) {
        ImageView view = new ImageView(getContext());
        view.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ImageSize.adjustViewSize(view, size);
        view.setScaleType(ScaleType.CENTER_CROP);
        return view;
    }

    @Override
    protected void bindView(int position, ViewHolder holder, PhotoInfo item) {
        ImageView view = (ImageView) holder.getConvertView();
        if (item == null)
        {
            view.setImageResource(R.drawable.add_image);
        }
        else
        {
            view.setImageBitmap(item.getPhoto(getContext().getContentResolver(),
                    size.getWidth(), size.getHeight(), false));
        }
    }

    @Override
    public void onPhotoCapture(PhotoInfo info) {
        insert(info, getCount() - 1);
    }
}