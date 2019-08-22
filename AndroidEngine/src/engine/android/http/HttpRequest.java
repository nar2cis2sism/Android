package engine.android.http;

import engine.android.util.io.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Http请求体
 * 
 * @author Daimon
 * @since 6/6/2015
 */
public class HttpRequest {

    public static final String METHOD_GET  = "GET";
    public static final String METHOD_POST = "POST";

    private final String url;                       // 请求的链接地址

    private Map<String, String> headers;            // 请求头

    private final HttpEntity entity;                // 请求数据

    private final String method;                    // 请求的方式

    HttpRequest(String url, Map<String, String> headers, HttpEntity entity) {
        this.url = url;
        this.headers = headers;
        method = (this.entity = entity) == null ? METHOD_GET : METHOD_POST;
    }
    
    @Override
    protected HttpRequest clone() {
        Map<String, String> headers = this.headers;
        if (headers != null) headers = new HashMap<String, String>(headers);
        
        return new HttpRequest(url, headers, entity);
    }
    
    public String getUrl() {
        return url;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public HttpEntity getEntity() {
        return entity;
    }
    
    public String getMethod() {
        return method;
    }

    /**
     * 设置头信息
     */
    public HttpRequest setHeader(String key, String value) {
        if (headers == null) headers = new HashMap<String, String>();
        headers.put(key, value);
        return this;
    }
    
    public HttpRequest setContentType(String value) {
        return setHeader("Content-Type", value);
    }
    
    public HttpRequest setUserAgent(String value) {
        return setHeader("User-Agent", value);
    }
    
    public interface HttpEntity {
        
        long getContentLength();
        
        void writeTo(OutputStream out) throws IOException;
    }
    
    public static class ByteArrayEntity implements HttpEntity {
        
        private final byte[] content;
        
        public ByteArrayEntity(byte[] data) {
            content = data;
        }
        
        public final byte[] getContent() {
            return content;
        }

        @Override
        public long getContentLength() {
            return content.length;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            out.write(content);
        }
        
        @Override
        public String toString() {
            return "[Bytes]" + getContentLength();
        }
    }
    
    public static class StringEntity extends ByteArrayEntity {
        
        private final String string;
        
        public StringEntity(String s) {
            super(s.getBytes());
            this.string = s;
        }
        
        public StringEntity(String s, String charset) throws UnsupportedEncodingException {
            super(s.getBytes(charset));
            this.string = s;
        }
        
        @Override
        public String toString() {
            return string;
        }
    }
    
    public static class FileEntity implements HttpEntity {
        
        protected final File file;
        
        public FileEntity(File file) {
            this.file = file;
        }

        @Override
        public long getContentLength() {
            return file.length();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            FileInputStream fis = new FileInputStream(file);
            try {
                IOUtil.writeStream(fis, out);
            } finally {
                fis.close();
            }
        }
        
        @Override
        public String toString() {
            return "[File]" + file.toString();
        }
    }
}