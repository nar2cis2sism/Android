package com.project.ui.more.authentication;

import static com.project.network.action.Actions.AUTHENTICATION;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.daimon.yueba.R;
import com.project.network.action.file.Authentication;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.annotation.InjectView;
import engine.android.framework.ui.BaseFragment;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.framework.ui.presenter.PhotoPresenter;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.widget.component.TitleBar;

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
        enableReceiveEvent(AUTHENTICATION);
        
        adapter = new AuthenticationAdapter(getContext());
        addPresenter(new PhotoPresenter(adapter));
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
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            getBaseActivity().showProgress(ProgressSetting.getDefault()
            .setMessage(getString(R.string.progress_waiting)));
            
            sendAuthenticationAction(list);
        }
    }

    /******************************* 实名认证 *******************************/
    
    private void sendAuthenticationAction(List<PhotoInfo> list) {
        getBaseActivity().sendHttpRequest(new Authentication(list));
    }
    
    @Override
    protected void onReceiveSuccess(String action, Object param) {
        if (AUTHENTICATION.equals(action))
        {
            getBaseActivity().hideProgress();
            ((SinglePaneActivity) getBaseActivity()).replaceFragment(new AuthenticationFinishFragment());
        }
    }
}