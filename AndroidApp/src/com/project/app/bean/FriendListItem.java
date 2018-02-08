package com.project.app.bean;

import com.project.util.MyValidator;

import net.sourceforge.pinyin4j.lite.PinyinHelper;

public class FriendListItem {

    public static final String[] CATEGORY = {"搜", "A", "B", "C", "D", "E", "F", "G", "H", 
                                             "I", "J", "K", "L", "M", "N", "O", "P", "Q", 
                                             "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    
    public static final int SORT_ENGLISH    = 0;
    public static final int SORT_OTHER      = 1;

    public final String category;               // 分类
    public final String name;                   // 名称
    public final String pinyin;                 // 名称全拼（小写）
    public final String signature;              // 签名
    public final String sortOrder;              // 分类排序
    
    public FriendListItem(String name, String signature) {
        pinyin = PinyinHelper.getInstance().getPinyins(this.name = name, "").toLowerCase();
        sortOrder = sort(pinyin);
        category = getCategory(pinyin);
        this.signature = signature;
    }

    private String getCategory(String pinyin) {
        if (getSort(pinyin) == SORT_OTHER)
        {
            // 英文外其他字符均归类到'#'下面
            return CATEGORY[CATEGORY.length - 1];
        }
    
        return pinyin.substring(0, 1).toUpperCase();
    }
    
    /**
     * 根据拼音获取排序分类
     * 
     * @return {@link #SORT_ENGLISH}, {@link #SORT_OTHER}
     */
    private static int getSort(String pinyin) {
        String firstLetter = pinyin.substring(0, 1);
        if (MyValidator.validate(firstLetter, MyValidator.ENGLISH))
        {
            return SORT_ENGLISH;
        }
        else
        {
            // 首字母为英文外其他字符排在后面
            return SORT_OTHER;
        }
    }
    
    /**
     * 根据拼音排序
     */
    private static String sort(String pinyin) {
        return getSort(pinyin) + pinyin;
    }
}