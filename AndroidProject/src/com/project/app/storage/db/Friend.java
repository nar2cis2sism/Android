package com.project.app.storage.db;

import android.text.TextUtils;

import com.project.app.storage.provider.ProviderContract.FriendColumns;
import com.project.util.MyValidator;

import net.sourceforge.pinyin4j.lite.PinyinHelper;

import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import protocol.java.json.FriendInfo;

/**
 * 好友表
 * 
 * @author Daimon
 */
@DAOTable(name = com.project.app.storage.provider.ProviderContract.Friend.TABLE)
public class Friend {

    @DAOPrimaryKey(column = FriendColumns.USER_ID)
    private long userId;                        // 用户存储在服务器上的ID，永远是唯一的

    @DAOProperty(column = FriendColumns.REMARK)
    public String remark;                       // 备注

    @DAOProperty(column = FriendColumns.NICK_NAME)
    public String nickName;                     // 昵称

    @DAOProperty(column = FriendColumns.SIGNATURE)
    public String signature;                    // 签名

    @DAOProperty(column = FriendColumns.AVATAR_URL)
    public String avatarUrl;                    // 头像下载地址

    @DAOProperty(column = FriendColumns.VERSION)
    public String version;                      // 好友信息版本号

    @DAOProperty(column = FriendColumns.DISPLAY_NAME)
    public String displayName;                  // 显示名称
    
    /************************* 排序字段 *************************/

    @DAOProperty(column = FriendColumns.PINYIN)
    public String pinyin;                       // 拼音（大写）

    @DAOProperty(column = FriendColumns.SORT_ORDER)
    public String sortOrder;                    // 分类排序

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public Friend() {}
    
    public Friend(FriendInfo info) {
        userId = info.friend_id;
        remark = info.remark;
        nickName = info.nickname;
        signature = info.signature;
        avatarUrl = info.avatar_url;
        version = info.friend_info_ver;
        displayName = getDisplayName();
        pinyin = PinyinHelper.getInstance().getPinyins(displayName, "").toUpperCase();
        sortOrder = sort(pinyin);
    }
    
    public final long getUserId() {
        return userId;
    }
    
    /**
     * 获取显示名称
     */
    private String getDisplayName() {
        // 优先显示备注信息
        String displayName = remark;
        if (!TextUtils.isEmpty(displayName) && displayName.trim().length() > 0)
        {
            return displayName;
        }
        // 昵称其次
        displayName = nickName;
        if (!TextUtils.isEmpty(displayName) && displayName.trim().length() > 0)
        {
            return displayName;
        }
        // 没有名称显示用户ID
        return String.valueOf(userId);
    }
    
    /**
     * 根据拼音排序
     */
    private static String sort(String pinyin) {
        String firstLetter = pinyin.substring(0, 1);
        if (MyValidator.validate(firstLetter, MyValidator.ENGLISH))
        {
            pinyin = "0" + pinyin;
        }
        else
        {
            // 首字母为英文外其他字符排在后面
            pinyin = "1" + pinyin;
        }
        
        return pinyin;
    }
}