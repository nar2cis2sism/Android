package com.project.network.action.socket;

import com.project.storage.MyDAOManager;
import com.project.storage.db.Message;
import com.project.storage.provider.ProviderContract.MessageColumns;

import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.framework.network.socket.SocketResponse;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;
import protocol.java.stream.ack.MessageACK;

/**
 * 发送消息
 * 
 * @author Daimon
 */
public class SendMessage implements SocketBuilder, SocketResponse {
    
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
            message.isSendOut = true;
            MyDAOManager.getDAO().update(message, MessageColumns.IS_SEND_OUT);
            
            return true;
        }
        
        return false;
    }
}