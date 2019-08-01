package com.project.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.TextView;

import com.daimon.yueba.R;

import engine.android.framework.ui.BaseActivity;
import engine.android.framework.util.HtmlImageGetter;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;
import protocol.http.NavigationData.AppUpgradeInfo;

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
    
    /**
     * 设置HTML文本（包含图片获取）
     */
    public static void setHtml(TextView text, String html) {
        text.setText(Html.fromHtml(html, new HtmlImageGetter(text), null));
    }
    
    /**
     * 控件不可操作时置灰（半透明）
     */
    public static void setupAlpha(View view) {
        if (view.isEnabled())
        {
            view.setAlpha(1);
        }
        else
        {
            view.setAlpha(0.5f);
        }
    }
}