package engine.android.library.lbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

public class LocationUtil {

    private final Activity activity;

    public LocationUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     */
    public boolean isGpsEnabled() {
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    /**
     * 提示用户开启Gps的对话框
     */
    public void showTipDialog() {
        CharSequence appName = activity.getApplicationInfo().loadLabel(activity.getPackageManager());
        new AlertDialog.Builder(activity)
                .setTitle("温馨提示")
                .setMessage("系统检测到未开启GPS定位服务")
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
     * 打开系统设置中的我的位置界面,手动开启或关闭GPS
     */
    public void startSettingActivity() {
        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
