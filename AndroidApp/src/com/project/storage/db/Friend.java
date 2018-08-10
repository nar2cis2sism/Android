package com.project.storage.db;

import android.text.TextUtils;

import com.project.storage.provider.ProviderContract.FriendColumns;
import com.project.util.MyValidator;

import engine.android.dao.DAOTemplate;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;

import net.sourceforge.pinyin4j.lite.PinyinHelper;

import protocol.http.FriendSync;

/**
 * 好友信息
 * 
 * @author Daimon
 */
//@DAOTable(name=com.project.storage.provider.ProviderContract.Friend.TABLE)
public class Friend {

    
    @DAOPrimaryKey(column=FriendColumns.ACCOUNT)
    public String account;                      // 账号

    @DAOProperty(column=FriendColumns.NICK_NAME)
    public String nickname;                     // 昵称

    @DAOProperty(column=FriendColumns.SIGNATURE)
    public String signature;                    // 签名

//    @DAOProperty(column=FriendColumns.VERSION)
//    public long version;                        // 好友信息版本号

    @DAOProperty(column=FriendColumns.AVATAR_URL)
    public String avatar_url;                   // 头像下载地址

//    @DAOProperty(column=FriendColumns.AVATAR_VER)
//    public long avatar_ver;                     // 头像版本号

    @DAOProperty(column=FriendColumns.DISPLAY_NAME)
    public String displayName;                  // 显示名称

    @DAOProperty(column=FriendColumns.PINYIN)
    public String pinyin;                       // 汉字全拼（小写）
    
    /************************* 排序字段 *************************/

    public static final int SORT_ENGLISH    = 0;
    public static final int SORT_OTHER      = 1;

    @DAOProperty(column=FriendColumns.SORTING)
    public String sorting;

    /**
     * Mandatory empty constructor for the {@link DAOTemplate}
     */
    public Friend() {}

    /******************************* 华丽丽的分割线 *******************************/
    
    public Friend fromProtocol(FriendSync item) {
        account = item.account;
        nickname = item.nickname;
        signature = item.signature;
        avatar_url = item.avatar_url;
        displayName = getDisplayName();
        pinyin = PinyinHelper.getInstance().getPinyins(displayName, "").toLowerCase();
        sorting = sort(pinyin);
        return this;
    }
    
    /**
     * 获取显示名称
     */
    private String getDisplayName() {
        String displayName = nickname;
        if (!TextUtils.isEmpty(displayName) && displayName.trim().length() > 0)
        {
            return displayName;
        }
        
        return account;
    }
    
    /**
     * 根据拼音排序
     */
    private static String sort(String pinyin) {
        String firstLetter = pinyin.substring(0, 1);
        int sort;
        if (MyValidator.validate(firstLetter, MyValidator.ENGLISH))
        {
            sort = SORT_ENGLISH;
        }
        else
        {
            // 首字母为英文外其他字符排在后面
            sort = SORT_OTHER;
        }
    
        return sort + pinyin;
    }

    /******************************* 华丽丽的分割线 *******************************/

    /**
     * 获取排序分类
     * 
     * @return {@link #SORT_ENGLISH}, {@link #SORT_OTHER}
     */
    public int getSort() {
        return Integer.parseInt(sorting.substring(0, 1));
    }
}