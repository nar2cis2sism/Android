package com.project.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;

import com.daimon.yueba.R;

import protocol.http.AppUpgradeInfo;
import engine.android.framework.ui.BaseActivity;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

public class AppUtil {
    
    /**
     * 密码输入限制
     */
    public static final KeyListener passwordKeyListener
    = DigitsKeyListener.getInstance("_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ");
    
    /**
     * 用户密码加密算法
     */
    public static String encryptPassword(String password) {
        return HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
    }
    
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