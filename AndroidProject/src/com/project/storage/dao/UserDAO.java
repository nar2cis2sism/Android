package com.project.storage.dao;

import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

public class UserDAO extends BaseDAO {
    
    /**
     * 根据用户ID获取用户信息
     */
    public static User getUserById(long uid) {
        return findItemByProperty(User.class, UserColumns.USER_ID, uid);
    }
}