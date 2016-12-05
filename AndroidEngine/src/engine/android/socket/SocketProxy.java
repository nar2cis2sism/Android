package engine.android.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Socket代理（单机测试用）
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class SocketProxy {

    private final ExecutorService threadPool
    = Executors.newSingleThreadExecutor(new ThreadFactory() {
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "SocketServlet");

            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);

            return t;
        }
    });

    final WrappedPipedInputStream pisReader;

    final WrappedPipedOutputStream posReader;

    final WrappedPipedInputStream pisWriter;

    final WrappedPipedOutputStream posWriter;

    final SocketServlet servlet;

    boolean isRunning;                                 // 网络是否正在运行

    boolean isClosed;                                  // 网络是否已关闭

    final Runnable receive = new Runnable() {          // 网络接收线程

        @Override
        public void run() {
            while (isRunning)
            {
                try {
                    while (isRunning)
                    {
                        servlet.doServlet(pisReader, posWriter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public SocketProxy(SocketServlet servlet) {
        this.servlet = servlet;

        pisReader = new WrappedPipedInputStream();
        posReader = new WrappedPipedOutputStream();
        pisWriter = new WrappedPipedInputStream();
        posWriter = new WrappedPipedOutputStream();
        try {
            posReader.connect(pisReader);
            posWriter.connect(pisWriter);
        } catch (IOException e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }

    public InputStream getInputStream() {
        checkOpenAndCreate();
        return pisWriter;
    }

    public OutputStream getOutputStream() {
        checkOpenAndCreate();
        return posReader;
    }

    private synchronized void checkOpenAndCreate() {
        if (isClosed || isRunning)
        {
            return;
        }

        isRunning = true;
        threadPool.execute(receive);
    }

    public static interface SocketServlet {

        public void doServlet(InputStream in, OutputStream out);
    }

    /**
     * 关闭网络连接
     */
    public synchronized void close() {
        if (isClosed)
        {
            return;
        }

        isRunning = false;
        isClosed = true;
        if (pisReader != null)
        {
            try {
                pisReader._close();
            } catch (IOException e) {}
        }

        if (posReader != null)
        {
            try {
                posReader._close();
            } catch (IOException e) {}
        }

        if (pisWriter != null)
        {
            try {
                pisWriter._close();
            } catch (IOException e) {}
        }

        if (posWriter != null)
        {
            try {
                posWriter._close();
            } catch (IOException e) {}
        }

        threadPool.shutdownNow();
    }

    private class WrappedPipedInputStream extends PipedInputStream {

        @Override
        public synchronized void close() throws IOException {
            // Do Nothing.
        }

        public synchronized void _close() throws IOException {
            super.close();
        }
    }

    private class WrappedPipedOutputStream extends PipedOutputStream {

        @Override
        public void close() throws IOException {
            // Do Nothing.
        }

        public void _close() throws IOException {
            super.close();
        }
    }
}