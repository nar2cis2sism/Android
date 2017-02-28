package engine.android.framework.network;

/**
 * 网络拦截器
 * 
 * @author Daimon
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