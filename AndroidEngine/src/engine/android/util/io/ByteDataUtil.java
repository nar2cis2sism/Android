package engine.android.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 字节数据存取工具
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public final class ByteDataUtil {

    /**
     * 存储数据
     */
    public static byte[] save(ByteData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            data.write(dos);
        } finally {
            dos.close();
        }

        return baos.toByteArray();
    }

    /**
     * 恢复数据
     */
    public static void restore(byte[] bs, ByteData data) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bs));
        try {
            data.read(dis);
        } finally {
            dis.close();
        }
    }

    /**
     * 用于存取的字节数据接口
     */
    public interface ByteData {

        void write(DataOutputStream dos) throws IOException;

        void read(DataInputStream dis) throws IOException;
    }

    /**
     * @param data The current data.
     * @param bit Location of bit to set, indexed from 1 right to left.
     * @param value Whether to enable or disable the bit.
     * @return The modified data.
     */
    public static byte setBitMask(byte data, int bit, boolean value) {
        byte b = 0x01;
        b <<= bit - 1;
        if (value)
        {
            data |= b;
        }
        else
        {
            data &= ~b;
        }

        return data;
    }

    /**
     * @param data The current data.
     * @param bit Location of bit to extract, indexed from 1 right to left.
     * @return True if the bit is enable, false else.
     */
    public static boolean hasBitMask(byte data, int bit) {
        byte b = 0x01;
        b <<= bit - 1;
        return (data & b) != 0;
    }

    /**
     * 1个int转换为4个byte（从低位到高位排列）
     */
    public static byte[] intToBytes_LH(int i) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (i >> 24);
        bytes[2] = (byte) (i >> 16);
        bytes[1] = (byte) (i >> 8);
        bytes[0] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytes_LH(int i, byte[] bs, int offset) {
        byte[] bytes = intToBytes_LH(i);
        System.arraycopy(bytes, 0, bs, offset, bytes.length);
        return bytes;
    }

    /**
     * 4个byte转换为1个int（从低位到高位排列）
     */
    public static int bytesToInt_LH(byte[] bs, int offset) {
        return (bs[offset] & 0xff)
            | ((bs[offset + 1] & 0xff) << 8)
            | ((bs[offset + 2] & 0xff) << 16)
            | ((bs[offset + 3] & 0xff) << 24);
    }

    /**
     * 1个int转换为4个byte（从高位到低位排列）
     */
    public static byte[] intToBytes_HL(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i >> 24);
        bytes[1] = (byte) (i >> 16);
        bytes[2] = (byte) (i >> 8);
        bytes[3] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytes_HL(int i, byte[] bs, int offset) {
        byte[] bytes = intToBytes_HL(i);
        System.arraycopy(bytes, 0, bs, offset, bytes.length);
        return bytes;
    }

    /**
     * 4个byte转换为1个int（从高位到低位排列）
     */
    public static int bytesToInt_HL(byte[] bs, int offset) {
        return (bs[offset + 3] & 0xff)
            | ((bs[offset + 2] & 0xff) << 8)
            | ((bs[offset + 1] & 0xff) << 16)
            | ((bs[offset] & 0xff) << 24);
    }
}