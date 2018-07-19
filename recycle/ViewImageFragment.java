package com.daan.project.ui.extra;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daan.project.R;
import com.daan.project.ui.BaseFragment;
import com.daan.project.ui.util.PhotoUtil.PhotoInfo;
import com.daan.project.ui.widget.ImageZoomView;
import com.daan.project.ui.widget.ImageZoomView.ZoomState;
import com.daan.project.ui.widget.TitleBar;

import engine.android.util.AndroidUtil;

public class ViewImageFragment extends BaseFragment {
    
    private Pair<Integer, Integer> imageSize;
    
    private PhotoInfo photoInfo;
    
    private Callback callback;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (photoInfo == null)
        {
            throw new RuntimeException("请设置回调接口!");
        }
        
        DisplayMetrics dm = AndroidUtil.getResolution(getActivity());
        
        imageSize = new Pair<Integer, Integer>(dm.widthPixels, dm.heightPixels);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        super.setupTitleBar(titleBar);
        
        TextView delete = new TextView(getContext());
        delete.setText("删除");
        delete.setTextColor(getResources().getColor(R.color.textColorBlue));
        delete.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (callback != null)
                {
                    callback.deleteImage(photoInfo);
                    finish();
                }
            }
        });
        
        titleBar
        .setDisplayUpEnabled(true)
        .addAction(delete)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        ImageZoomView root = new ImageZoomView(getContext());
        root.setZoomImage(photoInfo.getPhoto(
                getContext().getContentResolver(), 
                imageSize.first, imageSize.second, false));
        
        ZoomState state = root.getZoomState();
        state.setPanX(0.5f);
        state.setPanY(0.5f);
        state.setZoom(1.0f);
        
        root.setOnTouchListener(new ImageZoomView.SimpleZoomListener(state));
        
        return root;
    }
    
    public static interface Callback {
        
        public void deleteImage(PhotoInfo photoInfo);
    }
    
    public void setCallback(PhotoInfo photoInfo, Callback callback) {
        this.photoInfo = photoInfo;
        this.callback = callback;
    }
}