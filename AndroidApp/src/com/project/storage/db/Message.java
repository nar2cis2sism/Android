package com.project.storage.db;

import com.project.app.MySession;
import com.project.storage.provider.ProviderContract.MessageColumns;

import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import protocol.java.stream.req.Message.MessageBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 收发消息
 */
//@DAOTable(name=com.project.storage.provider.ProviderContract.Message.TABLE)
public class Message {
    
    @DAOPrimaryKey(column=MessageColumns._ID, autoincrement=true)
    private long id;                        // 消息ID
    
    @DAOProperty(column=MessageColumns.IS_RECEIVED)
    public boolean isReceived;              // True:接收消息,False:发送消息

    @DAOProperty(column=MessageColumns.ACCOUNT)
    public String account;                  // 对方账号

    @DAOProperty(column=MessageColumns.CONTENT)
    public String content;                  // 消息内容
    
    /**
     * 0：文本
     * 1：单图
     * 2：多图
     * 3：音频
     * 4：视频
     * 5：位置
     * 6：名片
     */
    @DAOProperty(column=MessageColumns.TYPE)
    public int type;                        // 消息类型
    
    /**
     * 0：二人会话
     * 1：群组消息
     * 2：系统消息
     * 3：公众平台
     */
    @DAOProperty(column=MessageColumns.EVENT)
    public int event;                       // 消息事件

    @DAOProperty(column=MessageColumns.CREATION_TIME)
    public long creationTime;               // 消息创建时间

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public Message() {}

    /******************************* 华丽丽的分割线 *******************************/
    
    public protocol.java.stream.req.Message toProtocol() {
        MessageBody body = new MessageBody();
        body.content = content;
        body.type = type;
        body.event = event;
        
        protocol.java.stream.req.Message item = new protocol.java.stream.req.Message();
        item.from = MySession.getUser().username;
        item.to = account;
        item.body = new MessageBody[] { body };
        
        return item;
    }
    
    public static List<Message> fromProtocol(protocol.java.stream.req.Message message) {
        MessageBody[] body = message.body;
        if (body != null && body.length > 0)
        {
            List<Message> list = new ArrayList<Message>(body.length);
            for (MessageBody msg : body)
            {
                Message item = new Message();
                item.isReceived = true;
                item.account = message.from;
                item.content = msg.content;
                item.type = msg.type;
                item.event = msg.event;
                item.creationTime = msg.creationTime;
                list.add(item);
            }
            
            return list;
        }
        
        return null;
    }
}