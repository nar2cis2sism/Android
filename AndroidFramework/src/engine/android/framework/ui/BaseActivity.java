package engine.android.framework.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import engine.android.core.Forelet;
import engine.android.framework.R;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.http.HttpConnector;
import engine.android.widget.component.TitleBar;

public class BaseActivity extends NetworkActivity {
    
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
        title_bar.setUpListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onBackPressed();
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

    /**
     * Provide a convenient way to start fragment wrapped in {@link SinglePaneActivity}
     * (需要在Manifest中注册)
     */
    public void startFragment(Class<? extends Fragment> fragmentCls) {
        startFragment(fragmentCls, null);
    }
    
    /**
     * Provide a convenient way to start fragment wrapped in {@link SinglePaneActivity}
     * (需要在Manifest中注册)
     */
    public void startFragment(Class<? extends Fragment> fragmentCls, Bundle args) {
        startActivity(SinglePaneActivity.buildIntent(this, fragmentCls, args));
    }
    
    public final TitleBar getTitleBar() {
        return title_bar;
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