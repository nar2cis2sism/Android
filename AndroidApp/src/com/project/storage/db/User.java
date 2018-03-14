package com.project.storage.db;

import com.project.storage.provider.ProviderContract.UserColumns;

import protocol.java.json.UserInfo;

import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;

/**
 * 用户资料
 */
@DAOTable(name=com.project.storage.provider.ProviderContract.User.TABLE)
public class User {
    
    @DAOPrimaryKey(column=UserColumns.USERNAME)
    public String username;                 // 用户名

    @DAOProperty(column=UserColumns.NICKNAME)
    public String nickname;                 // 用户昵称
    
    @DAOProperty(column=UserColumns.IS_FEMALE)
    public boolean isFemale;                // 性别[True:女,False:男]

    @DAOProperty(column=UserColumns.BIRTHDAY)
    public long birthday;                   // 出生日期

    @DAOProperty(column=UserColumns.CITY)
    public String city;                     // 常驻城市

    @DAOProperty(column=UserColumns.SIGNATURE)
    public String signature;                // 签名

    @DAOProperty(column=UserColumns.PROFILE)
    public String profile;                  // 个人简介

    @DAOProperty(column=UserColumns.IS_AUTHENTICATED)
    public boolean isAuthenticated;         // 实名认证

    @DAOProperty(column=UserColumns.VERSION)
    public long version;                    // 用户信息版本号

    @DAOProperty(column=UserColumns.AVATAR_URL)
    public String avatar_url;               // 头像下载地址

    @DAOProperty(column=UserColumns.AVATAR_VER)
    public long avatar_ver;                 // 头像版本号

    @DAOProperty(column=UserColumns.FRIEND_LIST_TIMESTAMP)
    public long friend_list_timestamp;      // 好友列表同步时间戳

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public User() {}

    /******************************* 华丽丽的分割线 *******************************/
    
//    /**
//     * 包含“用户信息版本”和“头像版本”，用“:”分隔
//     */
//    public String getVersion() {
//        return version + ":" + avatar_ver;
//    }
//    
    public void fromProtocol(UserInfo item) {
        version = item.version;
        nickname = item.nickname;
        isFemale = item.gender == 1;
        birthday = item.birthday;
        city = item.city;
        signature = item.signature;
        profile = item.profile;
        isAuthenticated = item.authentication == 1;
        avatar_url = item.avatar_url;
    }
}