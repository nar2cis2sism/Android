package engine.android.framework.network.file;

import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.http.HttpConnector;
import engine.android.util.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.text.TextUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Http连接器<p>
 * 需要声明权限<uses-permission android:name="android.permission.INTERNET" />
 * 
 * @author Daimon
 * @since 7/29/2013
 * 
 * Daimon:HttpClient
 * @deprecated
 */
public class HttpExecutor {
    
    private String remark;                                          // 备注（用于日志查询）

    private final HttpUriRequest request;                           // 连接请求

    private HttpClient client;                                      // 连接管理

    private boolean useDefaultHttpClient;                           // 是否使用默认连接管理器设置

    private HttpContext context;                                    // 连接环境

    private HttpHost proxy;                                         // 连接代理

    private int timeout;                                            // 超时时间（毫秒）

    private String charset;                                         // 字符编码格式

    private final AtomicBoolean isConnected = new AtomicBoolean();  // 网络是否连接完成

    private final AtomicBoolean isCancelled = new AtomicBoolean();  // 是否取消网络连接

    public HttpExecutor(HttpUriRequest request) {
        this.request = request;
    }

    /**
     * GET请求
     * 
     * @param url 请求URL地址
     */
    public HttpExecutor(String url) {
        this(url, null, null);
    }

    /**
     * POST请求
     * 
     * @param url 请求URL地址
     * @param entity 请求数据
     */
    public HttpExecutor(String url, HttpEntity entity) {
        this(url, null, entity);
    }

    /**
     * Http连接请求
     * 
     * @param url 请求URL地址
     * @param headers 请求头
     * @param entity 请求数据
     */
    public HttpExecutor(String url, Map<String, String> headers, HttpEntity entity) {
        if (entity == null)
        {
            request = new HttpGet(url);
        }
        else
        {
            HttpPost post = new HttpPost(url);
            post.setEntity(entity);
            request = post;
        }

        if (headers != null && !headers.isEmpty())
        {
            for (Entry<String, String> entry : headers.entrySet())
            {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    public HttpUriRequest getRequest() {
        return request;
    }

    /**
     * 设置连接管理器
     */
    public HttpExecutor setHttpClient(HttpClient client) {
        this.client = client;
        useDefaultHttpClient = false;
        return this;
    }

    /**
     * 返回连接管理器（设置连接参数）
     * 
     * @return 如没设置则默认建立一个
     */
    public HttpClient getHttpClient() {
        if (client == null)
        {
            client = new DefaultHttpClient();
            useDefaultHttpClient = true;
        }

        return client;
    }

    /**
     * 设置连接环境
     */
    public HttpExecutor setHttpContext(HttpContext context) {
        this.context = context;
        return this;
    }

    /**
     * 返回连接环境
     * 
     * @return 如没设置则默认建立一个
     */
    public HttpContext getHttpContext() {
        if (context == null)
        {
            context = new BasicHttpContext();
        }

        return context;
    }

    /**
     * Sets an optional CookieStore to use when making requests
     */
    public HttpExecutor setCookieStore(CookieStore cookieStore) {
        getHttpContext().setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        return this;
    }

    /**
     * 根据手机设置自动选择代理<br>
     * 需要声明权限<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     */
    public HttpExecutor setProxy(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo(); // 获取当前网络连接信息
        if (info != null && info.getType() != ConnectivityManager.TYPE_WIFI)
        {
            // 不是WIFI网络
            if (Proxy.getHost(context) != null)
            {
                // 有代理网关
                new HttpHost(Proxy.getHost(context), Proxy.getPort(context));
            }
        }

        return this;
    }


    /**
     * 设置代理主机
     */
    public HttpExecutor setProxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * 设置代理地址（不含scheme）
     */
    public HttpExecutor setProxyAddress(String address) {
        int port = 80;
        int index = address.indexOf(":");
        if (index > 0)
        {
            port = Integer.parseInt(address.substring(index + 1));
            address = address.substring(0, index);
        }

        proxy = new HttpHost(address, port);
        return this;
    }


    /**
     * 设置超时时间
     * 
     * @param timeout 单位：毫秒
     */
    public HttpExecutor setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置字符编码格式，默认为ISO编码
     * 
     * @param charset {@link HTTP#DEFAULT_CONTENT_CHARSET}
     */
    public HttpExecutor setContentCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 连接网络
     */
    public synchronized HttpEntity execute() throws Exception {
        if (isCancelled())
        {
            return null;
        }
        
        log("联网请求：" + request.getURI());
        long time = System.currentTimeMillis();
        try {
            HttpResponse response = doExecute(request);
            if (!isCancelled())
            {
                HttpHost target = getTargetHost();
                log(String.format("服务器%s响应时间--%dms", 
                        Util.getString(target, ""), System.currentTimeMillis() - time));
                
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                if (statusCode >= HttpStatus.SC_OK
                &&  statusCode <  HttpStatus.SC_MULTIPLE_CHOICES)
                {
                    // Success
                    HttpEntity entity = response.getEntity();
                    if (entity != null) entity = new BufferedHttpEntity(entity);
                    return entity;
                }

                throw new Exception(String.format("服务器返回%d--%s", statusCode, status.getReasonPhrase()));
            }
        } catch (Exception e) {
            if (!isCancelled())
            {
                log(e);
                throw e;
            }
        } finally {
            isConnected.set(true);
            client.getConnectionManager().shutdown();
        }

        return null;
    }

    protected HttpResponse doExecute(HttpUriRequest request) throws Exception {
        HttpClient client = getHttpClient();
        if (useDefaultHttpClient)
        {
            HttpParams params = client.getParams();
            // 设置超时
            if (timeout > 0)
            {
                HttpConnectionParams.setConnectionTimeout(params, timeout);
                HttpConnectionParams.setSoTimeout(params, timeout);
            }
            // 设置代理
            if (proxy != null)
            {
                log("使用代理网关：" + proxy);
                params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            // 设置字符编码格式
            if (charset != null)
            {
                HttpProtocolParams.setContentCharset(params, charset);
            }
        }
        
        return client.execute(request, getHttpContext());
    }

    /**
     * 取消网络连接
     */
    public final void cancel() {
        if (isCancelled.compareAndSet(false, true))
        {
            if (!isConnected.get()) log("取消网络连接：" + request.getURI());
            request.abort();
        }
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }

    /**
     * 日志输出
     */
    private void log(Object message) {
        if (!TextUtils.isEmpty(remark)) LOG.log(remark, message);
    }

    /**
     * 获取目标主机（必须连接成功以后才有返回）
     * 
     * @return 重定向之后的最终地址
     */
    public HttpHost getTargetHost() {
        return (HttpHost) getHttpContext().getAttribute(ExecutionContext.HTTP_TARGET_HOST);
    }

    /**
     * HttpClient制造类
     */
    public static class HttpClientBuilder {

        private final HttpClient client;

        private final HttpParams params;

        public HttpClientBuilder() {
            this(new DefaultHttpClient());
        }

        public HttpClientBuilder(HttpClient client) {
            params = (this.client = client).getParams();
        }

        /**
         * @param version {@link HttpVersion#HTTP_1_1}
         */
        public HttpClientBuilder setVersion(ProtocolVersion version) {
            HttpProtocolParams.setVersion(params, version);
            return this;
        }

        /**
         * @param charset {@link HTTP#DEFAULT_CONTENT_CHARSET}
         */
        public HttpClientBuilder setContentCharset(String charset) {
            HttpProtocolParams.setContentCharset(params, charset);
            return this;
        }

        /**
         * @param b 默认为true
         */
        public HttpClientBuilder setUseExpectContinue(boolean b) {
            HttpProtocolParams.setUseExpectContinue(params, b);
            return this;
        }

        public HttpClientBuilder setUserAgent(String useragent) {
            HttpProtocolParams.setUserAgent(params, useragent);
            return this;
        }

        /**
         * 设置重定向
         * 
         * @param value 默认为true
         */
        public HttpClientBuilder setRedirecting(boolean value) {
            HttpClientParams.setRedirecting(params, value);
            return this;
        }

        /**
         * 设置网络连接超时时间
         * 
         * @param timeout 单位：毫秒
         */
        public HttpClientBuilder setConnectionTimeout(int timeout) {
            HttpConnectionParams.setConnectionTimeout(params, timeout);
            return this;
        }

        /**
         * 设置数据读取超时时间
         * 
         * @param timeout 单位：毫秒
         */
        public HttpClientBuilder setSoTimeout(int timeout) {
            HttpConnectionParams.setSoTimeout(params, timeout);
            return this;
        }

        /**
         * 设置默认连接代理
         */
        public HttpClientBuilder setProxy(HttpHost proxy) {
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            return this;
        }

        /**
         * 创建HttpClient方法
         */
        public HttpClient build() {
            return client;
        }
    }

    static
    {
        LogFactory.addLogFile(HttpExecutor.class, HttpConnector.class);
    }
}