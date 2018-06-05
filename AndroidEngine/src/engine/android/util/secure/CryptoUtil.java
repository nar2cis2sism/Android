package engine.android.util.secure;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 操作系统提供的各种算法工具
 * 
 * @author Daimon
 * @version N
 * @since 7/4/2013
 */
public final class CryptoUtil {
    
    /**
     * Message-Digest Algorithm 5(信息-摘要算法)<br>
     * 一种基于散列算法的单向加密算法<br>
     * 常用于文件校验<br>
     * 输出结果为128位，速度较SHA1慢一点
     */
    public static byte[] md5(byte[] bs) {
        return digest(bs, "MD5");
    }

    /**
     * SHA（Secure Hash Algorithm）安全哈希算法<br>
     * 一般用于数字签名，较之MD5更为安全，输出结果为160位
     * 还可以传入SHA-256 SHA-384 SHA-512...
     */
    public static byte[] SHA1(byte[] bs) {
        return digest(bs, "SHA-1");
    }
    
    private static byte[] digest(byte[] bs, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(bs);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }

    public static byte[] signAndEncrypt(PrivateKey key, byte[] data) {
        try {
            // 实例化一个用SHA算法进行散列，用RSA算法进行加密的Signature
            Signature sign = Signature.getInstance("SHA1WithRSA");
            // 设置加密散列码用的私钥
            sign.initSign(key);
            // 设置散列算法的输入
            sign.update(data);
            // 进行散列，对产生的散列码进行加密并返回
            return sign.sign();
        } catch (Exception e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyAndDecrypt(PublicKey key, byte[] data, byte[] signature) {
        try {
            // 实例化一个用SHA算法进行散列，用RSA算法进行加密的Signature
            Signature sign = Signature.getInstance("SHA1WithRSA");
            // 设置解密散列码用的公钥
            sign.initVerify(key);
            // 设置散列算法的输入
            sign.update(data);
            // 进行散列计算，比较计算所得散列码和解密的散列码是否一致
            return sign.verify(signature);
        } catch (Exception e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }

    /************************* 对称加密算法 *************************/
    // http://www.seacha.com/tools/aes.php

    public static byte[] AES_encrypt(byte[] key, byte[] data) {
        return encrypt(new SecretKeySpec(key, "AES"), data, "AES/ECB/PKCS5Padding");
    }

    public static byte[] AES_decrypt(byte[] key, byte[] data) {
        return decrypt(new SecretKeySpec(key, "AES"), data, "AES/ECB/PKCS5Padding");
    }

    /**
     * 随机获取128位密钥
     */
    public static byte[] getRandomAESKey(byte[] seed) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed);
            generator.init(128, random); // 192 and 256 bits may not be available

            SecretKey key = generator.generateKey();
            return key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }

    public static byte[] DES_encrypt(byte[] key, byte[] data) {
        return encrypt(getDESKey(key), data, "DES");
    }

    public static byte[] DES_decrypt(byte[] key, byte[] data) {
        return decrypt(getDESKey(key), data, "DES");
    }

    private static Key getDESKey(byte[] seed) {
        try {
            DESKeySpec keySpec = new DESKeySpec(seed);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            return key;
        } catch (Exception e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }
    
    static byte[] encrypt(Key key, byte[] data, String transformation) {
        return crypto(key, data, transformation, Cipher.ENCRYPT_MODE);
    }
    
    static byte[] decrypt(Key key, byte[] data, String transformation) {
        return crypto(key, data, transformation, Cipher.DECRYPT_MODE);
    }
    
    private static byte[] crypto(Key key, byte[] data, String transformation, int cipherMode) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(cipherMode, key/* , new IvParameterSpec(iv) IV向量 */);
            return cipher.doFinal(data);
        } catch (Exception e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
    }
}