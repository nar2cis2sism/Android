package engine.android.util.os;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Android6.0以上权限申请
 * 
 * @author Daimon
 * @since 6/6/2016
 */
@TargetApi(Build.VERSION_CODES.M)
public class PermissionUtil {

    public interface PermissionCallback {

        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    }

    private final Activity activity;

    private final PermissionCallback callback;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
        callback = activity instanceof PermissionCallback ? (PermissionCallback) activity : null;
    }

    /**
     * 请求权限
     */
    public void requestPermission(String... permissions) {
        requestPermission(0, permissions);
    }

    /**
     * 请求权限
     */
    public void requestPermission(int requestCode, String... permissions) {
        if (checkPermission(activity, permissions))
        {
            if (callback != null)
            {
                callback.onRequestPermissionsResult(requestCode, permissions, new int[0]);
            }

            return;
        }

        List<String> list = getNeedPermission(activity, permissions);
        activity.requestPermissions(list.toArray(new String[list.size()]), requestCode);
    }

    /**
     * 权限回调，需在Activity同名方法中调用
     * 
     * @return True:用户同意授权 False:用户拒绝授权
     */
    public boolean onRequestPermissionsResult(int[] grantResults) {
        return verifyPermission(activity, grantResults);
    }

    /**
     * 检查所有的权限是否都已授权
     */
    private static boolean checkPermission(Activity activity, String[] permissions) {
        if (isAndroidM())
        {
            for (String permission : permissions)
            {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isAndroidM() {
        return Build.VERSION.SDK_INT >= 23;
    }

    /**
     * 获取需要申请权限列表
     */
    private static List<String> getNeedPermission(Activity activity, String[] permissions) {
        ArrayList<String> needPermissionList = new ArrayList<String>();
        for (String permission : permissions)
        {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
                    || activity.shouldShowRequestPermissionRationale(permission))
            {
                needPermissionList.add(permission);
            }
        }

        return needPermissionList;
    }

    /**
     * 验证所有的权限是否都已授权
     */
    private static boolean verifyPermission(Activity activity, int[] grantResults) {
        for (int grantResult : grantResults)
        {
            if (grantResult != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * 提示用户开启权限的对话框
     */
    public void showTipDialog() {
        CharSequence appName = activity.getApplicationInfo().loadLabel(activity.getPackageManager());
        new AlertDialog.Builder(activity)
        .setTitle("权限申请")
        .setMessage(String.format("在设置-应用-%s-权限中开启对应权限，以正常使用应用功能", appName))
        .setPositiveButton("去设置", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSettingActivity();
            }
        })
        .setNegativeButton("取消", null)
        .show();
    }

    /**
     * 启动当前应用设置界面
     */
    public void startSettingActivity() {
        activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + activity.getPackageName())));
    }
}