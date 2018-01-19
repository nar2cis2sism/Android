package engine.android.framework.network.socket;

import engine.android.framework.network.ConnectionStatus;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;

public interface SocketResponse {
    
    /**
     * @return True表示不再继续接收后续事件
     */
    boolean response(ProtocolData data, Callback callback);
    
    interface Callback extends ConnectionStatus {

        void call(String action, int status, Object param);
    }
}