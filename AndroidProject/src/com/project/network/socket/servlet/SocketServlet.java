package com.project.network.socket.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.util.GsonUtil;
import engine.android.util.io.IOUtil;
import protocol.java.ProtocolWrapper;
import protocol.java.ProtocolWrapper.ProtocolEntity;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;

public class SocketServlet implements engine.android.socket.SocketProxy.SocketServlet {

    @Override
    public void doServlet(InputStream in, OutputStream out) {
        try {
            ProtocolEntity entity = ProtocolWrapper.parse(in);
            if (entity == null)
            {
                throw new IOException("read bytes is -1.");
            }

            entity.parseBody();
            
            ProtocolData[] ack = process(entity.getCmd(), entity.getData());
            if (ack != null)
            {
                for (ProtocolData d : ack)
                {
                    entity = ProtocolEntity.newInstance(entity.getMsgId(), d);
                    entity.generateBody();
                    out.write(ProtocolWrapper.wrap(entity));
                }
            }
        } catch (Exception e) {
            LOG.log(e);
        }
    }
    
    private ProtocolData[] process(int cmd, ProtocolData data) throws Exception {
        Class<?> cls = data.getClass();
        String name = cls.getSimpleName();
        String json = new String(getFileContent(name));
        return (ProtocolData[]) GsonUtil.parseJson(json, cls);
    
    }
    
    private static byte[] getFileContent(String fileName) throws IOException {
        InputStream is = null;
        try {
            is = SocketServlet.class.getResourceAsStream(fileName);
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