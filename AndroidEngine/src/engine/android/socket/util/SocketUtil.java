package engine.android.socket.util;

public final class SocketUtil {

    private static final byte[] PASSWORD = "yanhao".getBytes();     // 握手密钥

    private static byte[] crypt_key;                                // 数据密钥

    /**
     * 握手
     * 
     * @param bs 握手信息
     * @return CRC校验值
     */

    public static int handshake(byte[] bs) {
        byte[] key = new byte[8];
        System.arraycopy(PASSWORD, 0, key, 0, Math.min(PASSWORD.length, key.length));
        crypt_key = DesEncrypt.DESplus(key, bs, false); // 解密握手信息

        return CRCUtility.calculate(crypt_key, crypt_key.length);
    }

    /**
     * 数据处理
     */

    public static void crypt(byte[] data) {
        if (crypt_key != null && data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                data[i] = (byte) (data[i] ^ crypt_key[i % crypt_key.length]);
            }
        }
    }
}