package com.project.network.action.socket;

import com.project.logic.MessageLogic;

import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.framework.network.socket.SocketResponse;
import protocol.socket.ack.OfflineMessageACK;
import protocol.socket.req.Message;
import protocol.socket.req.OfflineMessage;
import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

/**
 * 拉取离线消息
 * 
 * @author Daimon
 */
public class PullOfflineMessage implements SocketBuilder, SocketResponse {
    
    private final OfflineMessage message;
    
    public PullOfflineMessage(long timestamp) {
        message = new OfflineMessage();
        message.timestamp = timestamp;
    }

    @Override
    public ProtocolData buildData() {
        return message;
    }

    @Override
    public SocketResponse buildResponse() {
        return this;
    }

    @Override
    public boolean response(ProtocolData data, Callback callback) {
        if (data instanceof OfflineMessageACK)
        {
            receiveMessage((OfflineMessageACK) data);
            return true;
        }
        
        return false;
    }
    
    private void receiveMessage(OfflineMessageACK ack) {
        Message[] messages = ack.message;
        if (messages == null || messages.length == 0)
        {
            // 没有离线消息
            return;
        }
        
        com.project.storage.db.Message[] items = new com.project.storage.db.Message[messages.length];
        for (int i = 0; i < items.length; i++)
        {
            items[i] = new com.project.storage.db.Message().fromProtocol(messages[i]);
        }
        
        MessageLogic.receiveMessage(items);
    }
}