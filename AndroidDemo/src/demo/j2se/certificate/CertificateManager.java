package demo.j2se.certificate;

public class CertificateManager {
    
    public static void main(String[] args)
    {
        try {
            final String crypto_string = "`1234567890-=~!@#$%^&*()_+qwertyuiop[]\\QWERTYUIOP{中文}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?";
            final byte[] crypto_bytes = crypto_string.getBytes();
            
            String certificatePath = "C:/Users/hyan/Desktop/certificate/androidEngine.cer";
            String keyStorePath = "C:/Users/hyan/Desktop/certificate/androidEngine.keystore";
            String alias = "androidEngine";
            String password = "androidEngine";
            
            System.out.println("验证证书有效性：" + CertificateUtil.verifyCertificate(certificatePath));
            System.out.println("验证密钥有效性：" + CertificateUtil.verifyCertificate(keyStorePath, alias, password));
            
            System.out.println();
            System.out.println("公钥加密——私钥解密");
            long time = System.currentTimeMillis();
            byte[] encrypt_result = CertificateUtil.encryptByPublicKey(crypto_bytes, certificatePath);
            long time1 = System.currentTimeMillis();
            byte[] decrypt_result = CertificateUtil.decryptByPrivateKey(encrypt_result, keyStorePath, alias, password);
            long time2 = System.currentTimeMillis();
            
            System.out.println("原文：" + crypto_string);
            System.out.println("加密：" + new String(encrypt_result));
            System.out.println("用时：" + (time1 - time));
            System.out.println("解密：" + new String(decrypt_result));
            System.out.println("用时：" + (time2 - time1));

            System.out.println();
            System.out.println("私钥加密——公钥解密");
            time = System.currentTimeMillis();
            encrypt_result = CertificateUtil.encryptByPrivateKey(crypto_bytes, keyStorePath, alias, password);
            time1 = System.currentTimeMillis();
            decrypt_result = CertificateUtil.decryptByPublicKey(encrypt_result, certificatePath);
            time2 = System.currentTimeMillis();
            
            System.out.println("原文：" + crypto_string);
            System.out.println("加密：" + new String(encrypt_result));
            System.out.println("用时：" + (time1 - time));
            System.out.println("解密：" + new String(decrypt_result));
            System.out.println("用时：" + (time2 - time1));

            System.out.println();
            System.out.println("私钥签名——公钥验证签名");
            time = System.currentTimeMillis();
            byte[] signature = CertificateUtil.sign(encrypt_result, keyStorePath, alias, password);
            time1 = System.currentTimeMillis();
            boolean verify = CertificateUtil.verify(encrypt_result, signature, certificatePath);
            time2 = System.currentTimeMillis();
            
            System.out.println("原文：" + new String(encrypt_result));
            System.out.println("签名：" + new String(signature));
            System.out.println("用时：" + (time1 - time));
            System.out.println("验证：" + verify);
            System.out.println("用时：" + (time2 - time1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}