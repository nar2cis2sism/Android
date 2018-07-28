package com.project.storage.db;

import android.text.TextUtils;

import com.project.app.bean.ServerUrl;
import com.project.app.config.ImageTransformer;
import com.project.storage.provider.ProviderContract.UserColumns;

import engine.android.core.util.CalendarFormat;
import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import engine.android.framework.app.image.ImageManager.ImageUrl;
import protocol.java.json.UserInfo;

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

    @DAOProperty(column=UserColumns.REGION)
    public String region;                   // 地区名称

    @DAOProperty(column=UserColumns.REGION_CODE)
    public String region_code;              // 区域编码

    @DAOProperty(column=UserColumns.SIGNATURE)
    public String signature;                // 签名

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
    
    public void fromProtocol(UserInfo item) {
        version = item.version;
        nickname = item.nickname;
        isFemale = item.gender == 1;
        birthday = item.birthday;
        parseRegion(item.region);
        signature = item.signature;
        isAuthenticated = item.authentication == 1;
        avatar_url = item.avatar_url;
    }
    
    private void parseRegion(String region) {
        if (!TextUtils.isEmpty(region))
        {
            String[] strs = region.split(":");
            region_code = strs[0];
            this.region = strs[1];
        }
    }
    
    public String toRegion() {
        return region_code + ":" + region;
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    public String getGenderText() {
        return isFemale ? "女" : "男";
    }
    
    public String getBirthdayText() {
        return CalendarFormat.formatDateByLocale(birthday, CalendarFormat.SHOW_YEAR);
    }
    
    public void setRegion(Region region) {
        this.region = region.name;
        region_code = region.code;
    }
    
    public String getAuthenticationText() {
        return isAuthenticated ? "已认证" : "未认证";
    }
    
    public ImageUrl getAvatarUrl() {
        if (avatar_ver == 0)
        {
            return null;
        }
        
        return new ImageUrl(ImageTransformer.TYPE_AVATAR, ServerUrl.getDownloadUrl(avatar_url), String.valueOf(avatar_ver));
    }
}