package engine.android.framework.network;

/**
 * 网络状态常量
 * 
 * @author Daimon
 * @version N
 * @since 3/15/2012
 */
public interface ConnectionStatus {

    public static final int SUCCESS         =  0;   // 联网成功
    public static final int FAIL            = -1;   // 联网失败

    public static final int ERROR           = -2;   // 联网异常
    public static final int TIMEOUT         = -3;   // 联网超时
    public static final int DISCONNECTED    = -4;   // 联网断开（无可用网络连接）
    
    /**
     * 网络拦截器
     */
    public interface ConnectionInterceptor extends ConnectionStatus {
        
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
}