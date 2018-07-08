package com.project.storage.db;

import com.project.storage.provider.ProviderContract.MessageColumns;

import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;

/**
 * 收发消息
 */
@DAOTable(name=com.project.storage.provider.ProviderContract.Message.TABLE)
public class Message {
    
    @DAOPrimaryKey(column=MessageColumns._ID, autoincrement=true)
    private long _id;

    @DAOProperty(column=MessageColumns.ID)
    public String id;                       // 消息ID，用于排重

    @DAOProperty(column=MessageColumns.ACCOUNT)
    public String account;                  // 发送/接收方账号

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
    
    @DAOProperty(column=MessageColumns.IS_RECEIVED)
    public boolean isReceived;              // True:接收消息,False:发送消息
    
    @DAOProperty(column=MessageColumns.IS_SEND_OUT)
    public boolean isSendOut;               // 消息是否发送成功

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public Message() {}
    
    /**
     * 生成唯一消息ID
     */
    public void generateId() {
        id = String.valueOf(_id);
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    public protocol.java.stream.req.Message toProtocol() {
        protocol.java.stream.req.Message msg = new protocol.java.stream.req.Message();
        msg.id = id;
        msg.account = account;
        msg.content = content;
        msg.type = type;
        msg.event = event;
        return msg;
    }
    
    public Message fromProtocol(protocol.java.stream.req.Message msg) {
        id = msg.id;
        account = msg.account;
        content = msg.content;
        type = msg.type;
        event = msg.event;
        creationTime = msg.creationTime;
        return this;
    }
}