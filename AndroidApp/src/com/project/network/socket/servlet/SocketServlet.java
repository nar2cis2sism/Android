﻿package com.project.network.socket.servlet;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.util.GsonUtil;
import engine.android.util.file.FileManager;

import protocol.socket.ack.MessageACK;
import protocol.socket.ack.OfflineMessageACK;
import protocol.socket.req.Message;
import protocol.socket.req.OfflineMessage;
import protocol.util.ProtocolWrapper;
import protocol.util.ProtocolWrapper.ProtocolEntity;
import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            
            ProtocolData ack = new Process(out).process(entity.getCmd(), entity.getData());
            if (ack != null)
            {
                entity = ProtocolEntity.newInstance(entity.getMsgId(), ack);
                entity.generateBody();
                out.write(ProtocolWrapper.wrap(entity));
            }
            // 避免延迟
            out.flush();
        } catch (Exception e) {
            LOG.log(e);
        }
    }
    
    private static class Process {
        
        private final OutputStream out;
        
        public Process(OutputStream out) {
            this.out = out;
        }
        
        /**
         * 推送消息给客户端
         */
        public void push(ProtocolData data) throws Exception {
            ProtocolEntity entity = ProtocolEntity.newInstance(0, data);
            entity.generateBody();
            out.write(ProtocolWrapper.wrap(entity));
        }
        
        /**
         * 模拟服务器处理
         * 
         * @param cmd 收包指令码
         * @param data 收包数据
         * @return 响应客户端
         */
        public ProtocolData process(int cmd, ProtocolData data) throws Exception {
            if (data instanceof OfflineMessage)
            {
                return ack(OfflineMessageACK.class);
            }
            else if (data instanceof Message)
            {
                return processMessage((Message) data);
            }
            
            return null;
        }
        
        private ProtocolData ack(Class<? extends ProtocolData> ackCls) throws Exception {
            String json = new String(FileManager.readFile(getClass(), ackCls.getSimpleName()));
            return GsonUtil.parseJson(json, ackCls);
        }
        
        private ProtocolData processMessage(Message msg) throws Exception {
            // 消息返还给发送者，重新设置内容和时间
            msg.content = "收到：" + msg.content;
            msg.creationTime = System.currentTimeMillis();
            push(msg);
            
            return new MessageACK();
        }
    }
}