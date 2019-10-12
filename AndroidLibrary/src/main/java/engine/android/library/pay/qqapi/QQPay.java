package engine.android.library.pay.qqapi;

import static engine.android.core.ApplicationManager.getMainApplication;

import engine.android.core.util.LogFactory.LOG;
import engine.android.library.Library.Function;
import engine.android.library.pay.Pay.IN;

import android.util.Base64;
import android.widget.Toast;

import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.data.pay.PayApi;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * QQ支付
 *
 * @author Daimon
 * @since 1/14/2019
 */
public class QQPay {

    public static PayFunction FUNCTION() {
        return new PayFunction();
    }
}

class PayFunction implements Function<IN, Void> {

    public static final String APP_ID = "1108009380";
    public static final String MCH_ID = "1529877991";
    public static final String SIGN_KEY = "8ZAHBcNyProIyGDU";

    @Override
    public void doFunction(IN params, final Callback<Void> callback) {
        IOpenApi openApi = OpenApiFactory.getInstance(getMainApplication(), APP_ID);
        if (!openApi.isMobileQQInstalled()) {
            Toast.makeText(getMainApplication(), "请下载并安装最新版QQ", Toast.LENGTH_SHORT).show();
            return;
        }
        // 调起支付
        PayApi api = new PayApi();
        api.appId = APP_ID; // 腾讯开放平台或QQ互联平台审核通过的应用AppID
        api.bargainorId = MCH_ID; // QQ钱包分配的商户号
        api.serialNumber = params.prepayId; // 支付序号,用于标识此次支付
        api.callbackScheme = "qwallet_daimon"; // QQ钱包支付结果回调给urlscheme为callbackScheme的activity
        api.tokenId = params.prepayId;
        api.pubAcc = ""; // 手Q公众帐号，暂时未对外开放申请
        api.pubAccHint = ""; // 支付完成页面，展示给用户的提示语：提醒关注公众帐号
        api.nonce = params.nonceStr;
        api.timeStamp = Long.parseLong(params.timeStamp);
        api.sig = params.sign;
        signApi(api);
        api.sigType = "HMAC-SHA1"; // 签名时，使用的加密方式，默认为"HMAC-SHA1"
        openApi.execApi(api);
    }

    /**
     * 签名步骤建议不要在app上执行，要放在服务器上执行.
     */
    private void signApi(PayApi api) {
        try {
            // 按key排序
            StringBuilder sb = new StringBuilder();
            sb.append("appId=").append(api.appId);
            sb.append("&bargainorId=").append(api.bargainorId);
            sb.append("&nonce=").append(api.nonce);
            sb.append("&pubAcc=").append(api.pubAcc);
            sb.append("&tokenId=").append(api.tokenId);

            byte[] byteKey = (SIGN_KEY + "&").getBytes("UTF-8");
            // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(byteKey, "HmacSHA1");
            // 生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance("HmacSHA1");
            // 用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            byte[] byteSrc = sb.toString().getBytes("UTF-8");
            // 完成 Mac 操作
            byte[] dst = mac.doFinal(byteSrc);
            // Base64
            api.sig = Base64.encodeToString(dst, Base64.NO_WRAP);
        } catch (Exception e) {
            LOG.log(e);
        }
    }
}