package engine.android.framework.ui.extra;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import engine.android.framework.R;
import engine.android.framework.ui.BaseFragment;
import engine.android.widget.common.image.ZoomImageView;

/**
 * 图片查看界面
 * 
 * @author Daimon
 * @version N
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
        
        
//        ImageZoomView root = new ImageZoomView(getContext());
//        root.setZoomImage(photoInfo.getPhoto(
//                getContext().getContentResolver(), 
//                imageSize.first, imageSize.second, false));
//        
//        ZoomState state = root.getZoomState();
//        state.setPanX(0.5f);
//        state.setPanY(0.5f);
//        state.setZoom(1.0f);
//        
//        root.setOnTouchListener(new ImageZoomView.SimpleZoomListener(state));
//        
//        return root;
    }
    
    
    
    
    
    
    
    
    
    
    
    
//    private Pair<Integer, Integer> imageSize;
//    
//    private PhotoInfo photoInfo;
//    
//    private Callback callback;
//    
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        
//        if (photoInfo == null)
//        {
//            throw new RuntimeException("请设置回调接口!");
//        }
//        
//        DisplayMetrics dm = AndroidUtil.getResolution(getActivity());
//        
//        imageSize = new Pair<Integer, Integer>(dm.widthPixels, dm.heightPixels);
//    }
//    
}