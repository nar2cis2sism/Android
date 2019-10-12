package engine.android.library.pay.qqapi;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.app.event.Events;
import engine.android.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.IOpenApiListener;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.data.base.BaseResponse;
import com.tencent.mobileqq.openpay.data.pay.PayResponse;

public class PayHandlerActivity extends Activity implements IOpenApiListener {

    private IOpenApi openApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openApi = OpenApiFactory.getInstance(this, PayFunction.APP_ID);
        openApi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openApi.handleIntent(intent, this);
    }

    @Override
    public void onOpenResponse(BaseResponse baseResponse) {
        LOG.log("QQ支付回调", Util.toString(baseResponse));
        if (baseResponse instanceof PayResponse)
        {
            Events.notifyPayCallback(Events.PAY_QQ, baseResponse.isSuccess());
        }

        finish();
        // 防止界面闪动
        overridePendingTransition(0, 0);
    }
}
