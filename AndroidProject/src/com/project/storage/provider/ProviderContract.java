package com.project.storage.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import static com.project.storage.provider.MyContentProvider.registerTable;

public final class ProviderContract {
    
    /** The authority for the provider */
    public static final String AUTHORITY = "com.project.provider";
    
    /** A content:// style uri to the authority for the provider */
    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    static void init() {
        // 对外暴露的表需要在这里注册
        registerTable(Friend.TABLE);
    }
    
    /**
     * 排序字段
     */
    protected interface SortColumns {

        /**
         * 汉字全拼（小写）
         * <P>Type: TEXT</P>
         */
        public static final String PINYIN = "pinyin";

        /**
         * 分类排序
         * <P>Type: TEXT</P>
         */
        public static final String SORT_ORDER = "sort_order";
    }

    /**
     * @see User
     */
    public interface UserColumns {

        /**
         * 用户存储在服务器上的ID，永远是唯一的
         * <P>Type: INTEGER</P>
         */
        public static final String USER_ID = BaseColumns._ID;

        public static final String NICK_NAME = "nickname";
        public static final String IS_FEMALE = "isFemale";
        public static final String BIRTHDAY = "birthday";
        public static final String CITY = "city";
        public static final String SIGNATURE = "signature";
        public static final String PROFILE = "profile";
        public static final String IS_AUTHENTICATED = "isAuthenticated";
        public static final String VERSION = "version";
        public static final String AVATAR_URL = "avatar_url";
        public static final String AVATAR_VER = "avatar_ver";
        public static final String FRIEND_LIST_TIMESTAMP = "friend_list_timestamp";
        
    }

    public static final class User implements UserColumns {

        public static final String TABLE = "user";

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE);

        /**
         * This utility class cannot be instantiated
         */
        private User() {}
    }
    
    /**
     * @see Friend
     */
    public interface FriendColumns extends SortColumns {

        /**
         * 用户存储在服务器上的ID，永远是唯一的
         * <P>Type: INTEGER</P>
         */
        public static final String USER_ID = BaseColumns._ID;
        public static final String REMARK = "remark";
        public static final String NICK_NAME = "nick_name";
        public static final String SIGNATURE = "signature";
        public static final String VERSION = "version";
        public static final String AVATAR_URL = "avatar_url";
        public static final String AVATAR_VER = "avatar_ver";
        public static final String DISPLAY_NAME = "display_name";
    }

    public static final class Friend implements FriendColumns {

        public static final String TABLE = "friend";

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE);

        /**
         * This utility class cannot be instantiated
         */
        private Friend() {}
    }
}