package engine.android.library.wxapi;

import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import engine.android.core.ApplicationManager;
import engine.android.library.Library.Function;
import engine.android.library.wxapi.Pay.IN;

/**
 * 微信支付
 *
 * @author Daimon
 * @version N
 * @since 1/14/2019
 */
public class Pay {

    public static class IN {

        public String prepayId;             // 微信返回的支付交易会话ID
        public String nonceStr;             // 随机字符串
        public String timeStamp;            // 时间戳
        public String sign;                  // 签名
    }

    public static PayFunction FUNCTION() {
        return new PayFunction();
    }
}

class PayFunction implements Function<IN, Void> {

    public static final String APP_ID = "wxe735f79d7b24b0c9";
    public static final String MCH_ID = "1530453771";

    @Override
    public void doFunction(IN params, Callback<Void> callback) {
        IWXAPI api = WXAPIFactory.createWXAPI(ApplicationManager.getMainApplication(), null);
        // 将该app注册到微信
        api.registerApp(APP_ID);
        //
        if (!api.isWXAppInstalled())
        {
            Toast.makeText(ApplicationManager.getMainApplication(), "请下载并安装最新版微信", Toast.LENGTH_SHORT).show();
            return;
        }
        // 调起支付
        PayReq req = new PayReq();
        req.appId = APP_ID; // 微信开放平台审核通过的应用APPID
        req.partnerId = MCH_ID; // 微信支付分配的商户号
        req.prepayId = params.prepayId;
        req.packageValue = "Sign=WXPay"; // 固定值
        req.nonceStr = params.nonceStr;
        req.timeStamp = params.timeStamp;
        req.sign = params.sign;
        api.sendReq(req);
    }
}