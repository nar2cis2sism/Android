package engine.android.framework.network;

/**
 * 网络状态常量
 * 
 * @author Daimon
 */
public interface ConnectionStatus {

    public static final int SUCCESS         =  0;   // 联网成功
    public static final int FAIL            = -1;   // 联网失败

    public static final int ERROR           = -2;   // 联网异常
    public static final int TIMEOUT         = -3;   // 联网超时
    public static final int DISCONNECTED    = -4;   // 联网断开（无可用网络连接）
}