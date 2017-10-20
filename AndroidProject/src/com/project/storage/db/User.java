package com.project.storage.db;

import com.project.storage.provider.ProviderContract.UserColumns;

import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;

/**
 * 用户资料
 */
@DAOTable(name=com.project.storage.provider.ProviderContract.User.TABLE)
public class User {
    
    @DAOPrimaryKey(column=UserColumns.USER_ID)
    private long uid;                       // 用户唯一标识

    @DAOProperty(column=UserColumns.NICK_NAME)
    public String nickname;                 // 用户昵称
    
    @DAOProperty(column=UserColumns.IS_FEMALE)
    public boolean isFemale;                // 性别[true:女,false:男]

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
    public String version;                  // 用户信息的版本号

    @DAOProperty(column=UserColumns.AVATAR_URL)
    public String avatar_url;               // 头像下载地址

    @DAOProperty(column=UserColumns.AVATAR_VER)
    public String avatar_ver;               // 头像版本号

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public User() {}

    /******************************* 华丽丽的分割线 *******************************/
    
    public User(long uid) {
        this.uid = uid;
    }
    
    public final long getUid() {
        return uid;
    }
    
    /**
     * 包含“用户信息版本”和“头像版本”，用“:”分隔
     */
    public String getVersion() {
        return version + ":" + avatar_ver;
    }
}