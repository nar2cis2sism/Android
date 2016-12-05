package engine.android.framework.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.Toast;

import engine.android.core.Forelet;
import engine.android.framework.MyConfiguration.MyConfiguration_NET;
import engine.android.framework.R;
import engine.android.framework.net.MyNetManager;
import engine.android.framework.net.event.Event;
import engine.android.framework.net.event.EventCallback;
import engine.android.framework.net.event.EventHandler;
import engine.android.framework.net.event.EventObserver;
import engine.android.framework.net.http.MyHttpManager.HttpBuilder;
import engine.android.http.HttpConnector;
import engine.android.util.Util;

import java.util.concurrent.Executor;

/**
 * 网络事件封装
 * 
 * @author Daimon
 */
public class BaseNetActivity extends Forelet implements EventHandler {
    
    /**
     * 检查网络状态
     * 
     * @param showTip 网络不可用时是否提示用户
     */
    public boolean checkNetStatus(boolean showTip) {
        if (MyConfiguration_NET.NET_OFF)
        {
            return true;
        }
        
        if (HttpConnector.isAccessible(this))
        {
            return true;
        }
        
        if (showTip)
        {
            Toast.makeText(this, R.string.net_is_not_accessible, Toast.LENGTH_SHORT).show();
        }
        
        return false;
    }
    
    public void sendHttpRequest(HttpBuilder builder) {
        executeTask(new HttpTask(builder));
    }
    
    public void sendHttpRequest(HttpBuilder builder, long delay) {
        executeTask(new HttpTask(builder), delay);
    }

    private static class HttpTask extends Task {
        
        private static final Executor exec
        = MyNetManager.getHttpManager().getThreadPool();

        public HttpTask(HttpBuilder builder) {
            super(new HttpTaskExecutor(builder), null);
        }

        @Override
        protected void doExecuteTask() {
            executeOnExecutor(exec);
        }
        
        private static class HttpTaskExecutor implements TaskExecutor {
            
            private final HttpBuilder builder;
            
            private volatile boolean isCancelled;
            
            private HttpConnector conn;
            
            public HttpTaskExecutor(HttpBuilder builder) {
                this.builder = builder;
            }

            @Override
            public Object doExecute() {
                synchronized (builder) {
                    if (isCancelled)
                    {
                        return null;
                    }
                    
                    conn = builder.buildHttpConnector();
                }
                
                try {
                    return conn.connect();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public void cancel() {
                synchronized (builder) {
                    if (conn != null)
                    {
                        conn.cancel();
                    }
                    else
                    {
                        isCancelled = true;
                    }
                }
            }
        }
    }

    /******************************* EventBus *******************************/
    
    private boolean isReceiveEventEnabled;
    
    /**
     * Call it in {@link #onCreate(android.os.Bundle)}
     */
    protected void enableReceiveEvent(String... actions) {
        if (isReceiveEventEnabled = true)
        {
            for (String action : actions)
            {
                EventObserver.getDefault().register(action, this);
            }
        }
    }

    @Override
    public void handleEvent(Event event) {
        onReceive(event.action, event.status, event.param);
    }
    
    protected void onReceive(String action, int status, Object param) {
        if (isReceiveSuccess(status, param))
        {
            onReceiveSuccess(action, param);
        }
    }
    
    protected void onReceiveSuccess(String action, Object param) {}
    
    @Override
    protected void onDestroy() {
        if (isReceiveEventEnabled)
        {
            EventObserver.getDefault().unregister(this);
        }
        
        super.onDestroy();
    }
    
    /**
     * 供子类调用（已做错误处理）
     */
    protected boolean isReceiveSuccess(int status, Object param) {
        if (status == EventCallback.SUCCESS)
        {
            return true;
        }
        
        hideProgress();
        showErrorDialog(param);
        return false;
    }

    protected void showErrorDialog(Object error) {
        Dialog dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.dialog_error_title)
        .setMessage(Util.getString(error, null))
        .setPositiveButton(R.string.ok, null)
        .create();

        showDialog("dialog_error", dialog);
    }
}