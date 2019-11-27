package engine.android.framework.ui.fragment;

import engine.android.framework.ui.BaseFragment;
import engine.android.widget.common.image.ZoomImageView;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 图片查看界面
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
        view.setZoomImage((Bitmap) getData());

        return view;
    }

    public void setImage(Bitmap image) {
        super.setListener(image, null);
    }
}