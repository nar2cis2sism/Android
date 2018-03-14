package com.project.util;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.daimon.yueba.R;

import engine.android.framework.ui.BaseActivity;
import protocol.java.json.AppUpgradeInfo;

public class AppUpgradeUtil {
    
    /**
     * 升级提示对话框
     * 
     * @param force True:强制升级,False:建议升级
     */
    public static void upgradeApp(BaseActivity activity, AppUpgradeInfo info, boolean force) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
        .setTitle(R.string.dialog_upgrade_title)
        .setMessage(info.desc)
        .setPositiveButton(R.string.dialog_upgrade_now, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 下载安装包
            }
        })
        .setCancelable(false);
        
        if (!force)
        {
            builder.setNegativeButton(R.string.dialog_upgrade_later, null);
        }
        
        activity.showDialog("upgrade", builder.create());
    }
}