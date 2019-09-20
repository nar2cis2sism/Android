package com.project.app.bean;

import engine.android.util.image.AsyncImageLoader.ImageUrl;

import com.project.storage.db.Friend;

public class FriendListItem {

    public static final String[] CATEGORY = {"搜", "A", "B", "C", "D", "E", "F", "G", "H", 
                                             "I", "J", "K", "L", "M", "N", "O", "P", "Q", 
                                             "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    public final Friend friend;
    
    public final String category;               // 分类
    public final ImageUrl avatarUrl;            // 头像地址
    
    public FriendListItem(Friend friend) {
        category = getCategory(this.friend = friend);
        avatarUrl = friend.getAvatarUrl();
    }

    private static String getCategory(Friend friend) {
        if (friend.getSort() == Friend.SORT_OTHER)
        {
            // 英文外其他字符均归类到'#'下面
            return CATEGORY[CATEGORY.length - 1];
        }
        
        return friend.pinyin.substring(0, 1).toUpperCase();
    }
}