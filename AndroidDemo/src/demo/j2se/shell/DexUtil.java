package demo.j2se.shell;

import android.content.Context;

import engine.android.util.io.ByteDataUtil;
import engine.android.util.io.IOUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class DexUtil {
    
    /**
     * 加壳算法
     * @param data 加壳数据
     */
    
    public static byte[] shellToDex(byte[] dex, byte[] data) throws Exception {
        byte[] srcData = data;
        byte[] dexData = dex;
        
        int srcLen = srcData.length;
        int dexLen = dexData.length;
        
        int length = srcLen + dexLen + 4;
        byte[] newData = new byte[length];
        
        //添加解壳代码
        System.arraycopy(dexData, 0, newData, 0, dexLen);
        //添加加密后的解壳数据
        System.arraycopy(srcData, 0, newData, dexLen, srcLen);
        //添加解壳数据长度
        ByteDataUtil.intToBytes_HL(srcLen, newData, length - 4);
        
        //修改DEX file size文件头
        updateFileSizeHeader(newData);
        //修改DEX SHA1 文件头
        updateSHA1Header(newData);
        //修改DEX CheckSum文件头
        updateCheckSumHeader(newData);
        
        return newData;
    }
    
    private static void updateFileSizeHeader(byte[] dexData) {
        //名称：file_size
        //偏移：0x20
        //长度：4
        //描述：Dex文件的总长度
        ByteDataUtil.intToBytes_LH(dexData.length, dexData, 32);
    }

    private static void updateSHA1Header(byte[] dexData) throws NoSuchAlgorithmException {
        //名称：signature
        //偏移：0xC
        //长度：20
        //描述：SHA-1签名
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(dexData, 32, dexData.length - 32);
        byte[] bs = md.digest();
        System.arraycopy(bs, 0, dexData, 12, 20);
    }
    
    private static void updateCheckSumHeader(byte[] dexData) {
        //名称：checksum
        //偏移：0x8
        //长度：4
        //描述：校验码
        //功能：主要用来检查从这个字段开始到文件结尾，这段数据是否完整，有没有人修改过，或者传送过程中是否有出错等等。
        Adler32 adler32 = new Adler32();
        adler32.update(dexData, 12, dexData.length - 12);
        long value = adler32.getValue();
        ByteDataUtil.intToBytes_LH((int) value, dexData, 8);
    }
    
    /**
     * 解壳算法
     * @return 解壳数据
     */
    
    public static byte[] unshellFromDex(byte[] dex) throws Exception {
        byte[] dexData = dex;
        int dexLen = dexData.length;

        int srcLen = ByteDataUtil.bytesToInt_HL(dexData, dexLen - 4);
        byte[] srcData = new byte[srcLen];
        System.arraycopy(dexData, dexLen - 4 - srcLen, srcData, 0, srcLen);
        
        return srcData;
    }
    
    public static byte[] getDexData(Context context) throws Exception {
        ZipFile zf = null;
        try {
            zf = new ZipFile(context.getApplicationInfo().sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            if (ze == null)
            {
                throw new Exception("'classes.dex' is not exist.");
            }

            return IOUtil.readStream(zf.getInputStream(ze));
        } finally {
            if (zf != null)
            {
                zf.close();
            }
        }
    }
}