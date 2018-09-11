package com.project.logic;

import android.text.TextUtils;

import com.project.MyNotificationManager;
import com.project.app.event.Events;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Message;
import com.project.ui.MainActivity;

import java.util.List;

public class MessageLogic {
    
    public static String currentConversation;               // 当前会话对象
    
    /**
     * 接收消息
     */
    public static void receiveMessage(Message... msgs) {
        int count = msgs.length;
        // 过滤已读消息
        if (!TextUtils.isEmpty(currentConversation))
        {
            for (Message msg : msgs)
            {
                if (currentConversation.equals(msg.account))
                {
                    msg.isRead = true;
                    count--;
                }
            }
        }

        MessageDAO.receiveMessage(msgs);
        if (count > 0)
        {
            // 还有未读消息
            messageUpdated();
        }
    }
    
    /**
     * 设置消息已读
     * 
     * @param account 消息来源
     */
    public static void setMessageRead(String account) {
        if (MessageDAO.setMessageRead(account))
        {
            messageUpdated();
        }
    }
    
    /**
     * 消息有更新
     */
    private static void messageUpdated() {
        List<Message> unreadList = MessageDAO.getUnreadMessageList();
        int unreadCount = unreadList.size();
        boolean hasUnreadMessage = unreadCount > 0;
        // 更新通知栏
        if (hasUnreadMessage)
        {
            MyNotificationManager.getInstance().notifyMessage(unreadList.get(0), unreadCount);
        }
        else
        {
            MyNotificationManager.getInstance().cancelMessage();
        }
        // 更新主界面消息标签
        Events.notifyMainTabBadge(MainActivity.TAB_TAG_MESSAGE, hasUnreadMessage);
    }
}