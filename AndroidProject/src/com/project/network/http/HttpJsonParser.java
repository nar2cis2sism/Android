package com.project.network.http;

import com.project.app.bean.ErrorInfo;

import engine.android.framework.network.event.EventObserver.EventCallback;
import engine.android.framework.network.http.EntityUtil;
import engine.android.http.HttpResponse;

import org.json.JSONObject;

public class HttpJsonParser extends engine.android.http.util.json.HttpJsonParser {
    
    private final String action;

    private final EventCallback callback;
    
    public HttpJsonParser(String action, EventCallback callback) {
        this.action = action;
        this.callback = callback;
    }
    
    @Override
    public void parse(HttpResponse response) throws Exception {
        parse(new JSONObject(EntityUtil.toString(response.getContent())));
    }

    @Override
    protected void parse(JSONObject json) throws Exception {
        ErrorInfo error = ErrorInfo.parse(json);
        if (error == null)
        {
            // 成功
            JSONObject data = json.optJSONObject("data");
            Object param = null;
            if (data != null)
            {
                param = process(data);
            }
            
            callback.call(action, EventCallback.SUCCESS, param);
        }
        else
        {
            // 失败
            callback.call(action, EventCallback.FAIL, error);
        }
    }
    
    protected Object process(JSONObject data) {
        return null;
    }
}