package engine.android.framework.ui.extra;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
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
 * @version N
 * @since 6/6/2014
 */
public class SinglePaneActivity extends BaseActivity {
    
    private static final String EXTRA_FRAGMENT_CLASS_NAME = "fragmentClassName";
    
    private static final int CONTENT_ID = R.id.single_pane_content;
    
    /**
     * 通过此方法构造出Intent启动
     * 
     * @param fragmentCls Fragment类名
     * @param args Fragment参数
     */
    public static final Intent buildIntent(Context context, Class<? extends Fragment> fragmentCls, 
            Bundle args) {
        Intent intent = new Intent(context, SinglePaneActivity.class)
        .putExtra(EXTRA_FRAGMENT_CLASS_NAME, fragmentCls.getName());
        
        if (args != null)
        {
            intent.putExtras(args);
        }
        
        return intent;
    }

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
            Fragment fragment = parseIntent(getIntent());
            if (fragment == null)
            {
                fragment = onCreateFragment();
            }
            
            if (fragment != null)
            {
                getFragmentManager().beginTransaction()
                .add(CONTENT_ID, fragment)
                .commit();
                
                getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
                    
                    @Override
                    public void onBackStackChanged() {
                        getContentFragment().setMenuVisibility(true);
                    }
                });
            }
        }
    }

    private Fragment parseIntent(Intent intent) {
        if (intent != null)
        {
            Bundle args = intent.getExtras();
            if (args != null)
            {
                String fragmentClassName = args.getString(EXTRA_FRAGMENT_CLASS_NAME);
                if (fragmentClassName != null)
                {
                    args.remove(EXTRA_FRAGMENT_CLASS_NAME);
                    return Fragment.instantiate(this, fragmentClassName, args);
                }
            }
        }
        
        return null;
    }
    
    /**
     * You can assign through it when intent has no parameter of fragment.
     */
    protected Fragment onCreateFragment() {
        return null;
    }

    public Fragment getContentFragment() {
        return getFragmentManager().findFragmentById(CONTENT_ID);
    }
    
    public void addFragment(final Fragment fragment) {
        commitFragmentTransaction(new FragmentTransaction() {
            
            @Override
            public void commit(FragmentManager fragmentManager) {
                fragmentManager.beginTransaction()
                .hide(getContentFragment())
                .add(CONTENT_ID, fragment)
                .addToBackStack(null)
                .commit();
            }
        });
    }
    
    public void replaceFragment(final Fragment fragment) {
        commitFragmentTransaction(new FragmentTransaction() {
            
            @Override
            public void commit(FragmentManager fragmentManager) {
                fragmentManager.beginTransaction()
                .replace(CONTENT_ID, fragment)
                .commit();
            }
        });
    }
    
    /**
     * 回退事件监听器（用于处理Fragment回退事件）
     */
    public interface OnBackListener {
        
        boolean onBackPressed();
    }
    
    @Override
    public void onBackPressed() {
        Fragment fragment = getContentFragment();
        if (fragment instanceof OnBackListener
        && ((OnBackListener) fragment).onBackPressed())
        {
            return;
        }
        
        super.onBackPressed();
    }
}