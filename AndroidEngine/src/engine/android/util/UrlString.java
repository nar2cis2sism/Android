package engine.android.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UrlString {

    public static String UrlEncode(String url, String charsetName) {
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

    public static String UrlDecode(String url, String charsetName) {
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
}