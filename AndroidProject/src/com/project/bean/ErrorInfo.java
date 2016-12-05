package com.project.bean;

import org.json.JSONObject;

public class ErrorInfo {
    
    private final int code;                    // 错误编码
    
    private final String msg;                  // 错误原因描述
    
    private ErrorInfo(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public static ErrorInfo parse(JSONObject json) {
        int code = json.optInt("code");
        if (code == protocol.java.json.ErrorInfo.CODE_SUCCESS)
        {
            return null;
        }
        
        return new ErrorInfo(code, json.optString("msg"));
    }
    
    @Override
    public String toString() {
        return code + ":" + msg;
    }
}