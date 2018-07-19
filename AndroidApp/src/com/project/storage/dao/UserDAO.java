package com.project.storage.dao;

import com.project.app.MySession;
import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

public class UserDAO extends BaseDAO implements UserColumns {
    
    /**
     * 根据用户名获取用户信息
     */
    public static User getUserByUsername(String username) {
        return findItemByProperty(User.class, USERNAME, username);
    }
    
    /**
     * 更新用户头像版本号
     */
    public static void updateAvatarVersion(long version) {
        User user = MySession.getUser();
        user.avatar_ver = version;
        dao.update(user, AVATAR_VER);
    }
}