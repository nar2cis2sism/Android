package com.project.network.socket;

import com.project.storage.dao.MessageDAO;

import engine.android.framework.network.socket.SocketResponse.Callback;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;
import protocol.java.stream.req.Message;

public class SocketPushReceiver implements engine.android.framework.network.socket.SocketPushReceiver {

    @Override
    public void receive(int cmd, ProtocolData data, Callback callback) {
        if (data instanceof Message)
        {
            receiveMessage((Message) data);
        }
    }
    
    private void receiveMessage(Message message) {
        MessageDAO.saveMessage(com.project.storage.db.Message.fromProtocol(message));
    }
}