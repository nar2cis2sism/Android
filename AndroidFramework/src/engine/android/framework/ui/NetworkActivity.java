package engine.android.framework.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import engine.android.core.Forelet;
import engine.android.framework.R;
import engine.android.framework.app.App;
import engine.android.framework.app.AppConfig;
import engine.android.framework.network.event.Event;
import engine.android.framework.network.event.EventCallback;
import engine.android.framework.network.event.EventHandler;
import engine.android.framework.network.event.EventObserver;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.http.HttpConnector;
import engine.android.util.Util;

/**
 * 网络事件封装
 * 
 * @author Daimon
 */
class NetworkActivity extends Forelet implements EventHandler {
    
    private static final AppConfig CONFIG = App.getConfig();
    
    /**
     * 检查网络状态
     * 
     * @param showTip 网络不可用时是否提示用户
     */
    public boolean checkNetworkStatus(boolean showTip) {
        if (CONFIG.isOffline())
        {
            return true;
        }
        
        if (HttpConnector.isAccessible(this))
        {
            return true;
        }
        
        if (showTip)
        {
            Toast.makeText(this, R.string.connection_status_disconnected, Toast.LENGTH_SHORT).show();
        }
        
        return false;
    }
    
    public void sendHttpRequest(HttpBuilder builder) {
        executeTask(new HttpTask(builder), false);
    }

    private static class HttpTask extends Task {
        
        private static final Executor exec
        = CONFIG.getHttpThreadPool();

        public HttpTask(HttpBuilder builder) {
            super(new HttpTaskExecutor(builder));
        }

        @Override
        protected void doExecuteTask() {
            executeOnExecutor(exec);
        }
        
        private static class HttpTaskExecutor implements TaskExecutor {
            
            private final HttpBuilder builder;
            
            private final AtomicBoolean isCancelled = new AtomicBoolean();
            
            private HttpConnector conn;
            
            public HttpTaskExecutor(HttpBuilder builder) {
                this.builder = builder;
            }

            @Override
            public Object doExecute() {
                if (isCancelled.get())
                {
                    return null;
                }
                
                conn = builder.buildHttpConnector();
                if (isCancelled.get())
                {
                    return null;
                }
                
                try {
                    return conn.connect();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public void cancel() {
                if (isCancelled.compareAndSet(false, true) && conn != null)
                {
                    conn.cancel();
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
    public void handleEvent(final Event event) {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                onReceive(event.action, event.status, event.param);
            }
        });
    }
    
    private void onReceive(String action, int status, Object param) {
        if (status == EventCallback.SUCCESS)
        {
            onReceiveSuccess(action, param);
        }
        else
        {
            onReceiveFailure(action, status, param);
        }
    }
    
    protected void onReceiveSuccess(String action, Object param) {}
    
    protected void onReceiveFailure(String action, int status, Object param) {
        hideProgress();
        showErrorDialog(param);
    }
    
    protected void showErrorDialog(Object error) {
        Dialog dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.dialog_error_title)
        .setMessage(Util.getString(error, null))
        .setPositiveButton(R.string.ok, null)
        .create();
    
        showDialog("dialog_error", dialog);
    }

    @Override
    protected void onDestroy() {
        if (isReceiveEventEnabled)
        {
            EventObserver.getDefault().unregister(this);
        }
        
        super.onDestroy();
    }
}