package engine.android.http.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;

import engine.android.core.util.LogFactory.LogUtil;
import engine.android.http.HttpConnector;
import engine.android.http.HttpConnector.HttpConnectionListener;
import engine.android.http.HttpRequest;
import engine.android.http.HttpResponse;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.SocketTimeoutException;

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
public abstract class HttpManager implements HttpConnectionListener, HttpConnectionStatus {

    private final Context context;

    private final ConnectivityManager cm;                       // 网络连接管理器

    public HttpManager(Context context) {
        cm = (ConnectivityManager) (this.context = context.getApplicationContext())
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void connectBefore(HttpConnector conn, HttpRequest request) {
        NetworkInfo info = cm.getActiveNetworkInfo(); // 获取当前网络连接信息
        if (info == null || !info.isAvailable())
        {
            // 无可用网络
            conn.cancel();
            receive(conn, DISCONNECTED, "DISCONNECTED");
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
        int statusCode = response.getStatusCode();
        if (statusCode >= HttpURLConnection.HTTP_OK
        &&  statusCode <  HttpURLConnection.HTTP_MULT_CHOICE)
        {
            receive(conn, SUCCESS, statusCode + ":" + response.getReasonPhrase());
        }
        else
        {
            receive(conn, FAIL, statusCode + ":" + response.getReasonPhrase());
        }
    }

    @Override
    public void connectError(HttpConnector conn, Exception e) {
        if (!HttpConnector.isAccessible(context))
        {
            receive(conn, DISCONNECTED, "DISCONNECTED");
        }
        else if (e instanceof SocketTimeoutException)
        {
            // 超时处理
            receive(conn, TIMEOUT, "TIMEOUT");
        }
        else
        {
            receive(conn, ERROR, "ERROR:" + LogUtil.getExceptionInfo(e));
        }
    }

    protected void receive(HttpConnector conn, int type, Object param) {
        receive(conn.getName(), type, param);
    }

    /**
     * 处理网络事件接收
     * 
     * @param name 事件名称
     * @param type 事件类型
     * @param param 事件参数
     */
    public abstract void receive(String name, int type, Object param);
}