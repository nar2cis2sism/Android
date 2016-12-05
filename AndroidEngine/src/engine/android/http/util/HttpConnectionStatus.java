package engine.android.http.util;

/**
 * Http联网状态
 * 
 * @author Daimon
 * @version N
 * @since 3/15/2012
 */
public interface HttpConnectionStatus {

    public static final int SUCCESS         = -1;   // 联网成功
    public static final int FAIL            = -2;   // 联网失败

    public static final int ERROR           = -3;   // 联网异常
    public static final int TIMEOUT         = -4;   // 联网超时
    public static final int DISCONNECTED    = -5;   // 联网断开（无可用网络连接）
}