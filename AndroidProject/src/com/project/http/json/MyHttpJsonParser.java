package com.project.http.json;

import com.project.bean.ErrorInfo;

import engine.android.framework.net.event.EventCallback;
import engine.android.framework.net.http.util.EntityUtil;
import engine.android.http.HttpResponse;
import engine.android.http.util.json.HttpJsonParser;

import org.json.JSONObject;

public class MyHttpJsonParser extends HttpJsonParser {
    
    private final String action;

    private final EventCallback callback;
    
    public MyHttpJsonParser(String action, EventCallback callback) {
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