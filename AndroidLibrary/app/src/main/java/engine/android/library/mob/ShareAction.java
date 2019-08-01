package engine.android.library.mob;

import android.graphics.Bitmap;

import com.mob.MobSDK;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.library.MyApp;
import engine.android.plugin.Plugin;
import engine.android.plugin.share.Authorize.IN;
import engine.android.plugin.share.Authorize.OUT;

public class ShareAction implements Plugin.Action<IN, OUT> {

    @Override
    public void doAction(IN in, final Plugin.Callback<OUT> callback) {
        final OUT out = new OUT();

        OnekeyShare oks = new OnekeyShare();
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();
        // 隐藏QQ分享
//        oks.addHiddenPlatform(QQ.NAME);
        // 对于微信好友必须在分享完成之后的弹出框选择返回APP，才能获取成功回调，否则点击留在微信则不能。
        // 对于微信朋友圈如果分享成功则会直接回调APP，执行到成功的回调
        // 总结：没什么卵用
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//                showText("分享完成");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
//                showText(LogFactory.LogUtil.getExceptionInfo(throwable));
            }

            @Override
            public void onCancel(Platform platform, int i) {
                // 微信客户端版本从6.7.2以上开始，取消分享提示分享成功；即取消分享和分享成功都返回成功事件
            }
        });

        // title标题，微信、QQ和QQ空间等平台使用
        oks.setTitle("帮好友答题，助TA赢道具");
        // titleUrl QQ和QQ空间跳转链接
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("在玩同学战，你敢来挑战吗？");
        // 两个一起设置可显示注册在平台上的LOGO图片
        oks.setImagePath(" ");
        oks.setImageData(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));
        // url在微信、微博，Facebook等平台中使用
        oks.setUrl("http://sharesdk.cn");
        // 启动分享GUI
        oks.show(MobSDK.getContext());
    }
}
