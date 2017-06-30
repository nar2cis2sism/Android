package engine.android.framework.network.socket.util;

import engine.android.framework.network.ConnectionStatus;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;

public interface SocketResponse {
    
    /**
     * @return True表示不再继续接收后续事件
     */
    boolean response(ProtocolData data, Callback callback);
    
    public interface Callback extends ConnectionStatus {

        void call(String action, int status, Object param);
    }
}