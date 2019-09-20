package com.project.network.action.socket;

import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.framework.network.socket.SocketResponse;
import engine.android.framework.network.socket.SocketResponse.SocketTimeout;

import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;

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
    public void response(int cmd, ProtocolData data, Callback callback) {
        MessageDAO.sendoutMessage(message, true);
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