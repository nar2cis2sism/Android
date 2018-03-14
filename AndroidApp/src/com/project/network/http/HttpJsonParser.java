package com.project.network.http;

import com.project.app.bean.ErrorInfo;

import engine.android.framework.network.http.util.EntityUtil;
import engine.android.http.HttpResponse;

import org.json.JSONObject;

public class HttpJsonParser extends engine.android.http.util.json.HttpJsonParser {
    
    @Override
    public Object parse(HttpResponse response) throws Exception {
        return parse(new JSONObject(EntityUtil.toString(response.getContent())));
    }

    @Override
    protected Object parse(JSONObject json) throws Exception {
        ErrorInfo error = ErrorInfo.parse(json);
        if (error != null) return error;
        
        JSONObject data = json.optJSONObject("data");
        return data != null ? process(data) : null;
    }
    
    protected Object process(JSONObject data) throws Exception {
        return null;
    }
}