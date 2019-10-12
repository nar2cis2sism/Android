package engine.android.library.mob;

import static engine.android.core.ApplicationManager.getMainApplication;
import static engine.android.library.mob.Platform.PLATFORM_QQ;
import static engine.android.library.mob.Platform.PLATFORM_QZone;

import engine.android.library.Library.Function;
import engine.android.library.mob.Share.IN;

import android.graphics.Bitmap;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 三方分享
 *
 * @author Daimon
 * @since 1/14/2019
 */
public class Share implements engine.android.library.mob.Platform {

    public static class IN {

        /** 三方平台名称[不设置表示一键分享] */
        public String platform;

        /** 分享图片[设置后忽略其他参数] */
        public File image;
        /** 分享标题 */
        public String title;
        /** 分享文本 */
        public String text;
        /** 分享链接 */
        public String url;
        /** 分享LOGO */
        public Bitmap logo;
    }

    public static ShareFunction FUNCTION() {
        return new ShareFunction();
    }
}

class ShareFunction implements Function<IN, Void> {

    @Override
    public void doFunction(IN params, Callback<Void> callback) {
        Platform platform = ShareSDK.getPlatform(params.platform);
        if (!platform.isClientValid())
        {
            String platformName = platform.getName();
            if (PLATFORM_QQ.equals(platformName) || PLATFORM_QZone.equals(platformName))
            {
                platformName = "QQ";
            }
            else
            {
                platformName = "微信";
            }

            String tip = String.format("%s版本过低或者没有安装，需要升级或安装%s才能使用", platformName, platformName);
            Toast.makeText(getMainApplication(), tip, Toast.LENGTH_SHORT).show();
            return;
        }

        OnekeyShare oks = new OnekeyShare();
        oks.setPlatform(params.platform);
        // 覆盖回调提示语
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {}

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {}

            @Override
            public void onCancel(Platform platform, int i) {}
        });
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();
        if (params.image != null)
        {
            // 分享图片
            oks.setImagePath(params.image.getAbsolutePath());
        }
        else
        {
            // title标题，微信、QQ和QQ空间等平台使用
            oks.setTitle(params.title);
            // text是分享文本，所有平台都需要这个字段
            oks.setText(params.text);
            // titleUrl QQ和QQ空间跳转链接
            oks.setTitleUrl(params.url);
            // url在微信、微博，Facebook等平台中使用
            oks.setUrl(params.url);
            oks.setImageData(params.logo);
        }
        // 启动分享GUI
        oks.show(getMainApplication());
    }
}