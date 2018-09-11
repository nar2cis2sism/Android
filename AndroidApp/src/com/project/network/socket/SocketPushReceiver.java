package com.project.network.socket;

import com.project.logic.MessageLogic;

import engine.android.framework.network.socket.SocketResponse.Callback;
import protocol.socket.req.Message;
import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

public class SocketPushReceiver implements engine.android.framework.network.socket.SocketPushReceiver {

    @Override
    public void receive(int cmd, ProtocolData data, Callback callback) {
        if (data instanceof Message)
        {
            receiveMessage((Message) data);
        }
    }
    
    private void receiveMessage(Message msg) {
        MessageLogic.receiveMessage(new com.project.storage.db.Message().fromProtocol(msg));
    }
}