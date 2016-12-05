package engine.android.framework.ui.extra;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import engine.android.framework.R;
import engine.android.framework.ui.BaseActivity;

/**
 * 提供一个通用界面（显示单个Fragment）
 * 
 * @author Daimon
 */
public class SinglePaneActivity extends BaseActivity {
    
    private static final String EXTRA_FRAGMENT_CLASS_NAME = "fragmentClassName";
    
    private static final int CONTENT_ID = R.id.single_pane_content;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        View content = new FrameLayout(this);
        content.setId(CONTENT_ID);
        setContentView(content, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        if (savedInstanceState == null)
        {
            // initial setup
            Bundle args = getIntent().getExtras();
            if (args == null)
            {
                return;
            }
            
            String fragmentClassName = args.getString(EXTRA_FRAGMENT_CLASS_NAME);
            if (fragmentClassName == null)
            {
                return;
            }
            
            args.remove(EXTRA_FRAGMENT_CLASS_NAME);
            
            getFragmentManager().beginTransaction()
            .add(CONTENT_ID, Fragment.instantiate(this, fragmentClassName, args))
            .commit();
        }
    }

    /**
     * 通过此方法构造出Intent才能启动
     * @param fragmentCls Fragment类名
     * @param args Fragment参数
     */
    public static final Intent buildIntent(Context context, Class<?> fragmentCls, 
            Bundle args) {
        Intent intent = new Intent(context, SinglePaneActivity.class)
        .putExtra(EXTRA_FRAGMENT_CLASS_NAME, fragmentCls.getName());
        
        if (args != null)
        {
            intent.putExtras(args);
        }
        
        return intent;
    }
    
    private void replaceFragment(FragmentManager fragmentManager, 
            Fragment fragment, boolean addToBackStack) {
        android.app.FragmentTransaction transaction = 
                fragmentManager.beginTransaction()
                .replace(CONTENT_ID, fragment);
        if (addToBackStack)
        {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
    }
    
    public void addFragment(final Fragment fragment) {
        commitFragmentTransaction(new FragmentTransaction() {
            
            @Override
            public void commit(FragmentManager fragmentManager) {
                replaceFragment(fragmentManager, fragment, true);
            }
        });
    }
    
    public void replaceFragment(final Fragment fragment) {
        commitFragmentTransaction(new FragmentTransaction() {
            
            @Override
            public void commit(FragmentManager fragmentManager) {
                replaceFragment(fragmentManager, fragment, false);
            }
        });
    }
}