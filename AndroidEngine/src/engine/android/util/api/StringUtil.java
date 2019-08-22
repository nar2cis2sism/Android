package engine.android.util.api;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 字符串及其编码工具
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public final class StringUtil {

    public static final String ISO          = "ISO-8859-1";

    public static final String US_ASCII     = "US-ASCII";

    public static final String GBK          = "GBK";

    public static final String UTF_8        = "UTF-8";         // Android默认字符串编码

    public static final String UTF_16BE     = "UTF-16BE";
    
    public static final String UNICODE      = "Unicode";

    /**
     * 功能：字符串转码
     * 
     * @param s 原字符串
     * @param src 转换前的编码类型
     * @param des 转换后的编码类型
     * @return 转换后的字符串
     */
    public static String toEncodingString(String s, String src, String des) {
        if (TextUtils.isEmpty(s))
        {
            return s;
        }

        try {
            byte[] bs = src == null ? s.getBytes() : s.getBytes(src);
            return des == null ? new String(bs) : new String(bs, des);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    /**
     * 判断某个字符是否双字节（汉字）
     */
    public static boolean isDoubleByte(String s, int index) {
        int ascii = Character.codePointAt(s, index);
        if (ascii < 0 || ascii > 255)
        {
            // 汉字
            return true;
        }

        return false;
    }

    /**
     * 获取字符串的字节长度（中文为2个字节）
     */
    public static int getByteLength(String s) {
        if (TextUtils.isEmpty(s))
        {
            return 0;
        }

        return s.replaceAll("[^\\x00-\\xff]", "**").length();
    }

    /**
     * 按字节截取字符串
     */
    public static String substring(String s, int length) throws Exception {
        if (length < 0)
        {
            throw new StringIndexOutOfBoundsException(length);
        }

        byte[] bs = s.getBytes(UNICODE);
        int n = 0; // 表示当前的字节数
        int i = 2; // 要截取的字节数，从第3个字节开始
        for (; i < bs.length && n < length; i++)
        {
            // 奇数位置，如3、5、7等，为UCS2编码中两个字节的第二个字节
            if (i % 2 == 1)
            {
                n++; // 在UCS2第二个字节时n加1
            }
            else
            {
                // 当UCS2编码的第一个字节不等于0时，该UCS2字符为汉字，一个汉字算两个字节
                if (bs[i] != 0)
                {
                    n++;
                }
            }
        }

        // 如果i为奇数时，处理成偶数
        if (i % 2 == 1)
        {
            // 该UCS2字符是汉字时，去掉这个截一半的汉字
            if (bs[i - 1] != 0)
            {
                i -= 1;
            }
            // 该UCS2字符是字母或数字，则保留该字符
            else
            {
                i += 1;
            }
        }

        return new String(bs, 0, i, UNICODE);
    }

    /***** 全角(SBC case)半角(DBC case)转换 *****/
    /***** 全角空格为12288，半角空格为32 *****/
    /***** 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248 *****/

    /**
     * 全角转半角
     */
    public static String SBC2DBC(String text) {
        char[] cs = text.toCharArray();
        for (int i = 0; i < cs.length; i++)
        {
            if (cs[i] == 12288)
            {
                cs[i] = (char) 32;
                continue;
            }

            if (cs[i] > 65280 && cs[i] < 65375)
            {
                cs[i] = (char) (cs[i] - 65248);
            }
        }

        return new String(cs);
    }

    /**
     * 半角转全角
     */
    public static String DBC2SBC(String text) {
        char[] cs = text.toCharArray();
        for (int i = 0; i < cs.length; i++)
        {
            if (cs[i] == 32)
            {
                cs[i] = (char) 12288;
                continue;
            }

            if (cs[i] < 127)
            {
                cs[i] = (char) (cs[i] + 65248);
            }
        }

        return new String(cs);
    }

    /**
     * String.format提高效率的替换实现
     * 
     * @param arg 仅有一个参数并且是字符串类型
     */
    public static String format(String format, String arg) {
        // return String.format(format, arg);
        // 事实证明以下方式运行更快(10:1)
        return format.replaceFirst("%s", arg);
    }
    
    /**
     * 将键值对拼装成XML格式的字符串
     */
    public static String getXMLString(String key, String value) {
        if (value != null)
        {
            if (value.length() == 0)
            {
                return String.format("<%s />", key);
            }
            else
            {
                return String.format("<%s>%s</%s>", key, value, key);
            }
        }
    
        return "";
    }

    @SuppressWarnings("deprecation")
    public static String urlEncode(String url, String charsetName) {
        try {
            if (TextUtils.isEmpty(charsetName))
            {
                return URLEncoder.encode(url);
            }
            else
            {
                return URLEncoder.encode(url, charsetName);
            }
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    @SuppressWarnings("deprecation")
    public static String urlDecode(String url, String charsetName) {
        try {
            if (TextUtils.isEmpty(charsetName))
            {
                return URLDecoder.decode(url);
            }
            else
            {
                return URLDecoder.decode(url, charsetName);
            }
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    /**
     * 解析URL地址里面的查询参数并分离成名值对表
     */
    public static Map<String, String> parseQueryParameters(String url) {
        if (!TextUtils.isEmpty(url))
        {
            int index = url.indexOf("?");
            if (index >= 0 && index < url.length() - 1)
            {
                url = url.substring(index + 1);
            }

            Map<String, String> map = new HashMap<String, String>();
            for (String s : url.split("&"))
            {
                index = s.indexOf("=");
                if (index > 0 && index < s.length() - 1)
                {
                    map.put(s.substring(0, index), s.substring(index + 1));
                }
            }

            return map;
        }

        return null;
    }

    /**
     * 追加查询参数到URL地址里面
     */
    public static String appendQueryParameters(String url, Map<String, String> map) {
        if (map == null || map.isEmpty())
        {
            return url;
        }
        
        StringBuilder sb = new StringBuilder(url);
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            if (first)
            {
                sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            else
            {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        
        return sb.toString();
    }

    /**
     * Null == ""
     */
    public static boolean equals(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }

    /**
     * 兼容{@link SpannableStringBuilder#append(CharSequence, Object, int)}
     */
    public static void appendSpan(SpannableStringBuilder span, String s, Object what) {
        int start = span.length();
        span.append(s);
        span.setSpan(what, start, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * 总有一些菜鸟把JSON格式拼错
     */
    public static String adjustJson(String json) {
        if (TextUtils.isEmpty(json))
        {
            return json;
        }
        
        return json.replace(":\"{", ":{").replace("}\"", "}")
                   .replace("\\\"", "\"");
    }

    /**
     * Alphabetical comparison of object.
     */
    public static abstract class AlphaComparator<T> implements Comparator<T> {
        
        private final Collator collator = Collator.getInstance();
    
        @Override
        public int compare(T lhs, T rhs) {
            return collator.compare(toString(lhs), toString(rhs));
        }
        
        public abstract String toString(T obj);
    }
}