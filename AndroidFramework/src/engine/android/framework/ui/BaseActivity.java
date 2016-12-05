package engine.android.framework.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import engine.android.framework.R;
import engine.android.widget.TitleBar;

import java.util.LinkedList;

public class BaseActivity extends BaseNetActivity {
    
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
    
    public static interface OnBackListener {
        
        public boolean onBackPressed();
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
        
        goBack();
    }
    
    protected final void goBack() {
        super.onBackPressed();
    }
}