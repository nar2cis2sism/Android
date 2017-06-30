package engine.android.framework.network.socket.util;

import engine.android.framework.network.socket.util.SocketResponse.Callback;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;

/**
 * 推送消息接收器
 * 
 * @author Daimon
 * @version N
 * @since 3/15/2012
 */
public interface SocketPushReceiver {
    
    void receive(int cmd, ProtocolData data, Callback callback);
}