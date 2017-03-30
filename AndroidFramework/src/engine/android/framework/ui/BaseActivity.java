package engine.android.framework.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import engine.android.core.Forelet;
import engine.android.framework.R;
import engine.android.framework.app.App;
import engine.android.framework.app.AppConfig;
import engine.android.framework.network.event.Event;
import engine.android.framework.network.event.EventObserver;
import engine.android.framework.network.event.EventObserver.EventCallback;
import engine.android.framework.network.event.EventObserver.EventHandler;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.http.HttpConnector;
import engine.android.util.Util;
import engine.android.widget.TitleBar;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseActivity extends Forelet implements EventHandler {
    
    private LinearLayout root;
    
    private TitleBar title_bar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        
        setupTitleBar(title_bar = (TitleBar) getLayoutInflater().inflate(
                R.layout.title_bar, root, false));
    }
    
    private void setupTitleBar(TitleBar title_bar) {
        title_bar.findViewById(R.id.navigation_up).setOnClickListener(
                new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onNavigationUpClicked();
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, root, false));
    }

    @Override
    public void setContentView(View view) {
        root.removeAllViewsInLayout();
        
        root.addView(title_bar);
        root.addView(view);
        
        super.setContentView(root);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        view.setLayoutParams(params);
        setContentView(view);
    }
    
    /******************** TitleBar模块 ********************/
    
    public final TitleBar getTitleBar() {
        return title_bar;
    }
    
    final void onNavigationUpClicked() {
        Class<? extends Activity> cls = parentActivity();
        if (cls != null)
        {
            navigateUpTo(cls);
        }
        else
        {
            finish();
        }
    }
    
    @Override
    protected void navigateUpTo(Class<? extends Activity> cls) {
        super.navigateUpTo(cls);
    }
    
    @Override
    protected Class<? extends Activity> parentActivity() {
        return super.parentActivity();
    }
    
    /******************** 回退事件处理 ********************/
    
    public interface OnBackListener {
        
        boolean onBackPressed();
    }
    
    private LinkedList<OnBackListener> onBackListener;
    
    public void addOnBackListener(OnBackListener listener) {
        if (onBackListener == null)
        {
            onBackListener = new LinkedList<OnBackListener>();
        }
        
        onBackListener.addFirst(listener);
    }
    
    @Override
    public void onBackPressed() {
        if (onBackListener != null)
        {
            for (OnBackListener listener : onBackListener)
            {
                if (listener.onBackPressed())
                {
                    return;
                }
            }
        }
        
        super.onBackPressed();
    }
    
    /**
     * Provide a convenient way to start fragment wrapped in {@link SinglePaneActivity}.
     */
    public void startFragment(Class<? extends Fragment> fragmentCls) {
        startActivity(SinglePaneActivity.buildIntent(this, fragmentCls, null));
    }

    /******************************* 网络事件封装 *******************************/
    
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

        public HttpTask(HttpBuilder builder) {
            super(new HttpTaskExecutor(builder));
        }

        @Override
        protected void doExecuteTask() {
            executeOnExecutor(CONFIG.getHttpThreadPool());
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
     * 允许接收事件回调<br>
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
    
    private void unregisterEvent() {
        if (isReceiveEventEnabled)
        {
            EventObserver.getDefault().unregister(this);
        }
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
        unregisterEvent();
        super.onDestroy();
    }
}