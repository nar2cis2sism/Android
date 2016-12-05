package engine.android.util.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public final class IOUtil {

    /**
     * The default buffer size to use.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * 读取输入流数据
     */
    public static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeStream(is, baos);
        return baos.toByteArray();
    }

    /**
     * 读取输入流数据
     */
    public static byte[] readStream(Reader r) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        try {
            writeStream(r, osw);
            osw.flush();
            return baos.toByteArray();
        } finally {
            osw.close();
        }
    }

    /**
     * 写入输入流数据到输出流
     */
    public static long writeStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while ((n = is.read(buffer)) > 0)
        {
            os.write(buffer, 0, n);
            count += n;
        }

        return count;
    }

    /**
     * 写入输入流数据到输出流
     */
    public static long writeStream(Reader r, Writer w) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while ((n = r.read(buffer)) > 0)
        {
            w.write(buffer, 0, n);
            count += n;
        }

        return count;
    }

    public static void closeSilently(Closeable close) {
        if (close != null)
        {
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}