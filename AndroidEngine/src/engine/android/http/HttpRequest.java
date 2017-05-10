package engine.android.http;

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

    private final ByteArray postData;               // 请求数据

    HttpRequest(String url, Map<String, String> headers, ByteArray postData) {
        this.url = url;
        this.headers = headers;
        method = (this.postData = postData) == null ? METHOD_GET : METHOD_POST;
    }
    
    @Override
    protected HttpRequest clone() {
        Map<String, String> headers = this.headers;
        if (headers != null) headers = new HashMap<String, String>(headers);
        
        return new HttpRequest(url, headers, postData);
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getMethod() {
        return method;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public byte[] getPostData() {
        return postData == null ? null : postData.toByteArray();
    }

    /**
     * 设置头信息
     */
    public HttpRequest setHeader(String key, String value) {
        if (headers == null) headers = new HashMap<String, String>();
        headers.put(key, value);
        return this;
    }
    
    public void setContentType(String value) {
        setHeader("Content-Type", value);
    }
    
    public void setUserAgent(String value) {
        setHeader("User-Agent", value);
    }
    
    public interface ByteArray {
        
        byte[] toByteArray();
    }
    
    public static class ByteArrayEntity implements ByteArray {
        
        private final byte[] data;
        
        public ByteArrayEntity(byte[] data) {
            this.data = data;
        }

        @Override
        public byte[] toByteArray() {
            return data;
        }
    }
}