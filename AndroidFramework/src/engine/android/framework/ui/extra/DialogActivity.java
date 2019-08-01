package engine.android.framework.ui.extra;

import android.os.Bundle;
import android.view.WindowManager;

/**
 * 提供一个对话框活动界面（需要在manifest中应用对话框主题Theme_Dialog）
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class DialogActivity extends SinglePaneActivity {
    
    private int enterAnim, exitAnim;
    
    /**
     * 可设置界面参数
     */
    protected void initParams(WindowManager.LayoutParams params) {}
    
    /**
     * 可设置出入动画
     */
    protected final void setAnimation(int enterAnim, int exitAnim) {
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WindowManager.LayoutParams params = getWindow().getAttributes();
        initParams(params);
        getWindow().setAttributes(params);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        overridePendingTransition(enterAnim, exitAnim);
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(enterAnim, exitAnim);
    }
    
    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        if (enterAnim == 0 && exitAnim == 0)
        {
            return;
        }
        
        super.overridePendingTransition(enterAnim, exitAnim);
    }
}