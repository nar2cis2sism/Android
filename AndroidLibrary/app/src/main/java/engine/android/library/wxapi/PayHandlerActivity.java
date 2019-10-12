package engine.android.library.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.core.util.LogFactory.LOG;
import engine.android.util.Util;

public class PayHandlerActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, PayFunction.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        LOG.log("微信支付回调", Util.toString(baseResp));
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX)
        {
            EventBus.getDefault().post(new Event("WEIXIN_PAY", 0, baseResp.errCode == ErrCode.ERR_OK));
        }

        finish();
        // 防止界面闪动
        overridePendingTransition(0, 0);
    }
}
