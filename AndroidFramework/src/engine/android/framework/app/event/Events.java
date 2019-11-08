package engine.android.framework.app.event;

import static engine.android.framework.network.ConnectionStatus.FAIL;
import static engine.android.framework.network.ConnectionStatus.SUCCESS;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;

/**
 * 全局事件
 * 
 * @author Daimon
 */
public class Events {

    /******************************* 网络切换 *******************************/
    public static final String CONNECTIVITY_CHANGE = "CONNECTIVITY_CHANGE";
    
    /**
     * @param noNetwork True:网络不可用
     */
    public static void notifyConnectivityChange(boolean noNetwork) {
        EventBus.getDefault().post(new Event(CONNECTIVITY_CHANGE, 0, noNetwork));
    }

    /******************************* socket状态通知 *******************************/
    public static final String SOCKET_STATUS = "socket_status";
    
    /**
     * @param error Null:建立连接 or:断开连接并报告异常
     */
    public static void notifySocketStatus(Exception error) {
        EventBus.getDefault().post(new Event(SOCKET_STATUS, 0, error));
    }

    /******************************* 支付回调 *******************************/
    public static final String PAY_CALLBACK = "PAY_CALLBACK";
    public static final Object PAY_WEIXIN = "WEIXIN"; // 微信支付
    public static final Object PAY_QQ = "QQ"; // QQ钱包支付

    /**
     * @param type 支付方式
     * @param success True:支付成功 False:取消支付
     */
    public static void notifyPayCallback(Object type, boolean success) {
        EventBus.getDefault().post(new Event(PAY_CALLBACK, success ? SUCCESS : FAIL, type));
    }
}