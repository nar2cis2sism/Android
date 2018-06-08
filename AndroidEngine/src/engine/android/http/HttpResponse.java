package engine.android.http;

import android.text.TextUtils;

import engine.android.util.io.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Http响应体
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2015
 */
public class HttpResponse {
    
    private final int code;
    private final String reason;
    private final byte[] content;
    
    private Map<String, List<String>> headers;
    
    HttpResponse(HttpURLConnection conn) throws Exception {
        this(conn.getResponseCode(), conn.getResponseMessage(), IOUtil.readStream(conn.getInputStream()));
        headers = conn.getHeaderFields();
    }
    
    HttpResponse(int responseCode, String message, byte[] data) {
        code = responseCode;
        reason = message;
        content = data;
    }
    
    public int getStatusCode() {
        return code;
    }
    
    public String getReasonPhrase() {
        return reason;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }
    
    private String getHeaderField(String key) {
        if (headers == null)
        {
            return null;
        }
        
        List<String> list = headers.get(key);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        
        if (list.size() == 1)
        {
            return list.get(0);
        }
        
        return TextUtils.join(",", list);
    }
    
    private int getHeaderFieldInt(String field, int defaultValue) {
        try {
            return Integer.parseInt(getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @SuppressWarnings("deprecation")
    private long getHeaderFieldDate(String field, long defaultValue) {
        String date = getHeaderField(field);
        if (date == null)
        {
            return defaultValue;
        }
        
        try {
            return Date.parse(date); // TODO: use HttpDate.parse()
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取头信息
     */
    public String getHeader(String key) {
        return getHeaderField(key);
    }
    
    public String getContentEncoding() {
        return getHeaderField("Content-Encoding");
    }
    
    public int getContentLength() {
        return getHeaderFieldInt("Content-Length", -1);
    }
    
    public String getContentType() {
        return getHeaderField("Content-Type");
    }
    
    public String getUserAgent() {
        return getHeaderField("User-Agent");
    }
    
    public long getDate() {
        return getHeaderFieldDate("Date", 0);
    }
    
    public long getExpiration() {
        return getHeaderFieldDate("Expires", 0);
    }
    
    public long getLastModified() {
        return getHeaderFieldDate("Last-Modified", 0);
    }
    
    public String getLocation() {
        return getHeaderField("Location");
    }
    
    public String getHost() {
        return getHeaderField("Host");
    }
}