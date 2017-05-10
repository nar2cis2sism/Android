package engine.android.http;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import engine.android.http.HttpRequest.ByteArray;

/**
 * Http代理（单机测试用）
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class HttpProxy extends HttpConnector {

    private HttpServlet servlet;

    public HttpProxy(String url) {
        super(url);
    }

    public HttpProxy(String url, ByteArray postData) {
        super(url, postData);
    }

    public HttpProxy(String url, Map<String, String> headers, ByteArray postData) {
        super(url, headers, postData);
    }

    public HttpProxy setServlet(HttpServlet servlet) {
        this.servlet = servlet;
        return this;
    }
    
    @Override
    protected HttpResponse doConnect(HttpRequest request) throws Exception {
        if (servlet == null)
        {
            return super.doConnect(request);
        }
        
        HttpServlet.HttpResponse resp = new HttpServlet.HttpResponse();
        servlet.doServlet(request, resp);
        
        return new MockResponse(HttpURLConnection.HTTP_OK, null, resp);
    }
    
    private static class MockResponse extends HttpResponse {
        
        private final Map<String, String> headers;

        public MockResponse(int responseCode, String message, HttpServlet.HttpResponse resp) {
            super(responseCode, message, resp.entity);
            headers = resp.headers;
        }
        
        @Override
        public String getHeaderField(String key) {
            return headers == null ? null : headers.get(key);
        }
    }

    public interface HttpServlet {

        void doServlet(HttpRequest req, HttpResponse resp);
        
        public static class HttpResponse {

            Map<String, String> headers;            // 响应头

            byte[] entity;                          // 响应数据
            
            HttpResponse() {}

            /**
             * 设置头信息
             */
            public void setHeader(String key, String value) {
                if (headers == null) headers = new HashMap<String, String>();
                headers.put(key, value);
            }
            
            public void setEntity(byte[] entity) {
                this.entity = entity;
            }
        }
    }
}