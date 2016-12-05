package engine.android.util.secure;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA密钥对非对称加密算法<p>
 * 
 * 第一个既能用于数据加密也能用于数字签名的算法<br>
 * 只能对少量数据加密，否则会抛出
 * javax.crypto.IllegalBlockSizeException: Data must not be longer than 117 bytes
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public final class RSA {

    /**
     * 生成密钥对
     */
    public static KeyPair generateKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }

    /**
     * 用公钥加密
     */
    public static byte[] encrypt(PublicKey key, byte[] data) {
        if (key != null)
        {
            return CryptoUtil.encrypt(key, data, "RSA");
        }

        return null;
    }

    /**
     * 用私钥解密
     */
    public static byte[] decrypt(PrivateKey key, byte[] data) {
        if (key != null)
        {
            return CryptoUtil.decrypt(key, data, "RSA");
        }

        return null;
    }
}