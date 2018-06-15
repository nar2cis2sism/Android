package com.project.network.http.servlet;

import android.os.SystemClock;

import engine.android.core.util.LogFactory.LOG;
import engine.android.http.HttpRequest;
import engine.android.http.HttpRequest.HttpEntity;
import engine.android.util.io.IOUtil;

import org.json.JSONObject;

import protocol.java.EntityUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpServlet implements engine.android.http.HttpProxy.HttpServlet {
    
    private static final int RESPONSE_DELAY   = 500; // (0.5s)

    @Override
    public void doServlet(HttpRequest req, HttpResponse resp) {
        long time = System.currentTimeMillis();
        try {
            HttpEntity entity = req.getEntity();
            long length = entity.getContentLength();
            ByteArrayOutputStream baos = length > 0 ? 
                    new ByteArrayOutputStream((int) length) : new ByteArrayOutputStream();
            entity.writeTo(baos);
            String request = EntityUtil.toString(baos.toByteArray());
            resp.setEntity(EntityUtil.toByteArray(process(req.getUrl(), request)));
        } catch (Exception e) {
            LOG.log(e);
        }
        
        time = System.currentTimeMillis() - time;
        if (time < RESPONSE_DELAY) SystemClock.sleep(RESPONSE_DELAY - time);
    }
    
    private String process(String url, String request) throws Exception {
        JSONObject json = new JSONObject(request);
        String action = json.getString("action");

        return new String(getFileContent(action));
    }
    
    private static byte[] getFileContent(String fileName) throws IOException {
        InputStream is = null;
        try {
            is = HttpServlet.class.getResourceAsStream(fileName);
            if (is == null)
            {
                throw new IOException("No resource:" + fileName);
            }
            
            return IOUtil.readStream(is);
        } finally {
            if (is != null) is.close();
        }
    }
}