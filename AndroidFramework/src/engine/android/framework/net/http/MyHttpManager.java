package engine.android.framework.net.http;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.text.TextUtils;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory.LogUtil;
import engine.android.framework.MyConfiguration.MyConfiguration_HTTP;
import engine.android.framework.MyConfiguration.MyConfiguration_NET;
import engine.android.framework.net.MyNetManager;
import engine.android.framework.net.event.Event;
import engine.android.framework.net.event.EventCallback;
import engine.android.framework.net.event.EventObserver;
import engine.android.framework.net.http.util.EntityUtil;
import engine.android.http.HttpConnector;
import engine.android.http.HttpRequest;
import engine.android.http.HttpResponse;
import engine.android.http.util.HttpManager;
import engine.android.http.util.HttpParser;
import engine.android.util.MyThreadFactory;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 统一由{@link MyNetManager}管理，无需自己构造实例
 * 
 * @author Daimon
 */
public class MyHttpManager extends HttpManager implements
MyConfiguration_NET,
MyConfiguration_HTTP,
EventCallback {
    
    private static final int MAX_HTTP_CONNECTION
    = Math.max(3, Runtime.getRuntime().availableProcessors() - 1);
    
    private final Context context;
    
    private final ThreadPoolExecutor httpThreadPool;

    public MyHttpManager(Context context) {
        super(context);
        
        this.context = context.getApplicationContext();
        
        httpThreadPool = new ThreadPoolExecutor(
                MAX_HTTP_CONNECTION, 
                MAX_HTTP_CONNECTION,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), 
                new MyThreadFactory("Http网络连接"));
        httpThreadPool.allowCoreThreadTimeOut(true);
    }
    
    public ThreadPoolExecutor getThreadPool() {
        return httpThreadPool;
    }
    
    @Override
    public void connectBefore(HttpConnector conn, HttpRequest request) {
        if (NET_OFF)
        {
            return;
        }
    
        super.connectBefore(conn, request);
    }

    @Override
    public void connectAfter(HttpConnector conn, HttpResponse response) {
        try {
            int statusCode = response.getStatusCode();
            if (statusCode >= HttpURLConnection.HTTP_OK
            &&  statusCode <  HttpURLConnection.HTTP_MULT_CHOICE)
            {
                // Success
                if (NET_LOG_PROTOCOL)
                {
                    log(conn.getName(), "服务器返回--" + statusCode + ":" + toString(response.getContent()));
                }
                
                if (ApplicationManager.isDebuggable() && !NET_OFF)
                {
                    exportProtocolToFile(conn, response.getContent());
                }
                
                Object tag = conn.getTag();
                if (tag instanceof HttpTag)
                {
                    ((HttpTag) tag).parser.parse(response);
                }
                
                return;
            }
            
            log(conn.getName(), "服务器返回--" + statusCode + ":" + response.getReasonPhrase());
        } catch (Exception e) {
            log(conn.getName(), e);
        }
        
        receive(conn, FAIL, "出现未知错误，请稍后再试！");
    }
    
    private static String toString(byte[] content) {
        try {
            return EntityUtil.toString(content);
        } catch (Exception e) {
            return LogUtil.getExceptionInfo(e);
        }
    }
    
    private void exportProtocolToFile(HttpConnector conn, byte[] content) {
        if (!SDCardManager.isEnabled())
        {
            return;
        }
        
        File desDir = new File(SDCardManager.openSDCardAppDir(context), 
                "protocols/http");
        
        File file = new File(desDir, conn.getName());
        FileManager.writeFile(file, toString(content).getBytes(), false);
    }

    @Override
    public void receive(String name, int type, Object param) {
        call(name, type, param);
    }

    @Override
    public void call(String action, int status, Object param) {
        log(action + "|" + status + "|" + param);
        EventObserver.getDefault().post(new Event(action, status, param));
        
//        if (!interceptor.intercept(action, status, param))
//        {
//            EventBus.getDefault().post(new Event(action, status, param));
//        }
    }
    
    private static class HttpTag {
        
        public final HttpParser parser;
        
        public HttpTag(HttpParser parser) {
            this.parser = parser;
        }
    }
    
    public HttpConnector buildHttpConnector(String url, String name, String request, 
            HttpParser parser) {
        byte[] entity = null;
        if (!TextUtils.isEmpty(request))
        {
            if (NET_LOG_PROTOCOL)
            {
                log(LogUtil.getCallerStackFrame(), request);
            }
            
            entity = EntityUtil.toByteArray(request);
        }

        HttpConnector conn;
//        if (NET_OFF)
//        {
//            conn = new HttpProxy(url, entity).setServlet(servlet);
//        }
//        else
        {
            conn = new HttpConnector(url, entity);
        }
        
        if (parser != null)
        {
            conn.setTag(new HttpTag(parser));
        }
        
        return conn
        .setName(name)
        .setTimeout(HTTP_TIMEOUT)
        .setListener(this);
    }
    
    public void sendHttpRequestAsync(final HttpBuilder builder) {
        httpThreadPool.submit(new Callable<HttpResponse>() {

            @Override
            public HttpResponse call() throws Exception {
                return builder.buildHttpConnector().connect();
            }
        });
    }
    
    public static interface HttpBuilder {
        
        HttpConnector buildHttpConnector();
    }
}