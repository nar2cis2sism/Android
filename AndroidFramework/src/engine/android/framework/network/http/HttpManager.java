package engine.android.framework.network.http;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.text.TextUtils;

import engine.android.core.ApplicationManager;
import engine.android.framework.R;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.ConnectionInterceptor;
import engine.android.framework.network.event.Event;
import engine.android.framework.network.event.EventObserver;
import engine.android.framework.network.event.EventObserver.EventCallback;
import engine.android.http.HttpConnector;
import engine.android.http.HttpConnector.HttpConnectionListener;
import engine.android.http.HttpProxy;
import engine.android.http.HttpRequest;
import engine.android.http.HttpResponse;
import engine.android.http.util.HttpParser;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

/**
 * Http连接管理器<p>
 * 
 * @author Daimon
 */
public class HttpManager implements HttpConnectionListener, EventCallback {

    private final Context context;

    private final ConnectivityManager cm;
    
    private final AppConfig config;
    
    public HttpManager(Context context) {
        cm = (ConnectivityManager) (this.context = context.getApplicationContext())
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        config = AppGlobal.getConfig(context);
    }

    @Override
    public void connectBefore(HttpConnector conn, HttpRequest request) {
        if (config.isOffline())
        {
            return;
        }
        
        NetworkInfo info = cm.getActiveNetworkInfo(); // 获取当前网络连接信息
        if (info == null || !info.isAvailable())
        {
            // 无可用网络
            conn.cancel();
            receive(conn, DISCONNECTED, context.getString(R.string.connection_status_disconnected));
        }
        else if (info.getType() != ConnectivityManager.TYPE_WIFI)
        {
            // 不是WIFI网络
            if (Proxy.getHost(context) != null)
            {
                // 有代理网关
                conn.setProxy(new java.net.Proxy(Type.HTTP,
                              new InetSocketAddress(Proxy.getHost(context), Proxy.getPort(context))));
            }
        }
    }

    @Override
    public void connectAfter(HttpConnector conn, HttpResponse response) {
        try {
            int statusCode = response.getStatusCode();
            if (statusCode >= HttpURLConnection.HTTP_OK
            &&  statusCode <  HttpURLConnection.HTTP_MULT_CHOICE)
            {
                // Success
                if (config.isLogProtocol())
                {
                    log(conn.getName(), String.format("服务器返回%d--%s", 
                            statusCode, EntityUtil.toString(response.getContent())));
                }
                
                if (ApplicationManager.isDebuggable() && !config.isOffline())
                {
                    exportProtocolToFile(conn, response.getContent());
                }
                
                Object tag = conn.getTag();
                if (tag instanceof HttpParser)
                {
                    ((HttpParser) tag).parse(response);
                }
                
                return;
            }

            log(conn.getName(), String.format("服务器返回%d--%s", statusCode, response.getReasonPhrase()));
        } catch (Exception e) {
            log(conn.getName(), e);
        }
        
        receive(conn, FAIL, context.getString(R.string.connection_status_fail));
    }
    
    private void exportProtocolToFile(HttpConnector conn, byte[] content) {
        if (!SDCardManager.isEnabled())
        {
            return;
        }
        
        File desDir = new File(SDCardManager.openSDCardAppDir(context), 
                "protocols/http");
        
        File file = new File(desDir, conn.getName());
        FileManager.writeFile(file, EntityUtil.toString(content).getBytes(), false);
    }

    @Override
    public void connectError(HttpConnector conn, Exception e) {
        if (!isAccessible())
        {
            receive(conn, DISCONNECTED, context.getString(R.string.connection_status_disconnected));
        }
        else if (e instanceof SocketTimeoutException)
        {
            // 超时处理
            receive(conn, TIMEOUT, context.getString(R.string.connection_status_timeout));
        }
        else
        {
            receive(conn, ERROR, context.getString(R.string.connection_status_error));
        }
    }

    private boolean isAccessible() {
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    private void receive(HttpConnector conn, int status, Object param) {
        call(conn.getName(), status, param);
    }

    @Override
    public void call(String action, int status, Object param) {
        log(action + "|" + status + "|" + param);
        
        ConnectionInterceptor interceptor = config.getHttpInterceptor();
        if (interceptor != null && interceptor.intercept(action, status, param))
        {
            return;
        }

        EventObserver.getDefault().post(new Event(action, status, param));
    }
    
    public HttpConnector buildHttpConnector(String url, String name, String request, 
            HttpParser parser) {
        byte[] entity = null;
        if (!TextUtils.isEmpty(request))
        {
            if (config.isLogProtocol())
            {
                log(name, "发送请求--" + request);
            }
            
            entity = EntityUtil.toByteArray(request);
        }

        HttpConnector conn;
        if (config.isOffline())
        {
            conn = new HttpProxy(url, entity).setServlet(config.getHttpServlet());
        }
        else
        {
            conn = new HttpConnector(url, entity);
        }
        
        return conn
        .setName(name)
        .setTimeout(config.getHttpTimeout())
        .setTag(parser)
        .setListener(this);
    }
    
    public static interface HttpBuilder {
        
        HttpConnector buildHttpConnector();
    }
    
    public void sendHttpRequestAsync(final HttpBuilder builder) {
        config.getHttpThreadPool().submit(new Callable<HttpResponse>() {

            @Override
            public HttpResponse call() throws Exception {
                return builder.buildHttpConnector().connect();
            }
        });
    }
}