package com.project.storage.dao;

import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.Friend;
import com.project.storage.provider.ProviderContract.FriendColumns;

import java.util.List;

public class FriendDAO extends BaseDAO implements FriendColumns {
    
    /**
     * 根据账号获取好友信息
     */
    public static Friend getFriendByAccount(String account) {
        return findItemByProperty(Friend.class, ACCOUNT, account);
    }
    
    /**
     * 获取好友列表
     */
    public static List<Friend> getFriendList() {
        return dao.find(Friend.class)
            .orderBy(SORTING)
            .getAll();
    }
}