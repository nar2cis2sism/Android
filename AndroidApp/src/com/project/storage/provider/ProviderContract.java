package com.project.storage.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contains definitions for the supported URIs and columns.
 * 
 * @author Daimon
 */
public final class ProviderContract {
    
    /** The authority for the provider */
    public static final String AUTHORITY = "app.provider";
    
    /** A content:// style uri to the authority for the provider */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    protected interface SortingColumns {

        /**
         * 排序字段
         * <P>Type: TEXT</P>
         */
        String SORTING = "sorting";
    }

    /**
     * @see User
     */
    public interface UserColumns {

        String USERNAME = "username";
        String NICKNAME = "nickname";
        String IS_FEMALE = "isFemale";
        String BIRTHDAY = "birthday";
        String REGION = "region";
        String REGION_CODE = "region_code";
        String SIGNATURE = "signature";
        String IS_AUTHENTICATED = "isAuthenticated";
        String VERSION = "version";
        String AVATAR_URL = "avatar_url";
        String AVATAR_VER = "avatar_ver";
        String FRIEND_LIST_TIMESTAMP = "friend_list_timestamp";
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

        String ACCOUNT = "account";
        String NICKNAME = "nickname";
        String SIGNATURE = "signature";
        String VERSION = "version";
        String AVATAR_URL = "avatar_url";
        String AVATAR_VER = "avatar_ver";
        String DISPLAY_NAME = "display_name";
        String PINYIN = "pinyin";
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

    /**
     * @see Message
     */
    public interface MessageColumns extends BaseColumns {

        String ID = "id";
        String ACCOUNT = "account";
        String CONTENT = "content";
        String TYPE = "type";
        String EVENT = "event";
        String CREATION_TIME = "creationTime";
        String IS_RECEIVED = "isReceived";
        String SEND_STATUS = "sendStatus";
    }

    public static final class Message implements MessageColumns {

        public static final String TABLE = "message";

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE);

        /**
         * This utility class cannot be instantiated
         */
        private Message() {}
    }
}