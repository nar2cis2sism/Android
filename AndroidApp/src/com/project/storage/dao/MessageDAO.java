package com.project.storage.dao;

import com.project.app.MySession;
import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.Message;
import com.project.storage.provider.ProviderContract.MessageColumns;

import java.util.List;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOExpression;
import engine.android.dao.DAOTemplate.DAOTransaction;

public class MessageDAO extends BaseDAO implements MessageColumns {
    
    /**
     * 发送一条消息
     * 
     * @param receiver 接收方账号
     */
    public static Message sendMessage(String receiver, String message) {
        Message msg = new Message();
        msg.account = receiver;
        msg.content = message;
        msg.creationTime = System.currentTimeMillis();
        dao.save(msg);
        
        msg = dao.find(Message.class)
            .where(DAOExpression.create(IS_RECEIVED).eq(msg.isReceived)
            .and(ACCOUNT).eq(msg.account))
            .orderDesc(_ID)
            .get();
        msg.generateId();
        dao.update(msg, ID);
        
        return msg;
    }
    
    public static void receiveMessage(Message msg) {
        msg.isReceived = true;
        if (dao.find(Message.class)
            .where(DAOExpression.create(IS_RECEIVED).eq(msg.isReceived)
            .and(ACCOUNT).eq(msg.account)
            .and(ID).eq(msg.id))
            .get()
            == null)
        {
            dao.save(msg);
        }
    }
    
    public static void receiveMessage(final Message[] msgs) {
        dao.execute(new DAOTransaction() {
            
            @Override
            public boolean execute(DAOTemplate dao) throws Exception {
                for (Message msg : msgs)
                {
                    receiveMessage(msg);
                }
                
                return true;
            }
        });
    }
    
    /**
     * 获取消息列表
     * 
     * @param account 消息来源
     */
    public static List<Message> getMessageList(String account) {
        return dao.find(Message.class).where(DAOExpression.create(ACCOUNT).eq(account)).orderBy(CREATION_TIME).getAll();
    }
}