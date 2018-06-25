package com.project.network.socket.servlet;

import engine.android.core.util.LogFactory.LOG;
import protocol.java.ProtocolWrapper;
import protocol.java.ProtocolWrapper.ProtocolEntity;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;
import protocol.java.stream.ack.MessageACK;
import protocol.java.stream.req.Message;
import protocol.java.stream.req.Message.MessageBody;

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
            
            ProtocolData[] ack = new Process(out).process(entity.getCmd(), entity.getData());
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
         * @param push 推送给客户端
         * @return 响应客户端
         */
        public ProtocolData[] process(int cmd, ProtocolData data) throws Exception {
            if (data instanceof Message)
            {
                return processMessage((Message) data);
            }
            
            return null;
        }
        
        private ProtocolData[] processMessage(Message msg) throws Exception {
            // 消息返还给发送者
            String receiver = msg.to;
            msg.to = msg.from;
            msg.from = receiver;
            // 重新设置时间
            MessageBody[] body = msg.body;
            if (body != null)
            {
                for (MessageBody b : body)
                {
                    b.creationTime = System.currentTimeMillis();
                    b.content = "收到：" + b.content;
                }
            }
            
            push(msg);
            
            return new ProtocolData[]{ new MessageACK() };
        }
    }
}