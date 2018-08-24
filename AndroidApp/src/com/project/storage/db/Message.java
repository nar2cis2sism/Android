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
public class Message implements MessageColumns {
    
    @DAOPrimaryKey(column=_ID, autoincrement=true)
    private long _id;

    @DAOProperty(column=ID)
    public String id;                       // 消息ID，用于排重

    @DAOProperty(column=ACCOUNT)
    public String account;                  // 发送/接收方账号

    @DAOProperty(column=CONTENT)
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
    @DAOProperty(column=TYPE)
    public int type;                        // 消息类型
    
    /**
     * 0：二人会话
     * 1：群组消息
     * 2：系统消息
     * 3：公众平台
     */
    @DAOProperty(column=EVENT)
    public int event;                       // 消息事件

    @DAOProperty(column=CREATION_TIME)
    public long creationTime;               // 消息创建时间

    /******************************* 华丽丽的分割线 *******************************/
    
    @DAOProperty(column=IS_RECEIVED)
    public boolean isReceived;              // True:接收消息,False:发送消息

    /**
     * 0：消息发送中
     * 1：未发送成功
     * 2：消息已送达
     */
    @DAOProperty(column=SEND_STATUS)
    public int sendStatus;                  // 消息发送状态

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
    
    public protocol.socket.req.Message toProtocol() {
        protocol.socket.req.Message msg = new protocol.socket.req.Message();
        msg.id = id;
        msg.account = account;
        msg.content = content;
        msg.type = type;
        msg.event = event;
        return msg;
    }
    
    public Message fromProtocol(protocol.socket.req.Message msg) {
        id = msg.id;
        account = msg.account;
        content = msg.content;
        type = msg.type;
        event = msg.event;
        creationTime = msg.creationTime;
        return this;
    }
}