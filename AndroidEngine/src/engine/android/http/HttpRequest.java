package engine.android.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Http请求体
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2015
 */
public class HttpRequest {

    public static final String METHOD_GET  = "GET";
    public static final String METHOD_POST = "POST";

    private final String url;                       // 请求的链接地址

    private final String method;                    // 请求的方式

    private Map<String, String> headers;            // 请求头

    private final byte[] postData;                  // 请求数据

    HttpRequest(String url, Map<String, String> headers, byte[] entity) {
        this.url = url;
        if (headers != null) this.headers = new HashMap<String, String>(headers);
        method = (postData = entity) == null ? METHOD_GET : METHOD_POST;
    }
    
    @Override
    public HttpRequest clone() throws CloneNotSupportedException {
        byte[] cloneData = null;
        if (postData != null) cloneData = Arrays.copyOf(postData, postData.length);
        
        return new HttpRequest(url, headers, cloneData);
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getMethod() {
        return method;
    }
    
    public byte[] getPostData() {
        return postData;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 设置头信息
     */

    public HttpRequest setHeader(String key, String value) {
        if (headers == null)
        {
            headers = new HashMap<String, String>();
        }

        headers.put(key, value);
        return this;
    }
    
    public void setContentType(String value) {
        setHeader("Content-Type", value);
    }
    
    public void setUserAgent(String value) {
        setHeader("User-Agent", value);
    }
}