package com.project.network.http.servlet;

import engine.android.core.util.LogFactory.LOG;
import engine.android.http.HttpRequest;
import engine.android.http.HttpRequest.HttpEntity;
import engine.android.util.file.FileManager;

import android.os.SystemClock;

import com.project.app.MySession;
import com.project.app.bean.ServerUrl;
import com.project.network.NetworkConfig;

import org.json.JSONObject;

import protocol.util.EntityUtil;

import java.io.ByteArrayOutputStream;

public class HttpServlet implements engine.android.http.HttpProxy.HttpServlet {
    
    private static final int RESPONSE_DELAY = 100; // (0.1s)

    @Override
    public void doServlet(HttpRequest req, HttpResponse resp) {
        long time = System.currentTimeMillis();
        try {
            String url = req.getUrl();
            if (NetworkConfig.HTTP_URL.equals(url))
            {
                HttpEntity entity = req.getEntity();
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int) entity.getContentLength());
                entity.writeTo(baos);
                String request = EntityUtil.toString(baos.toByteArray());
                resp.setEntity(EntityUtil.toByteArray(process(request)));
            }
            else if (url.startsWith(ServerUrl.getUploadServerUrl()))
            {
                // 文件上传
                if (url.contains("action=avatar"))
                {
                    // 上传头像
                    resp.setHeader("crc", String.valueOf(MySession.getUser().avatar_ver + 1));
                }
            }
        } catch (Exception e) {
            LOG.log(e);
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