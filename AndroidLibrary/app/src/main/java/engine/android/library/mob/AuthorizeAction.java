package engine.android.library.mob;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import engine.android.plugin.Plugin;
import engine.android.plugin.share.Authorize.IN;
import engine.android.plugin.share.Authorize.OUT;

public class AuthorizeAction implements Plugin.Action<IN, OUT> {

    @Override
    public void doAction(IN in, final Plugin.Callback<OUT> callback) {
        final OUT out = new OUT();

        Platform platform = ShareSDK.getPlatform(in.name);
        platform.SSOSetting(in.sso);
        if (in.removeAccount) platform.removeAccount(true);

        if (in.validClient && platform.isClientValid()) {
            out.isClientValid = true;
            callback.doResult(out);
            return;
        }

        if (in.validAuth && platform.isAuthValid()) {
            out.isAuthValid = true;
            out.platformDB = platform.getDb().exportData();
            callback.doResult(out);
            return;
        }

        // 授权回调监听
        platform.setPlatformActionListener(new PlatformActionListener() {

            // 回调信息，可以在这里获取基本的授权返回的信息，但是注意如果做提示和UI操作要传到主线程handler里去执行
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                out.platformDB = platform.getDb().exportData();
                out.originResult = hashMap;
                callback.doResult(out);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                out.cancel = true;
                callback.doResult(out);
            }
        });

        if (in.getUserInfo) {
            platform.showUser(null);
        } else {
            platform.authorize();
        }
    }
}
