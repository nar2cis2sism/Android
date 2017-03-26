package com.project.app.bean;

public class Friend {

    public static final String[] CATEGORY = {"#"};

    public final com.project.app.storage.db.Friend friend;

    public final String category;               // 分类
//    public final AvatarUrl avatarUrl;       // 头像

    public Friend(com.project.app.storage.db.Friend friend) {
        this.friend = friend;
        category = getCategory();
//        avatarUrl = new AvatarUrl(AvatarUrl.TYPE_FRIEND, friend.uri,
//                String.valueOf(friend.portraitCrc));
    }

    private String getCategory() {
        int sort = Integer.valueOf(friend.sortOrder.substring(0, 1));
        if (sort == com.project.app.storage.db.Friend.SORT_OTHER)
        {
            // 英文外其他字符均归类到'#'下面
            return CATEGORY[CATEGORY.length - 1];
        }

        return friend.pinyin.substring(0, 1);
    }
}