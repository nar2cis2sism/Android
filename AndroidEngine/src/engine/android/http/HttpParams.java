package engine.android.http;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Http参数设置
 * 
 * @author Daimon
 * @since 6/6/2015
 */
public class HttpParams {
    
    private Boolean instanceFollowRedirects;

    private Boolean useCaches;

    private Boolean allowUserInteraction;

    private Long ifModifiedSince;

    private Integer connectTimeout;

    private Integer readTimeout;
    
    private boolean supportHttps;

    HttpParams() {}

    /**
     * 设置是否自动处理重定向
     * 
     * @param followRedirects 默认为true
     */
    public HttpParams setRedirecting(boolean followRedirects) {
        instanceFollowRedirects = followRedirects;
        return this;
    }
    
    /**
     * 设置是否使用缓存
     */
    public HttpParams setUseCaches(boolean value) {
        useCaches = value;
        return this;
    }
    
    /**
     * @param value 默认为false
     */
    public HttpParams setAuthenticating(boolean value) {
        allowUserInteraction = value;
        return this;
    }

    public HttpParams setIfModifiedSince(long value) {
        ifModifiedSince = value;
        return this;
    }

    /**
     * 设置网络连接超时时间
     * 
     * @param timeout 单位：毫秒
     */
    public HttpParams setConnectTimeout(int timeout) {
        connectTimeout = timeout;
        return this;
    }

    /**
     * 设置数据读取超时时间
     * 
     * @param timeout 单位：毫秒
     */
    public HttpParams setReadTimeout(int timeout) {
        readTimeout = timeout;
        return this;
    }
    
    /**
     * 对HTTPS连接进行设置
     */
    public HttpParams setupHttps(boolean supportHttps) {
        this.supportHttps = supportHttps;
        return this;
    }
    
    void setup(HttpURLConnection conn) throws Exception {
        if (instanceFollowRedirects != null)
            conn.setInstanceFollowRedirects(instanceFollowRedirects);
        if (useCaches != null)
            conn.setUseCaches(useCaches);
        if (allowUserInteraction != null)
            conn.setAllowUserInteraction(allowUserInteraction);
        if (ifModifiedSince != null)
            conn.setIfModifiedSince(ifModifiedSince);
        if (connectTimeout != null)
            conn.setConnectTimeout(connectTimeout);
        if (readTimeout != null)
            conn.setReadTimeout(readTimeout);
        if (supportHttps && conn instanceof HttpsURLConnection)
        {
            HttpsURLConnection conns = (HttpsURLConnection) conn;
            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, tms, new SecureRandom());
            conns.setSSLSocketFactory(ssl.getSocketFactory());
        }
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() { return null; }
    }

    private static final TrustManager[] tms = { new DefaultTrustManager() };
}