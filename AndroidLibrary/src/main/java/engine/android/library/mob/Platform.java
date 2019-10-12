package engine.android.library.mob;

import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

interface Platform {

    String PLATFORM_Wechat = Wechat.NAME;
    String PLATFORM_WechatMoments = WechatMoments.NAME;
    String PLATFORM_QQ = QQ.NAME;
    String PLATFORM_QZone = QZone.NAME;
}
