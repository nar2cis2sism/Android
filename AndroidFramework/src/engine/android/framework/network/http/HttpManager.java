package engine.android.framework.network.http;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.util.SparseArray;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.core.util.LogFactory;
import engine.android.framework.R;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.ConnectionStatus;
import engine.android.framework.network.http.HttpParser.Failure;
import engine.android.http.HttpConnector;
import engine.android.http.HttpConnector.HttpConnectionListener;
import engine.android.http.HttpProxy;
import engine.android.http.HttpRequest;
import engine.android.http.HttpRequest.HttpEntity;
import engine.android.http.HttpResponse;
import engine.android.http.util.HttpParser;
import protocol.java.EntityUtil;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

/**
 * Http连接管理器<p>
 * 需要声明权限
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class HttpManager implements HttpConnectionListener, ConnectionStatus {

    private final Context context;

    private final AppConfig config;
    
    private final ConnectivityManager cm;
    
    private final SparseArray<HttpAction> request = new SparseArray<HttpAction>();
    
    public HttpManager(Context context) {
        cm = (ConnectivityManager) (this.context = (config = AppGlobal.get(context).getConfig()).getContext())
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @SuppressWarnings("deprecation")
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
            receive(conn, DISCONNECTED, context.getString(R.string.connection_status_disconnected));
            conn.cancel();
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
                
                HttpAction action = request.get(conn.hashCode());
                if (action == null || conn.isCancelled())
                {
                    return;
                }

                Object param = action.response(response);
                if (param instanceof Failure)
                {
                    receive(conn, FAIL, param);
                }
                else
                {
                    receive(conn, SUCCESS, param);
                }
                
                return;
            }

            log(conn.getName(), String.format("服务器返回%d--%s", 
                    statusCode, response.getReasonPhrase()));
        } catch (Exception e) {
            log(conn.getName(), e);
        }
        
        receive(conn, FAIL, context.getString(R.string.connection_status_fail));
    }

    @Override
    public void connectError(HttpConnector conn, Exception e) {
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isAvailable())
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

    /**
     * 处理网络事件接收
     * 
     * @param conn 网络连接
     * @param status 网络状态
     * @param param 网络参数
     */
    private void receive(HttpConnector conn, int status, Object param) {
        String action = conn.getName();
        ConnectionInterceptor interceptor = config.getHttpInterceptor();
        if (interceptor != null && interceptor.intercept(action, status, param))
        {
            return;
        }

        EventBus.getDefault().post(new Event(action, status, param));
    }
    
    /**
     * 创建HTTP请求
     * 
     * @param url 请求地址
     * @param action 请求标识
     * @param entity 请求内容
     */
    public HttpConnector buildHttpConnector(String url, String action, HttpEntity entity) {
        HttpProxy conn = new HttpProxy(url, entity);
        if (config.isOffline())
        {
            conn.setServlet(config.getHttpServlet());
        }
        
        return conn
        .setName(action)
        .setTimeout(config.getHttpTimeout())
        .setListener(this);
    }
    
    private class HttpAction implements Callable<HttpResponse> {
        
        public final HttpConnector conn;
        public final HttpParser parser;
        
        public HttpAction(HttpConnector conn, HttpParser parser) {
            this.conn = conn;
            this.parser = parser;
        }

        @Override
        public HttpResponse call() throws Exception {
            try {
                return conn.connect();
            } finally {
                request.remove(conn.hashCode());
            }
        }
        
        public Object response(HttpResponse response) throws Exception {
            return parser != null ? parser.parse(response) : null;
        }
    }

    /**
     * 发送HTTP请求
     * 
     * @return 可用于取消请求
     */
    public int sendHttpRequest(HttpConnector conn, HttpParser parser) {
        if (config.isLogProtocol())
        {
            log(conn.getName(), "发送请求--" + conn.getRequest().getEntity());
        }
        
        int hash = conn.hashCode();
        if (request.indexOfKey(hash) < 0)
        {
            HttpAction action = new HttpAction(conn, parser);
            request.append(hash, action);
            config.getHttpThreadPool().submit(action);
        }
        
        return hash;
    }
    
    /**
     * 取消HTTP请求
     */
    public void cancelHttpRequest(int id) {
        int index = request.indexOfKey(id);
        if (index >= 0)
        {
            request.valueAt(index).conn.cancel();
            request.removeAt(index);
        }
    }

    /**
     * Implement this interface for individual logic of HTTP action.
     */
    public interface HttpBuilder {
        
        HttpConnector buildConnector(HttpConnectorBuilder builder);
        
        HttpParser buildParser();
    }
    
    /**
     * 发送HTTP请求
     * 
     * @return 可用于取消请求
     */
    public int sendHttpRequest(HttpBuilder http) {
        return sendHttpRequest(http.buildConnector(builder), http.buildParser());
    }
    
    private final HttpConnectorBuilder builder = new HttpConnectorBuilder(this);
    
    static
    {
        LogFactory.addLogFile(HttpManager.class, HttpConnector.class);
    }
}