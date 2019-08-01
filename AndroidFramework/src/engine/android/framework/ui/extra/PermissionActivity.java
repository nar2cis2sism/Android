package engine.android.framework.ui.extra;

import android.os.Bundle;

import engine.android.framework.util.PermissionUtil;
import engine.android.framework.util.PermissionUtil.PermissionCallback;

/**
 * 权限申请界面
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class PermissionActivity extends SinglePaneActivity implements PermissionCallback {
    
    private PermissionUtil permission;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permission = new PermissionUtil(this);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (permission.onRequestPermissionsResult(grantResults))
        {
            onRequestPermissionsSuccess();
        }
        else
        {
            onRequestPermissionsFailure();
        }
    }
    
    protected void requestPermission(String... permissions) {
        permission.requestPermission(permissions);
    }
    
    protected void onRequestPermissionsSuccess() {}
    
    protected void onRequestPermissionsFailure() {
        permission.showTipDialog();
    }
}