package engine.android.library.mob;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import engine.android.framework.util.GsonUtil;
import engine.android.library.Library.Function;
import engine.android.library.mob.Authorize.IN;
import engine.android.library.mob.Authorize.OUT;

/**
 * 三方登录
 *
 * @author Daimon
 * @version N
 * @since 1/14/2019
 */
public class Authorize {

    public static final String platform_Wechat = Wechat.NAME;
    public static final String platform_WechatMoments = WechatMoments.NAME;
    public static final String platform_QQ = QQ.NAME;
    public static final String platform_QZone = QZone.NAME;

    public static class IN {

        public String platform;             // 三方平台名称
    }

    public static class OUT {

        public boolean notInstalled;    // 客户端未安装

        public String userID;               // 用户在平台上的身份标识
        public String nickname;             // 用户在平台上的昵称
        public String icon;                 // 用户在平台上的头像地址
    }

    public static AuthorizeFunction FUNCTION() {
        return new AuthorizeFunction();
    }
}

class AuthorizeFunction implements Function<IN, OUT> {

    @Override
    public void doFunction(IN params, final Callback<OUT> callback) {
        Platform platform = ShareSDK.getPlatform(params.platform);
        if (!platform.isClientValid())
        {
            OUT out = new OUT();
            out.notInstalled = true;
            callback.doResult(out);
            return;
        }

        platform.removeAccount(true);
        // 授权回调监听
        platform.setPlatformActionListener(new PlatformActionListener() {

            // 回调信息，可以在这里获取基本的授权返回的信息，但是注意如果做提示和UI操作要传到主线程handler里去执行
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                callback.doResult(GsonUtil.parseJson(platform.getDb().exportData(), OUT.class));
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                callback.doError(new Exception("取消授权"));
            }
        });

        platform.showUser(null); // 要数据不要功能，主要体现在不会重复出现授权界面
    }
}