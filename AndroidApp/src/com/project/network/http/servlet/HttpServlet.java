package com.project.network.http.servlet;

import android.os.SystemClock;

import com.project.network.NetworkConfig;

import engine.android.core.util.LogFactory.LOG;
import engine.android.http.HttpRequest;
import engine.android.http.HttpRequest.HttpEntity;
import engine.android.util.file.FileManager;

import org.json.JSONObject;

import protocol.java.EntityUtil;

import java.io.ByteArrayOutputStream;

public class HttpServlet implements engine.android.http.HttpProxy.HttpServlet {
    
    private static final int RESPONSE_DELAY   = 500; // (0.5s)

    @Override
    public void doServlet(HttpRequest req, HttpResponse resp) {
        long time = System.currentTimeMillis();
        
        if (NetworkConfig.HTTP_URL.equals(req.getUrl()))
        {
            try {
                HttpEntity entity = req.getEntity();
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int) entity.getContentLength());
                entity.writeTo(baos);
                String request = EntityUtil.toString(baos.toByteArray());
                resp.setEntity(EntityUtil.toByteArray(process(request)));
            } catch (Exception e) {
                LOG.log(e);
            }
        }
        
        time = System.currentTimeMillis() - time;
        if (time < RESPONSE_DELAY) SystemClock.sleep(RESPONSE_DELAY - time);
    }
    
    private String process(String request) throws Exception {
        JSONObject json = new JSONObject(request);
        String action = json.getString("action");

        return new String(FileManager.readFile(getClass(), action));
    }
}