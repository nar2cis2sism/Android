package engine.android.framework.network.socket;

import engine.android.framework.network.ConnectionStatus;

import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

/**
 * Socket响应
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public interface SocketResponse {
    
    void response(int cmd, ProtocolData data, Callback callback);
    
    /**
     * 可以处理超时事件
     */
    interface SocketTimeout extends SocketResponse {
        
        /**
         * @return 设置超时时间
         */
        int getTimeout();
        
        void timeout(Callback callback);
    }
    
    interface Callback extends ConnectionStatus {

        void call(String action, int status, Object param);
    }
}