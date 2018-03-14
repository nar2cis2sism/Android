package com.project.storage.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ProviderContract {
    
    /** A content:// style uri to the authority for the provider */
    private static final Uri AUTHORITY_URI = Uri.parse("content://" + MyContentProvider.AUTHORITY);
    
    protected interface SortingColumns {

        /**
         * 排序字段
         * <P>Type: TEXT</P>
         */
        public static final String SORTING = "sorting";
    }

    /**
     * @see User
     */
    public interface UserColumns extends BaseColumns {

        public static final String USERNAME = "username";
        public static final String NICKNAME = "nickname";
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
    public interface FriendColumns extends SortingColumns {

        public static final String ACCOUNT = "account";
        public static final String NICK_NAME = "nick_name";
        public static final String SIGNATURE = "signature";
        public static final String AVATAR_URL = "avatar_url";
        public static final String DISPLAY_NAME = "display_name";
        public static final String PINYIN = "pinyin";
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