package engine.android.util.secure;

import java.util.Random;

/**
 * 混淆类
 * 
 * @author Daimon
 * @since 3/21/2012
 */
public final class Obfuscate {

    public static byte[] obfuscate(byte[] data) {
        Random rand = new Random();
        byte[] key = new byte[rand.nextInt(128)];
        rand.nextBytes(key);

        return obfuscate(data, key);
    }

    public static byte[] obfuscate(byte[] data, byte[] key) {
        int len = key.length;
        byte[] bs = new byte[data.length + len + 1];
        int index = 0;

        bs[index++] = (byte) len;
        System.arraycopy(key, 0, bs, index, len);
        index += len;
        XOR(data, 0, bs, index, data.length, key);

        return bs;
    }

    public static byte[] clarify(byte[] data) {
        int index = 0;
        int len = data[index++] & 0x7F;

        byte[] key = new byte[len];
        System.arraycopy(data, index, key, 0, len);
        index += len;

        byte[] bs = new byte[data.length - index];
        XOR(data, index, bs, 0, bs.length, key);

        return bs;
    }

    public static byte[] XOR(byte[] data, byte[] key) {
        byte[] bs = new byte[data.length];
        XOR(data, 0, bs, 0, data.length, key);
        return bs;
    }

    private static void XOR(byte[] src, int srcPos, byte[] dest, int destPos, 
            int length, byte[] key) {
        for (int i = 0; i < length; i++)
        {
            dest[i + destPos] = (byte) (src[i + srcPos] ^ key[i % key.length]);
        }
    }
}