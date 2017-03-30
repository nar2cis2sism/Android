package com.project.app.bean;

import com.project.storage.db.Friend;

public class FriendListItem {

    public static final String[] CATEGORY = {"#"};

    public final Friend friend;

    public final String category;               // 分类
//    public final AvatarUrl avatarUrl;       // 头像

    public FriendListItem(Friend friend) {
        this.friend = friend;
        category = getCategory();
//        avatarUrl = new AvatarUrl(AvatarUrl.TYPE_FRIEND, friend.uri,
//                String.valueOf(friend.portraitCrc));
    }

    private String getCategory() {
        int sort = Integer.valueOf(friend.sortOrder.substring(0, 1));
        if (sort == Friend.SORT_OTHER)
        {
            // 英文外其他字符均归类到'#'下面
            return CATEGORY[CATEGORY.length - 1];
        }

        return friend.pinyin.substring(0, 1);
    }
}