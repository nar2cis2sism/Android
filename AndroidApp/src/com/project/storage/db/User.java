package com.project.storage.db;

import engine.android.core.util.CalendarFormat;
import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import engine.android.framework.ui.fragment.region.Region;
import engine.android.util.image.AsyncImageLoader.ImageUrl;

import android.text.TextUtils;

import com.daimon.yueba.R;
import com.project.app.MyContext;
import com.project.app.bean.ServerUrl;
import com.project.app.config.ImageTransformer;
import com.project.storage.provider.ProviderContract.UserColumns;

import protocol.http.UserData;

/**
 * 用户资料
 */
@DAOTable(name=com.project.storage.provider.ProviderContract.User.TABLE)
public class User implements UserColumns {
    
    @DAOPrimaryKey(column=USERNAME)
    public String username;                     // 用户名

    @DAOProperty(column=NICKNAME)
    public String nickname;                     // 用户昵称

    /**
     * 0：男
     * 1：女
     */
    @DAOProperty(column=GENDER)
    public int gender;                          // 性别

    @DAOProperty(column=BIRTHDAY)
    public long birthday;                       // 出生日期

    @DAOProperty(column=REGION)
    public String region;                       // 地区名称

    @DAOProperty(column=REGION_CODE)
    public String region_code;                  // 区域编码

    @DAOProperty(column=SIGNATURE)
    public String signature;                    // 签名

    @DAOProperty(column=AUTHENTICATION)
    public boolean isAuthenticated;             // 实名认证

    @DAOProperty(column=VERSION)
    public int version;                         // 用户信息版本号

    @DAOProperty(column=AVATAR_URL)
    public String avatar_url;                   // 头像下载地址

    @DAOProperty(column=AVATAR_VER)
    public int avatar_ver;                      // 头像版本号

    @DAOProperty(column=FRIEND_LIST_TIMESTAMP)
    public long friend_list_timestamp;          // 好友列表同步时间戳

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public User() {}

    /******************************* 华丽丽的分割线 *******************************/
    
    public User fromProtocol(UserData data) {
        version = data.version;
        nickname = data.nickname;
        gender = data.gender;
        birthday = data.birthday;
        parseRegion(data.region);
        signature = data.signature;
        isAuthenticated = data.authentication == 1;
        avatar_url = data.avatar_url;
        return this;
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
        return MyContext.getResources().getString(gender == 0
                ? R.string.gender_male : R.string.gender_female);
    }
    
    public String getBirthdayText() {
        return CalendarFormat.formatDateByLocale(birthday, CalendarFormat.SHOW_YEAR);
    }
    
    public void setRegion(Region region) {
        this.region = region.name;
        region_code = region.code;
    }
    
    public String getAuthenticationText() {
        return MyContext.getResources().getString(isAuthenticated
             ? R.string.authentication_yes : R.string.authentication_no);
    }
    
    public ImageUrl getAvatarUrl() {
        if (avatar_ver == 0)
        {
            return null;
        }
        
        return new ImageUrl(ImageTransformer.TYPE_AVATAR,
                ServerUrl.getDownloadUrl(avatar_url), String.valueOf(avatar_ver));
    }
}