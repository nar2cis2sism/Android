package engine.android.util.listener;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * 我的手机状态监听器<br>
 * 需要声明权限
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public class MyPhoneStateListener extends PhoneStateListener {

    private final Context context;

    private final TelephonyManager tm;                          // 电话管理器

    private final ConnectivityManager cm;                       // 网络连接管理器

    private boolean isProxy;                                    // 是否使用代理

    public MyPhoneStateListener(Context context) {
        this.context = context.getApplicationContext();
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * 注册联网监听器
     */
    public void register() {
        tm.listen(this, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    /**
     * 程序退出时必须注销
     */
    public void unregister() {
        tm.listen(this, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * 获取APN数据库指针
     */
    public Cursor getAPN() {
        Uri uri = Uri.parse("content://telephony/carriers/preferapn");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return cursor;
    }

    /**
     * 获取APN数据库指针（双卡手机）
     */
    public Cursor getAPN2() {
        Uri uri = Uri.parse("content://telephony/carriers/preferapn2");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return cursor;
    }

    /**
     * 判断是否使用代理
     */
    public boolean isProxy() {
        return isProxy;
    }

    @Override
    public void onDataConnectionStateChanged(int state) {
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                // 网络无连接（可能使用WIFI）
            case TelephonyManager.DATA_CONNECTED:
                // 网络已连接
                setProxy();
                break;
        }
    }

    /**
     * 设置代理
     */
    @SuppressWarnings("deprecation")
    protected void setProxy() {
        NetworkInfo info = cm.getActiveNetworkInfo(); // 获取当前网络连接信息
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI)
        {
            // 使用WIFI网络
            isProxy = false;
        }
        else
        {
            if (Proxy.getHost(context) != null)
            {
                // 有代理网关
                isProxy = true;
            }
            else
            {
                isProxy = false;
            }
        }
    }
}