package engine.android.http.util.extra;

import android.text.TextUtils;

import engine.android.http.HttpRequest;
import engine.android.util.MyThreadFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Http连接池（使用队列机制）
 * <未验证>
 * 
 * @author Daimon
 * @version 3.0
 * @since 7/29/2013
 */

public class HttpConnectionPool {

    private final Map<String, HttpRunnable> conns;      // 联网请求队列

    private final ExecutorService es;                   // 联网请求线程池

    private HttpRunnable localRequest;                  // 当前联网请求处理线程

    public HttpConnectionPool() {
        conns = new HashMap<String, HttpRunnable>();
        es = Executors.newSingleThreadExecutor(new MyThreadFactory("Http连接队列"));
    }

    protected HttpURLConnection sendRequest(HttpRequest request) throws Exception {
        HttpURLConnection conn = HttpRequest.connect(request);
        System.out.println(String.format("发送%s请求:%s",
                conn.getRequestMethod(), conn.getURL()));
        return conn;
    }

    /**
     * 向请求队列中添加HTTP请求
     */

    public void addRequest(HttpRequest request, HttpParser parser) {
        String name = request.getName();
        if (TextUtils.isEmpty(name))
        {
            if (parser == null)
            {
                new HttpRunnable(request, parser).start();
                return;
            }
            else
            {
                throw new RuntimeException("请给网络请求取个名字");
            }
        }

        if (conns.containsKey(name))
        {
            return;
        }

        HttpRunnable http = new HttpRunnable(request, parser);
        conns.put(name, http);
        http.start();
    }

    /**
     * 取消当前HTTP请求
     */

    public void cancelRequest() {
        if (localRequest != null)
        {
            localRequest.cancel();
        }
    }

    /**
     * 取消指定的HTTP请求
     * 
     * @param name 请求名称
     */

    public void cancelRequest(String name) {
        if (TextUtils.isEmpty(name))
        {
            return;
        }

        for (Map.Entry<String, HttpRunnable> entry : conns.entrySet())
        {
            if (name.equals(entry.getKey()))
            {
                entry.getValue().cancel();
                break;
            }
        }
    }

    public static interface HttpParser {

        public void parse(InputStream is) throws Exception;
    }

    private class HttpRunnable implements Runnable {

        private final HttpRequest request;

        private final HttpParser parser;

        public HttpRunnable(HttpRequest request, HttpParser parser) {
            this.request = request;
            this.parser = parser;
        }

        public void start() {
            es.execute(this);
        }

        public void run() {
            if (parser != null)
            {
                localRequest = this;
            }

            try {
                HttpURLConnection conn = sendRequest(request);
                if (conn != null && parser != null)
                {
                    parser.parse(conn.getInputStream());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                request.close();
            }

            localRequest = null;
            conns.remove(getName());
        }

        public void cancel() {
            request.cancel();
        }

        public String getName() {
            return request.getName();
        }
    }
}