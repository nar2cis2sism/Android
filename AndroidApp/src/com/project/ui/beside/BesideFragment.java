package com.project.ui.beside;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.ui.BaseFragment;
import engine.android.framework.ui.dialog.MessageDialog;
import engine.android.plugin.Plugin;
import engine.android.util.AndroidUtil;
import engine.android.util.file.FileManager;
import engine.android.util.file.FileUtils;
import engine.android.util.secure.ZipUtil;
import engine.android.widget.common.layout.ActionContainer;
import engine.android.widget.component.TitleBar;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimon.yueba.R;

import java.io.File;

/**
 * 身边界面
 * 
 * @author Daimon
 */
public class BesideFragment extends BaseFragment implements OnLongClickListener {
    
    private static final String GAME_PACKAGE_NAME = "com.tower";
    
    @InjectView(R.id.action_container)
    ActionContainer action_container;
    
    @InjectView(R.id.test)
    TextView test;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar.setTitle("身边").show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beside_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupActionContainer(action_container);
        test.setOnLongClickListener(this);
    }
    
    private void setupActionContainer(ActionContainer action_container) {
        int paddingVertical = AndroidUtil.dp2px(getContext(), 36);
        action_container.addAction(null, "监测预警").setPadding(0, paddingVertical, 0, paddingVertical);
        action_container.addAction(null, "图文资讯");
        action_container.addAction(null, "电话资讯");
        
        action_container.setDividerDrawable(getContext().getResources()
                .getDrawable(R.color.divider_horizontal));
        action_container.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    }
    
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.test:
                AndroidUtil.uninstallApp(getContext(), GAME_PACKAGE_NAME);
                break;
        }
        
        return true;
    }

    @OnClick(R.id.test)
    void test() {
        try {
            AndroidUtil.startApp(getContext(), GAME_PACKAGE_NAME);
        } catch (Exception e) {
            // 程序未安装
            showUninstallDialog();
        }
    }
    
    private void showUninstallDialog() {
        MessageDialog dialog = new MessageDialog(getContext())
        .setMessage("程序未安装")
        .setPositiveButton("立即安装", new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runGame(true);
            }
        })
        .setNegativeButton("以插件方式运行", new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runGame(false);
            }
        });
        dialog.setCancelable(true);
        getBaseActivity().showDialog("uninstall", dialog);
    }
    
    private void runGame(boolean installApp) {
        try {
            String fileName = "TowerDefence.zip";
            File dir = FileManager.getCacheDir(getContext(), true);
            File zipFile = new File(dir, fileName);
            FileManager.createFileIfNecessary(zipFile);
            FileUtils.copyToFile(getContext().getAssets().open("project/" + fileName), zipFile);
            ZipUtil.unzip(zipFile, dir.getAbsolutePath());
            
            File apk = FileManager.searchFile(dir, "TowerDefence.apk");
            if (installApp)
            {
                AndroidUtil.installApp(getContext(), apk);
            }
            else
            {
                Plugin.loadPluginFromFile(apk);
                startActivity(new Intent().setClassName(GAME_PACKAGE_NAME, "com.tower.TowerDefenceActivity"));
            }
        } catch (Exception e) {
            LOG.log(e);
        }
    }
}