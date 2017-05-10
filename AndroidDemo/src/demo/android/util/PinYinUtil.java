package demo.android.util;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public final class PinYinUtil {

    /**
     * 汉字转拼音
     * 
     * @return 全拼小写 Null表示解析错误
     */

    public static String getPinYin(String s) {
        if (TextUtils.isEmpty(s))
        {
            return "";
        }

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = s.toCharArray();
        String output = "";

        try {
            for (int i = 0; i < input.length; i++)
            {
                if (String.valueOf(input[i]).matches("[\u4e00-\u9fa5]+"))
                {
                    String[] strs = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    if (strs == null || strs.length == 0 || TextUtils.isEmpty(strs[0]))
                    {
                        continue;
                    }

                    output += strs[0];
                }
                else
                {
                    output += String.valueOf(input[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }
}