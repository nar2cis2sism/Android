package com.project.network.action.socket;

import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;

import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.framework.network.socket.SocketResponse;
import engine.android.framework.network.socket.SocketResponse.SocketTimeout;
import protocol.socket.ack.MessageACK;
import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

/**
 * 发送消息
 * 
 * @author Daimon
 */
public class SendMessage implements SocketBuilder, SocketTimeout {
    
    private final Message message;
    
    public SendMessage(Message message) {
        this.message = message;
    }

    @Override
    public ProtocolData buildData() {
        return message.toProtocol();
    }

    @Override
    public SocketResponse buildResponse() {
        return this;
    }

    @Override
    public boolean response(ProtocolData data, Callback callback) {
        if (data instanceof MessageACK)
        {
            MessageDAO.sendoutMessage(message, true);
            return true;
        }
        
        return false;
    }

    @Override
    public int getTimeout() {
        return 30000;
    }

    @Override
    public void timeout(Callback callback) {
        MessageDAO.sendoutMessage(message, false);
    }
}