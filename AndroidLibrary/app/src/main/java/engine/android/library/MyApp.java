package engine.android.library;

import android.app.Application;

import com.mob.MobSDK;

import java.io.File;

import engine.android.core.ApplicationManager;
import engine.android.library.mob.AuthorizeAction;
import engine.android.library.mob.ShareAction;
import engine.android.plugin.Plugin;
import engine.android.plugin.share.Authorize;

public class MyApp extends Application {

    public static Plugin plugin;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) mockPlugin();
        initMob();
    }

    /**
     * 模仿宿主加载插件
     */
    private void mockPlugin() {
        new ApplicationManager().init(this);
        Plugin.init();
        plugin = Plugin.loadPluginFromFile(new File(getPackageCodePath()));
    }

    private void initMob() {
        MobSDK.init(this);
        Plugin.registerAction(this, Authorize.ACTION, new AuthorizeAction());
        Plugin.registerAction(this, "share", new ShareAction());
    }
}