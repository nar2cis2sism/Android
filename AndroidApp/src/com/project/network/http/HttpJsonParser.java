package com.project.network.http;

import engine.android.http.HttpResponse;

import android.text.TextUtils;

import com.project.app.bean.ErrorInfo;

import org.json.JSONObject;

import protocol.util.EntityUtil;

public class HttpJsonParser extends engine.android.http.util.json.HttpJsonParser {
    
    @Override
    public Object parse(HttpResponse response) throws Exception {
        return parse(new JSONObject(EntityUtil.toString(response.getContent())));
    }

    @Override
    protected Object parse(JSONObject json) throws Exception {
        ErrorInfo error = ErrorInfo.parse(json);
        if (error != null) return error;
        
        String data = json.optString("data");
        return TextUtils.isEmpty(data) ? null : process(data);
    }
    
    protected Object process(String data) throws Exception {
        return null;
    }
}