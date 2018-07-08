package com.project.network.socket;

import com.project.storage.dao.MessageDAO;

import engine.android.framework.network.socket.SocketResponse.Callback;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;
import protocol.java.stream.OfflineMessage;
import protocol.java.stream.req.Message;

public class SocketPushReceiver implements engine.android.framework.network.socket.SocketPushReceiver {

    @Override
    public void receive(int cmd, ProtocolData data, Callback callback) {
        if (data instanceof Message)
        {
            receiveMessage((Message) data);
        }
        else if (data instanceof OfflineMessage)
        {
            receiveMessage((OfflineMessage) data);
        }
    }
    
    private void receiveMessage(Message msg) {
        MessageDAO.receiveMessage(new com.project.storage.db.Message().fromProtocol(msg));
    }
    
    private void receiveMessage(OfflineMessage msg) {
        Message[] messages = msg.message;
        com.project.storage.db.Message[] items = new com.project.storage.db.Message[messages.length];
        for (int i = 0; i < items.length; i++)
        {
            items[i] = new com.project.storage.db.Message().fromProtocol(messages[i]);
        }
        
        MessageDAO.receiveMessage(items);
    }
}