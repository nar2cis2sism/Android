package app.test;

import engine.java.util.Util;
import engine.java.util.secure.Base64;
import engine.java.util.secure.Blowfish;
import engine.java.util.secure.CryptoUtil;
import engine.java.util.secure.HexUtil;
import engine.java.util.secure.Obfuscate;

public class CryptTest {
    
    public static void main(String[] args) {
        String crypto_string = "`1234567890-=~!@#$%^&*()_+qwertyuiop[]\\QWERTYUIOP{中文}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?";
        byte[] crypto_bytes = crypto_string.getBytes();
        byte[] bs = new byte[1024];
        for (int i = 0; i < 10; i++)
        {
            System.arraycopy(crypto_bytes, 0, bs, i * crypto_bytes.length, crypto_bytes.length);
        }
        
        System.out.println("密文长度:" + bs.length);
        
        byte[] key = "`1234567890-=~!@#$%^&*()_+qwertyuiop[]".getBytes();
        System.out.println("密钥长度:" + key.length);
        
        AES(key, bs);
        DES(key, bs);
        blowfish(key, bs);
        System.out.println();
        md5(key, bs);
        SHA1(key, bs);
        System.out.println();
        obfuscate(key, bs);
        hex(key, bs);
        Base64(key, bs);
    }
    
    private static void AES(byte[] key, byte[] bs) {
        byte[] newKey = CryptoUtil.getRandomAESKey(key);
        long time = System.nanoTime();
        byte[] data = CryptoUtil.AES_encrypt(newKey, bs);
        time = System.nanoTime() - time;
        System.out.println("AES加密后长度:" + data.length + time(time));
    }
    
    private static void DES(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        byte[] data = CryptoUtil.DES_encrypt(key, bs);
        time = System.nanoTime() - time;
        System.out.println("DES加密后长度:" + data.length + time(time));
    }
    
    private static void blowfish(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        Blowfish blowfish = new Blowfish();
        blowfish.setKey(key);
        byte[] data = blowfish.encrypt(bs);
        time = System.nanoTime() - time;
        System.out.println("Blowfish加密后长度:" + data.length + time(time));
    }
    
    private static void md5(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        byte[] data = CryptoUtil.md5(bs);
        time = System.nanoTime() - time;
        System.out.println("Md5加密后长度:" + data.length + time(time));
    }
    
    private static void SHA1(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        byte[] data = CryptoUtil.SHA1(bs);
        time = System.nanoTime() - time;
        System.out.println("SHA1加密后长度:" + data.length + time(time));
    }
    
    private static void obfuscate(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        byte[] data = Obfuscate.obfuscate(bs, key);
        time = System.nanoTime() - time;
        System.out.println("混淆后长度:" + data.length + time(time));
    }
    
    private static void hex(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        String data = HexUtil.encode(bs);
        time = System.nanoTime() - time;
        System.out.println("十六进制编码后长度:" + data.getBytes().length + time(time));
    }
    
    private static void Base64(byte[] key, byte[] bs) {
        long time = System.nanoTime();
        String data = Base64.encodeToString(bs, 0);
        time = System.nanoTime() - time;
        System.out.println("Base64编码后长度:" + data.getBytes().length + time(time));
    }
    
    private static String time(long time) {
        return "-花费时间" + Util.formatNumber(time * 0.1 / 100000, "0.00") + "ms";
    }
}