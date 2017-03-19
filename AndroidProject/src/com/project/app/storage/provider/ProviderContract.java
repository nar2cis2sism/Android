package com.project.app.storage.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ProviderContract {
    
    /** The authority for the provider */
    public static final String AUTHORITY = "com.project.provider";
    
    /** A content:// style uri to the authority for the provider */
    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    /**
     * 排序字段
     */
    public interface SortColumns {

        /**
         * 拼音（大写）
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
     * @see Friend
     */
    public interface FriendColumns extends SortColumns {

        /**
         * 用户存储在服务器上的ID，永远是唯一的
         * <P>Type: INTEGER</P>
         */
        public static final String USER_ID = BaseColumns._ID;

        /**
         * 备注
         * <P>Type: TEXT</P>
         */
        public static final String REMARK = "remark";

        /**
         * 昵称
         * <P>Type: TEXT</P>
         */
        public static final String NICK_NAME = "nick_name";

        /**
         * 签名
         * <P>Type: TEXT</P>
         */
        public static final String SIGNATURE = "signature";

        /**
         * 头像下载地址
         * <P>Type: TEXT</P>
         */
        public static final String AVATAR_URL = "avatar_url";

        /**
         * 好友信息版本号
         * <P>Type: TEXT</P>
         */
        public static final String VERSION = "version";

        /**
         * 显示名称
         * <P>Type: TEXT</P>
         */
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