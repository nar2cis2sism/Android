package demo.j2se.certificate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.Cipher;

/**
 * 证书工具
 * @author Daimon
 * @version 3.0
 * @since 7/5/2013
 */

public final class CertificateUtil {
    
    /**
     * Java密钥库(Java Key Store，JKS)
     */
    
    public static final String KEY_STORE = "JKS";
    
    public static final String X509 = "X.509";
    
    
    private static PrivateKey getPrivateKey(String keyStorePath, String alias, String password) throws Exception
    {
        return (PrivateKey) getKeyStore(keyStorePath, password).getKey(alias, password.toCharArray());
    }
    
    private static PublicKey getPublicKey(String certificatePath) throws Exception
    {
        return getCertificate(certificatePath).getPublicKey();
    }
    
    private static Certificate getCertificate(String certificatePath) throws Exception
    {
        InputStream in = null;
        try {
            in = new FileInputStream(certificatePath);
            return CertificateFactory.getInstance(X509).generateCertificate(in);
        } finally {
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    private static Certificate getCertificate(String keyStorePath, String alias, String password) throws Exception
    {
        return getKeyStore(keyStorePath, password).getCertificate(alias);
    }
    
    private static KeyStore getKeyStore(String keyStorePath, String password) throws Exception
    {
        InputStream in = null;
        try {
            in = new FileInputStream(keyStorePath);
            KeyStore ks = KeyStore.getInstance(KEY_STORE);
            ks.load(in, password.toCharArray());
            return ks;
        } finally {
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    public static byte[] encryptByPrivateKey(byte[] data, String keyStorePath, String alias, String password) throws Exception
    {
        PrivateKey key = getPrivateKey(keyStorePath, alias, password);
        
        Cipher c = Cipher.getInstance(key.getAlgorithm());
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(data);
    }
    
    public static byte[] decryptByPrivateKey(byte[] data, String keyStorePath, String alias, String password) throws Exception
    {
        PrivateKey key = getPrivateKey(keyStorePath, alias, password);
        
        Cipher c = Cipher.getInstance(key.getAlgorithm());
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(data);
    }
    
    public static byte[] encryptByPublicKey(byte[] data, String certificatePath) throws Exception
    {
        PublicKey key = getPublicKey(certificatePath);
        
        Cipher c = Cipher.getInstance(key.getAlgorithm());
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(data);
    }
    
    public static byte[] decryptByPublicKey(byte[] data, String certificatePath) throws Exception
    {
        PublicKey key = getPublicKey(certificatePath);
        
        Cipher c = Cipher.getInstance(key.getAlgorithm());
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(data);
    }
    
    public static boolean verifyCertificate(String certificatePath)
    {
        return verifyCertificate(new Date(), certificatePath);
    }
    
    /**
     * 验证证书是否过期或无效
     * @param date 比较的日期
     * @return true可以使用
     */
    
    public static boolean verifyCertificate(Date date, String certificatePath)
    {
        try {
            return verifyCertificate(date, getCertificate(certificatePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private static boolean verifyCertificate(Date date, Certificate certificate)
    {
        try {
            X509Certificate x509 = (X509Certificate) certificate;
            x509.checkValidity(date);
            return true;
        } catch (CertificateExpiredException e) {
            e.printStackTrace();
        } catch (CertificateNotYetValidException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public static byte[] sign(byte[] data, String keyStorePath, String alias, String password) throws Exception
    {
        X509Certificate x509 = (X509Certificate) getCertificate(keyStorePath, alias, password);
        PrivateKey key = getPrivateKey(keyStorePath, alias, password);
        Signature sign = Signature.getInstance(x509.getSigAlgName());
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }
    
    public static boolean verify(byte[] data, byte[] signature, String certificatePath) throws Exception
    {
        X509Certificate x509 = (X509Certificate) getCertificate(certificatePath);
        PublicKey key = getPublicKey(certificatePath);
        Signature sign = Signature.getInstance(x509.getSigAlgName());
        sign.initVerify(key);
        sign.update(data);
        return sign.verify(signature);
    }
    
    public static boolean verifyCertificate(String keyStorePath, String alias, String password)
    {
        return verifyCertificate(new Date(), keyStorePath, alias, password);
    }
    
    /**
     * 验证证书是否过期或无效
     * @param date 比较的日期
     * @return true可以使用
     */
    
    public static boolean verifyCertificate(Date date, String keyStorePath, String alias, String password)
    {
        try {
            return verifyCertificate(date, getCertificate(keyStorePath, alias, password));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}