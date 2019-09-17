package engine.android.framework.network.socket;

import engine.android.framework.network.socket.SocketResponse.Callback;

import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

/**
 * 推送消息接收器
 * 
 * @author Daimon
 * @since 3/15/2012
 */
public interface SocketPushReceiver {
    
    void receive(int cmd, ProtocolData data, Callback callback);
}