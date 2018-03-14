package com.project.storage.dao;

import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

public class UserDAO extends BaseDAO {
    
    /**
     * 根据用户名获取用户信息
     */
    public static User getUserByUsername(String username) {
        return findItemByProperty(User.class, UserColumns.USERNAME, username);
    }
}