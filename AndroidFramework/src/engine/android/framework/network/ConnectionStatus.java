package engine.android.framework.network;

import engine.android.framework.app.event.Events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * 网络状态常量
 * 
 * @author Daimon
 * @since 3/15/2012
 */
public interface ConnectionStatus {

    int SUCCESS         =  0;   // 联网成功
    int FAIL            = -1;   // 联网失败

    int ERROR           = -2;   // 联网异常
    int TIMEOUT         = -3;   // 联网超时
    int DISCONNECTED    = -4;   // 联网断开（无可用网络连接）
    
    /**
     * 网络拦截器
     */
    interface ConnectionInterceptor extends ConnectionStatus {
        
        /**
         * 网络拦截
         * 
         * @param action 信令名称
         * @param status 网络状态
         * @param param  通讯参数
         * 
         * @return 是否拦截
         */
        boolean intercept(String action, int status, Object param);
    }
    
    /**
     * 网络状态监听器<br>
     * 需注册到Manifest才能使用
     */
    public static class ConnectionStatusReceiver extends BroadcastReceiver {
        
        public static final String ACTION = ConnectivityManager.CONNECTIVITY_ACTION;

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean noNetwork = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            Events.notifyConnectivityChange(noNetwork);
        }
    }
}