package engine.android.util.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import java.util.List;

/**
 * 我的Wifi管理器<br>
 * 需要声明权限
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission android:name="android.permission.WAKE_LOCK" />
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public class MyWifiManager {

    private final Context context;

    private final WifiManager wm;               // Wifi管理器

    private WifiLock lock;                      // Wifi锁

    private BroadcastReceiver scanReceiver;     // Wifi扫描接收器

    private BroadcastReceiver stateReceiver;    // Wifi状态接收器

    public MyWifiManager(Context context) {
        wm = (WifiManager) (this.context = context.getApplicationContext())
                .getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 获取当前连接的Wifi信息
     */
    public WifiInfo getWifiInfo() {
        return wm.getConnectionInfo();
    }

    /**
     * Wifi是否开启
     */
    public boolean isWifiEnabled() {
        return wm.isWifiEnabled();
    }

    /**
     * 开启或关闭Wifi
     * 
     * @param enabled
     */
    public void setWifiEnabled(boolean enabled) {
        wm.setWifiEnabled(enabled);
    }

    /**
     * 判断当前是否Wifi连接<br>
     */
    public static boolean isAccessible(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo(); // 获取当前网络连接信息
        if (info != null && info.getState() == NetworkInfo.State.CONNECTED
                && info.getType() == ConnectivityManager.TYPE_WIFI)
        {
            return true;
        }

        return false;
    }

    /**
     * 锁定Wifi（2分钟不用Wifi会进入睡眠，此时需要锁定以便一直使用）
     */
    public void lock() {
        if (lock == null)
        {
            lock = wm.createWifiLock(getClass().getSimpleName());
        }

        lock.acquire();
    }

    /**
     * 解锁（锁定之后一定要记得解锁以节省电量）
     */
    public void unlock() {
        if (lock != null && lock.isHeld())
        {
            lock.release();
        }
    }

    /**
     * 注册扫描接收器
     */
    public void registerScanReceiver(BroadcastReceiver receiver) {
        if (receiver != null && scanReceiver == null)
        {
            context.registerReceiver(scanReceiver = receiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    /**
     * 取消扫描接收
     */
    public void unregisterScanReceiver() {
        if (scanReceiver != null)
        {
            context.unregisterReceiver(scanReceiver);
            scanReceiver = null;
        }
    }

    /**
     * 注册状态接收器
     */
    public void registerStateReceiver(BroadcastReceiver receiver) {
        if (receiver != null && stateReceiver == null)
        {
            context.registerReceiver(stateReceiver = receiver,
                    new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }
    }

    /**
     * 取消状态接收
     */
    public void unregisterStateReceiver() {
        if (stateReceiver != null)
        {
            context.unregisterReceiver(stateReceiver);
            stateReceiver = null;
        }
    }

    /**
     * 扫描可用的Wifi网络（此方法立即返回，如果注册了扫描接收器，在扫描结束时会收到通知）
     * 
     * @return 如果扫描初始化失败则返回false
     */
    public boolean scan() {
        return wm.startScan();
    }

    /**
     * 获取已扫描到的Wifi网络列表
     */
    public List<ScanResult> getWifiList() {
        return wm.getScanResults();
    }

    /**
     * 获取Wifi状态
     */
    public int getWifiState() {
        return wm.getWifiState();
    }
}