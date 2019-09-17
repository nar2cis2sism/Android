package engine.android.framework.ui.fragment;

import engine.android.framework.R;
import engine.android.framework.ui.BaseFragment;
import engine.android.widget.common.image.ZoomImageView;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 图片查看界面（未完待续）
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class ViewImageFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        ZoomImageView view = new ZoomImageView(getContext());
        view.setBackgroundResource(android.R.color.black);
        
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.avatar_default);
        view.setZoomImage(drawable.getBitmap());
        
        return view;
    }
}