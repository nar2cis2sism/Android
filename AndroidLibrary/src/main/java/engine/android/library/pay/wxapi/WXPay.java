package engine.android.library.pay.wxapi;

import static engine.android.core.ApplicationManager.getMainApplication;

import engine.android.library.Library.Function;
import engine.android.library.pay.Pay;
import engine.android.library.pay.Pay.IN;

import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信支付
 *
 * @author Daimon
 * @since 1/14/2019
 */
public class WXPay extends Pay {

    public static PayFunction FUNCTION() {
        return new PayFunction();
    }
}

class PayFunction implements Function<IN, Void> {

    public static final String APP_ID = "wxe735f79d7b24b0c9";
    public static final String MCH_ID = "1530453771";

    @Override
    public void doFunction(IN params, Callback<Void> callback) {
        IWXAPI api = WXAPIFactory.createWXAPI(getMainApplication(), APP_ID);
        if (!api.isWXAppInstalled()) {
            Toast.makeText(getMainApplication(), "请下载并安装最新版微信", Toast.LENGTH_SHORT).show();
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