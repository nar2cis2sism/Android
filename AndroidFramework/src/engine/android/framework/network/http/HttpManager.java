package engine.android.framework.network.http;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.util.SparseArray;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import engine.android.core.ApplicationManager;
import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.framework.R;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.ConnectionStatus;
import engine.android.framework.network.http.util.EntityUtil;
import engine.android.framework.network.http.util.HttpParser.Failure;
import engine.android.http.HttpConnector;
import engine.android.http.HttpConnector.HttpConnectionListener;
import engine.android.http.HttpProxy;
import engine.android.http.HttpRequest;
import engine.android.http.HttpRequest.ByteArray;
import engine.android.http.HttpResponse;
import engine.android.http.util.HttpParser;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;

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
    
    private final SparseArray<HttpAction> request
    = new SparseArray<HttpAction>();
    
    public HttpManager(Context context) {
        cm = (ConnectivityManager) (this.context = (config = AppGlobal.get(context).getConfig()).getContext())
                .getSystemService(Context.CONNECTIVITY_SERVICE);
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
                
                if (ApplicationManager.isDebuggable(context) && !config.isOffline())
                {
                    exportProtocolToFile(conn, response.getContent());
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
    
    private void exportProtocolToFile(HttpConnector conn, byte[] content) {
        if (!SDCardManager.isEnabled()) return;
        
        File desDir = new File(SDCardManager.openSDCardAppDir(context), 
                "protocols/http");
        
        File file = new File(desDir, conn.getName());
        FileManager.writeFile(file, EntityUtil.toString(content).getBytes(), false);
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
    protected void receive(HttpConnector conn, int status, Object param) {
        String action = conn.getName();
        log(action + "|" + status + "|" + param);
        
        ConnectionInterceptor interceptor = config.getHttpInterceptor();
        if (interceptor != null && interceptor.intercept(action, status, param))
        {
            return;
        }

        EventBus.getDefault().post(new Event(action, status, param));
    }
    
    private static class StringEntiry implements ByteArray {
        
        private final StringEntity entity;
        
        public StringEntiry(StringEntity entity) {
            this.entity = entity;
        }

        @Override
        public byte[] toByteArray() {
            return EntityUtil.toByteArray(entity.toString());
        }
    }
    
    public interface StringEntity {
        
        String toString();
    }
    
    /**
     * 创建HTTP请求
     * 
     * @param url 请求地址
     * @param action 请求标识
     * @param entity 请求内容
     */
    public HttpConnector buildHttpConnector(String url, String action, StringEntity entity) {
        if (config.isLogProtocol())
        {
            log(action, "发送请求--" + entity.toString());
        }

        HttpProxy conn = new HttpProxy(url, new StringEntiry(entity));
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
    
    public interface HttpBuilder {
        
        HttpConnector buildConnector(HttpManager http);
        
        HttpParser buildParser();
    }

    /**
     * 发送HTTP请求
     * 
     * @return 可用于取消请求
     */
    public int sendHttpRequest(HttpBuilder http) {
        return sendHttpRequest(http.buildConnector(this), http.buildParser());
    }

    /**
     * 发送HTTP请求
     * 
     * @return 可用于取消请求
     */
    public int sendHttpRequest(HttpConnector conn, HttpParser parser) {
        int hash = conn.hashCode();
        
        int index = request.indexOfKey(hash);
        if (index < 0)
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
}