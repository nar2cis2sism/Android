package engine.android.plugin.proxy;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import engine.android.plugin.PluginEnvironment;
import engine.android.util.ReflectObject;

public class PluginHandlerCallback implements Handler.Callback {
    
    private final PluginEnvironment environment;
    
    public PluginHandlerCallback(PluginEnvironment environment) {
        this.environment = environment;
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case /* LAUNCH_ACTIVITY */100:
                    handleLaunchActivity(new ReflectObject(/* ActivityClientRecord */msg.obj));
                    break;
            }
        } catch (Exception e) {
            PluginEnvironment.log(e);
        }
        
        return false;
    }
    
    private void handleLaunchActivity(ReflectObject ActivityClientRecordRef) throws Exception {
        Intent intent = (Intent) ActivityClientRecordRef.get("intent");
        ComponentName component = environment.handleIntent(intent);
        if (component == null)
        {
            return;
        }
        
        ActivityInfo activityInfo = environment.resolveActivity(component);
        if (activityInfo == null)
        {
            throw new Exception("Plugin Activity is not registered:" + component.getClassName());
        }
        
        // 替换成真实的Activity进行启动
        ActivityClientRecordRef.set("activityInfo", activityInfo);
    }
}