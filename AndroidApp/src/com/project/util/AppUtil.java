package com.project.util;

import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.activity.SinglePaneActivity;
import engine.android.framework.util.HtmlImageGetter;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.ui.login.LoginFragment;

import protocol.http.NavigationData.AppUpgradeInfo;

public class AppUtil {
    
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
    
    public static void logout(Context context) {
        MyApp.global().getSocketManager().close();
        // 清除缓存数据
        MySession.setUser(null);
        // 退到登录界面
        MyApp.getApp().getActivityStack().popupAllActivities();
        context.startActivity(SinglePaneActivity.buildIntent(context, LoginFragment.class, null));
    }

    /**
     * 发起手Q客户端申请加群
     *
     * @return True:呼起手Q成功,False:呼起失败
     */
    public static boolean joinQQGroup(Context context) {
        String key = "SyxgkrqR3B0cs8fQA32H650faPGtyuxf";

        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}