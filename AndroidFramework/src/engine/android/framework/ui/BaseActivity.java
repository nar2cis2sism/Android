package engine.android.framework.ui;

import engine.android.core.Forelet;
import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.framework.R;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.http.HttpConnector;
import engine.android.util.os.PermissionUtil;
import engine.android.util.os.PermissionUtil.PermissionCallback;
import engine.android.util.os.WindowUtil;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.TitleBar;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BaseActivity extends NetworkActivity implements PermissionCallback {
    
    private LinearLayout root;
    
    private View status_bar;
    
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
        title_bar.setUpListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onHomeUpPressed();
            }
        });
    }
    
    public final TitleBar getTitleBar() {
        return title_bar;
    }

    /**
     * PS：状态栏随标题栏变化
     */
    @SuppressWarnings("deprecation")
    public void apply沉浸式状态栏(boolean 深色字体) {
        if (status_bar == null & WindowUtil.沉浸式状态栏(getWindow(), 深色字体))
        {
            status_bar = new View(this);
            status_bar.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, WindowUtil.getStatusBarHeight(getResources())));
            root.addView(status_bar, 0);
        }

        if (status_bar != null)
        {
            status_bar.setBackgroundDrawable(title_bar.getBackground());
            status_bar.setVisibility(title_bar.getVisibility());
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, root, false));
    }

    @Override
    public void setContentView(View view) {
        root.removeAllViewsInLayout();
        
        if (status_bar != null) root.addView(status_bar);
        root.addView(title_bar);
        root.addView(view);
        
        super.setContentView(root);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        view.setLayoutParams(params);
        setContentView(view);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            View focus = getCurrentFocus();
            if (focus != null)
            {
                UIUtil.hideSoftInput(focus);
            }
        }

        return super.onTouchEvent(event);
    }

    /******************************* 应用权限 *******************************/

    public interface PermissionCallback {

        void onGrant(PermissionUtil permission, boolean success);
    }

    private PermissionUtil permission;
    private SparseArray<PermissionCallback> callback;

    /**
     * 申请权限
     */
    public void requestPermission(PermissionCallback call, String... permissions) {
        int requestCode = call == null ? 0 : call.hashCode();
        if (callback == null) callback = new SparseArray<PermissionCallback>();
        callback.append(requestCode, call);

        if (permission == null) permission = new PermissionUtil(this);
        permission.requestPermission(requestCode, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionCallback call = callback.get(requestCode);
        if (call != null) call.onGrant(permission, permission.onRequestPermissionsResult(grantResults));
    }

    /******************************* EventBus *******************************/
    
    private EventHandler handler;

    /**
     * 注册事件处理器
     */
    protected EventHandler registerEventHandler() {
        return null;
    }

    @Override
    public void onAttachedToWindow() {
        if ((handler = registerEventHandler()) != null)
        {
            registerEventHandler(handler, this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        if (handler != null) EventBus.getDefault().unregister(handler);
        super.onDetachedFromWindow();
    }
    
    public static abstract class EventHandler implements EventBus.EventHandler {
        
        private final String[] events;
        
        private BaseActivity baseActivity;
        
        public EventHandler(String... events) {
            this.events = events;
        }
    
        @Override
        public void handleEvent(Event event) {
            onReceive(event.action, event.status, event.param);
        }
        
        protected void onReceive(String action, int status, Object param) {
            if (status == 0)
            {
                onReceiveSuccess(action, param);
            }
            else
            {
                onReceiveFailure(action, status, param);
            }
        }
        
        protected abstract void onReceiveSuccess(String action, Object param);
        
        protected void onReceiveFailure(String action, int status, Object param) {
            if (baseActivity != null)
            {
                baseActivity.hideProgress();
                Toast.makeText(baseActivity, String.valueOf(param), Toast.LENGTH_SHORT).show();
            }
        }
        
        protected final void hideProgress() {
            if (baseActivity != null) baseActivity.hideProgress();
        }
    }

    public static void registerEventHandler(EventHandler handler, BaseActivity activity) {
        if (handler != null)
        {
            String[] events = handler.events;
            if (events != null && events.length > 0)
            {
                handler.baseActivity = activity;
                for (String event : events)
                {
                    EventBus.getDefault().register(event, handler);
                }
            }
        }
    }
}

/**
 * 网络事件封装
 */
class NetworkActivity extends Forelet {
    
    private AppGlobal app;
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        app = AppGlobal.get(newBase);
    }
    
    /**
     * 检查网络状态
     * 
     * @param showTip 网络不可用时是否提示用户
     */
    public boolean checkNetworkStatus(boolean showTip) {
        if (app.getConfig().isOffline() || HttpConnector.isAccessible(this))
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
        executeTask(new NetworkTask(app.getHttpManager(), builder), false);
    }
    
    public void sendSocketRequest(SocketBuilder builder) {
        executeTask(new NetworkTask(app.getSocketManager(), builder), false);
    }
    
    /**
     * 网络请求
     */
    private static class NetworkTask extends Task {

        public NetworkTask(HttpManager http, HttpBuilder builder) {
            super(new HttpTaskExecutor(http, builder));
        }
        
        public NetworkTask(SocketManager socket, SocketBuilder builder) {
            super(new SocketTaskExecutor(socket, builder));
        }
        
        @Override
        protected void doExecuteTask() {
            // 由于请求已经是异步操作，这里不执行任务
        }
        
        private static class HttpTaskExecutor implements TaskExecutor {
            
            private final HttpManager http;
            
            private final int id;
            
            public HttpTaskExecutor(HttpManager http, HttpBuilder builder) {
                id = (this.http = http).sendHttpRequest(builder);
            }

            @Override
            public Object doExecute() { return null; }

            @Override
            public void cancel() {
                http.cancelHttpRequest(id);
            }
        }
        
        private static class SocketTaskExecutor implements TaskExecutor {
            
            private final SocketManager socket;
            
            private final int id;
            
            public SocketTaskExecutor(SocketManager socket, SocketBuilder builder) {
                id = (this.socket = socket).sendSocketRequest(builder);
            }

            @Override
            public Object doExecute() { return null; }

            @Override
            public void cancel() {
                socket.cancelSocketRequest(id);
            }
        }
    }
}