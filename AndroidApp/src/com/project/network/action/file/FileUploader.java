package com.project.network.action.file;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.http.HttpConnector;
import engine.android.http.HttpRequest.FileEntity;
import engine.android.util.api.StringUtil;

import com.project.app.MySession;
import com.project.app.bean.ServerUrl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传
 * 
 * @author Daimon
 */
public abstract class FileUploader implements HttpBuilder {
    
    private static final int TIMEOUT = 20000;
    
    public final File file;          // 上传文件
    
    private Map<String, String> parameters;
    
    public FileUploader(File file) {
        this.file = file;
    }
    
    public void setParameter(String key, String value) {
        if (parameters == null) parameters = new HashMap<String, String>();
        parameters.put(key, value);
    }
    
    public void setAction(String action) {
        setParameter("action", action);
    }
    
    private String getAction() {
        if (parameters != null)
        {
            return parameters.get("action");
        }
        
        return null;
    }

    @Override
    public HttpConnector buildConnector(HttpConnectorBuilder builder) {
        HttpConnector conn = builder
        .setAction(getAction())
        .setUrl(StringUtil.appendQueryParameters(ServerUrl.getUploadServerUrl(), parameters))
        .setEntity(new FileEntity(file))
        .build()
        .setTimeout(TIMEOUT);
        conn.getRequest().setHeader("token", MySession.getToken());
        return conn;
    }
}