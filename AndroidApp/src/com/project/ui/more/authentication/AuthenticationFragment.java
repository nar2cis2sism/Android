package com.project.ui.more.authentication;

import static com.project.network.action.Actions.AUTHENTICATION;

import engine.android.core.annotation.InjectView;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.BaseFragment;
import engine.android.framework.ui.activity.SinglePaneActivity;
import engine.android.framework.ui.presenter.PhotoPresenter;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoCallback;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.util.image.ImageSize;
import engine.android.widget.component.TitleBar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.daimon.yueba.R;
import com.project.network.action.file.Authentication;
import com.project.ui.more.authentication.ViewImageFragment.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * 实名认证
 * 
 * @author Daimon
 */
public class AuthenticationFragment extends BaseFragment implements OnItemClickListener {
    
    @InjectView(R.id.grid)
    GridView grid;
    
    AuthenticationAdapter adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPresenter(new PhotoPresenter(adapter = new AuthenticationAdapter(getContext())));
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.authentication_title)
        // 提交
        .addAction(newTextAction(getString(R.string.authentication_commit), new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                commit();
            }
        }))
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authentication_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupGridView(grid);
    }
    
    private void setupGridView(GridView grid) {
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotoInfo item = adapter.getItem(position);
        if (item == null)
        {
            pickImage();
        }
        else
        {
            ViewImageFragment fragment = new ViewImageFragment();
            fragment.setCallback(item, new Callback() {
                @Override
                public void deleteImage(PhotoInfo photoInfo) {
                    adapter.remove(photoInfo);
                }
            });

            ((SinglePaneActivity) getBaseActivity()).addFragment(fragment);
        }
    }
    
    private void pickImage() {
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setItems(R.array.pick_image, 
        new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        getPresenter(PhotoPresenter.class).takePhoto(true, null);
                        break;
                    case 1:
                        getPresenter(PhotoPresenter.class).pickPhoto(null);
                        break;
                }
            }
        })
        .create();

        getBaseActivity().showDialog("pick_image", dialog);
    }
    
    void commit() {
        List<PhotoInfo> list = new ArrayList<PhotoInfo>(adapter.getItems());
        list.remove(null);
        
        if (list.isEmpty())
        {
            finish();
        }
        else if (getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(getString(R.string.progress_waiting));
            getBaseActivity().sendHttpRequest(new Authentication(list));
        }
    }
    
    @Override
    protected EventHandler registerEventHandler() {
        return new EventHandler();
    }
    
    private class EventHandler extends BaseActivity.EventHandler {
        
        public EventHandler() {
            super(AUTHENTICATION);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            hideProgress();
            ((SinglePaneActivity) getBaseActivity()).replaceFragment(new AuthenticationFinishFragment());
        }
    }
}

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