package demo.activity.test;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.android.R;
import engine.android.util.secure.Base64;
import engine.android.util.secure.Blowfish;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;
import engine.android.util.secure.MD5;
import engine.android.util.secure.Obfuscate;
import engine.android.util.secure.RSA;
import engine.android.util.secure.ZipUtil;

import java.io.IOException;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;

public class TestOnCrypto extends TestOnBase {
    
    boolean flag = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        invalidate();
    }
    
    private void invalidate()
    {
        if (flag = !flag)
        {
            showContent();
        }
        else
        {
            setContentView(R.layout.main);
            sb.delete(0, sb.length());
            
            final String crypto_string = "`1234567890-=~!@#$%^&*()_+qwertyuiop[]\\QWERTYUIOP{中文}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?";
            final byte[] crypto_bytes = crypto_string.getBytes();
            
            ListView lv = (ListView) findViewById(R.id.list);
            List<String> list = new LinkedList<String>();
            list.add("Blowfish");
            list.add("MD5");
            list.add("RSA");
            list.add("AES&DES");
            list.add("混淆");
            list.add("Base64");
            list.add("Hex工具");
            list.add("压缩解压缩");
            list.add("签名");
            lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    switch (position) {
                    case 0:
                    {
                        Blowfish bf = new Blowfish();
                        bf.setKey(crypto_bytes);
                        
                        long time = System.nanoTime();
                        byte[] encrypt_result = bf.encrypt(crypto_bytes);
                        long time1 = System.nanoTime();
                        byte[] decrypt_result = bf.decrypt(encrypt_result);
                        long time2 = System.nanoTime();
                        
                        log("原文：" + crypto_string);
                        log("加密：" + new String(encrypt_result));
                        log("用时：" + (time1 - time));
                        log("解密：" + new String(decrypt_result));
                        log("用时：" + (time2 - time1));
                        
                        break;
                    }
                        
                    case 1:
                    {
                        MD5 md5 = new MD5();
                        
                        long time = System.nanoTime();
                        String encrypt_result = md5.encrypt(crypto_bytes, 0, crypto_bytes.length);
                        long time1 = System.nanoTime();
                        String encrypt_result2 = HexUtil.encode(CryptoUtil.md5(crypto_bytes));
                        long time2 = System.nanoTime();
                        String encrypt_result3 = HexUtil.encode(CryptoUtil.SHA1(crypto_bytes));
                        long time3 = System.nanoTime();
                        
                        log("原文：" + crypto_string);
                        log("加密：" + encrypt_result);
                        log("用时：" + (time1 - time));
                        log("MD5官方加密：" + encrypt_result2);//更快
                        log("用时：" + (time2 - time1));
                        log("SHA1加密：" + encrypt_result3);//最快
                        log("用时：" + (time3 - time2));
                        log("解密：" + "不可逆转");
                        
                        break;
                    }
                    
                    case 2:
                    {
                        KeyPair key = RSA.generateKey();
                        
                        long time = System.nanoTime();
                        byte[] encrypt_result = RSA.encrypt(key.getPublic(), crypto_bytes);
                        long time1 = System.nanoTime();
                        byte[] decrypt_result = RSA.decrypt(key.getPrivate(), encrypt_result);
                        long time2 = System.nanoTime();
                        
                        log("原文：" + crypto_string);
                        log("加密：" + new String(encrypt_result));
                        log("用时：" + (time1 - time));
                        log("解密：" + new String(decrypt_result));//很慢
                        log("用时：" + (time2 - time1));
                        
                        break;
                    }
                    
                    case 3:
                    {
                        long time = System.nanoTime();
                        byte[] encrypt_result;
                        long time1;
                        byte[] decrypt_result;
                        long time2;
                        try {
                            encrypt_result = CryptoUtil.AES_encrypt(crypto_bytes, crypto_bytes);
                            time1 = System.nanoTime();
                            decrypt_result = CryptoUtil.AES_decrypt(crypto_bytes, encrypt_result);
                            time2 = System.nanoTime();
                            
                            log("原文：" + crypto_string);
                            log("AES加密：" + new String(encrypt_result));
                            log("用时：" + (time1 - time));
                            log("解密：" + new String(decrypt_result));
                            log("用时：" + (time2 - time1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        time = System.nanoTime();
                        encrypt_result = CryptoUtil.DES_encrypt(crypto_bytes, crypto_bytes);
                        time1 = System.nanoTime();
                        decrypt_result = CryptoUtil.DES_decrypt(crypto_bytes, encrypt_result);
                        time2 = System.nanoTime();
                        
                        log("DES加密：" + new String(encrypt_result));
                        log("用时：" + (time1 - time));
                        log("解密：" + new String(decrypt_result));
                        log("用时：" + (time2 - time1));
                        
                        break;
                    }
                    
                    case 4:
                    {
                        long time = System.nanoTime();
                        byte[] encrypt_result = Obfuscate.obfuscate(crypto_bytes, crypto_bytes);
                        long time1 = System.nanoTime();
                        byte[] decrypt_result = Obfuscate.clarify(encrypt_result);
                        long time2 = System.nanoTime();
                        
                        log("原文：" + crypto_string);
                        log("混淆：" + new String(encrypt_result));
                        log("用时：" + (time1 - time));
                        log("还原：" + new String(decrypt_result));
                        log("用时：" + (time2 - time1));
                        
                        break;
                    }
                    
                    case 5:
                    {
                        long time = System.nanoTime();
                        String encrypt_result;
                        long time1;
                        String decrypt_result;
                        long time2;
                        try {
                            encrypt_result = Base64.encode(crypto_string);
                            time1 = System.nanoTime();
                            decrypt_result = Base64.decode(encrypt_result);
                            time2 = System.nanoTime();
                            
                            log("原文：" + crypto_string);
                            log("编码         ：" + encrypt_result);
                            log("用时：" + (time1 - time));
                            log("解码：" + decrypt_result);
                            log("用时：" + (time2 - time1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        time = System.nanoTime();
                        encrypt_result = android.util.Base64.encodeToString(crypto_bytes, android.util.Base64.DEFAULT);
                        time1 = System.nanoTime();
                        decrypt_result = new String(android.util.Base64.decode(encrypt_result, android.util.Base64.DEFAULT));
                        time2 = System.nanoTime();

                        log("官方编码：" + encrypt_result);
                        log("用时：" + (time1 - time));
                        log("解码：" + decrypt_result);
                        log("用时：" + (time2 - time1));
                        
                        break;
                    }
                    
                    case 6:
                    {
                        long time = System.nanoTime();
                        String encrypt_result = HexUtil.encode(crypto_bytes);
                        long time1 = System.nanoTime();
                        byte[] decrypt_result = HexUtil.decode(encrypt_result);
                        long time2 = System.nanoTime();
                        
                        log("原文：" + crypto_string);
                        log("编码：" + encrypt_result);
                        log("用时：" + (time1 - time));
                        log("解码：" + new String(decrypt_result));
                        log("用时：" + (time2 - time1));
                        
                        break;
                    }
                    
                    case 7:
                    {
                        byte[] bs = new byte[crypto_bytes.length * 100];
                        int len = 0;
                        while (len < bs.length)
                        {
                            System.arraycopy(crypto_bytes, 0, bs, len, crypto_bytes.length);
                            len += crypto_bytes.length;
                        }
                        
                        try {
                            long time = System.nanoTime();
                            byte[] encrypt_result = ZipUtil.gzip(bs);
                            long time1 = System.nanoTime();
                            byte[] decrypt_result = ZipUtil.ungzip(encrypt_result);
                            long time2 = System.nanoTime();
                            
                            log("原文大小：" + bs.length);
                            log("压缩大小：" + encrypt_result.length);
                            log("用时：" + (time1 - time));
                            log("解压大小：" + decrypt_result.length);
                            log("用时：" + (time2 - time1));
                        } catch (IOException e) {
                            log(e.toString());
                        }
                        
                        break;
                    }
                    
                    case 8:
                    {
                        try {
                            KeyPair key = RSA.generateKey();
                            
                            long time = System.nanoTime();
                            byte[] encrypt_result = CryptoUtil.signAndEncrypt(key.getPrivate(), crypto_bytes);
                            long time1 = System.nanoTime();
                            boolean decrypt_result = CryptoUtil.verifyAndDecrypt(key.getPublic(), crypto_bytes, encrypt_result);
                            long time2 = System.nanoTime();
                            
                            log("原文：" + crypto_string);
                            log("签名：" + new String(encrypt_result));
                            log("用时：" + (time1 - time));
                            log("验证：" + decrypt_result);
                            log("用时：" + (time2 - time1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        break;
                    }
                    
                    default:
                        break;
                    }
                    
                    invalidate();
                }
            });
        }
    }
    
    @Override
    public void onBackPressed() {
        if (flag)
        {
            invalidate();
        }
        else
        {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void log(String content) {
        super.log(content + "\n");
    }
}