package com.project.app.bean;

import com.project.storage.db.Friend;

public class FriendListItem {

    public static final String[] CATEGORY = {"搜", "A", "B", "C", "D", "E", "F", "G", "H", 
                                             "I", "J", "K", "L", "M", "N", "O", "P", "Q", 
                                             "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    private final Friend friend;
    
    public final String category;           // 分类
//    public final ImageUrl avatarUrl;            // 头像地址
    
    public FriendListItem(Friend friend) {
        category = getCategory(this.friend = friend);
//        avatarUrl = new ImageUrl(0, friend.avatar_url, String.valueOf(friend.avatar_ver));
    }

    private String getCategory(Friend friend) {
        if (friend.getSort() == Friend.SORT_OTHER)
        {
            // 英文外其他字符均归类到'#'下面
            return CATEGORY[CATEGORY.length - 1];
        }
        
        return friend.pinyin.substring(0, 1).toUpperCase();
    }
    
    public String getName() {
        return friend.displayName;
    }
    
    public String getSignature() {
        return friend.signature;
    }
    
    public String getPinyin() {
        return friend.pinyin;
    }
}