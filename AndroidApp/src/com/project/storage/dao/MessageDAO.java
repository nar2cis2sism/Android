package com.project.storage.dao;

import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.Message;

import java.util.List;

public class MessageDAO extends BaseDAO {
    
    public static void saveMessage(List<Message> list) {
        if (list != null && !list.isEmpty())
        {
            dao.save(list.toArray(new Message[list.size()]));
        }
    }
}