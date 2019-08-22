package engine.android.socket;

import java.net.Socket;

/**
 * Socket联网事件监听器
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public interface SocketConnectionListener {

    /*** Socket 客户端一些常用设置 ***/
    // 客户端socket在接收数据时，有两种超时：1.连接服务器超时，即连接超时；2.连接服务器成功后，接收服务器数据超时，即接收超时
    // 设置socket读取数据流的超时时间
    // socket.setSoTimeout(5000);
    // 发送数据包，默认为false，即客户端发送数据采用Nagle算法；
    // 但是对于实时交互性高的程序，建议其改为true，即关闭Nagle算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
    // socket.setTcpNoDelay(true);
    // 设置客户端socket关闭时，close（）方法起作用时延迟1分钟关闭，如果1分钟内尽量将未发送的数据包发送出去
    // socket.setSoLinger(true, 60);
    // 设置输出流的发送缓冲区大小，默认是8KB，即8096字节
    // socket.setSendBufferSize(8096);
    // 设置输入流的接收缓冲区大小，默认是8KB，即8096字节
    // socket.setReceiveBufferSize(8096);
    // 作用：每隔一段时间检查服务器是否处于活动状态，如果服务器端长时间没响应，自动关闭客户端socket
    // 防止服务器端无效时，客户端长时间处于连接状态
    // socket.setKeepAlive(true);

    /*** Socket客户端向服务器端发送数据 ****/
    // 代表可以立即向服务器端发送单字节数据
    // socket.setOOBInline(true);
    // 数据不经过输出缓冲区，立即发送
    // socket.sendUrgentData(65);//"A"

    /**
     * 连接已建立
     */
    void onConnected(Socket socket);

    /**
     * 联网数据接收
     * 
     * @param data 数据包
     */
    void onReceive(Object data);

    /**
     * 联网发生错误
     * 
     * @param e 错误异常
     */
    void onError(Exception e);

    /**
     * 连接已断开
     */
    void onClosed();
}