package engine.android.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * PNG图片操作
 * 
 * @author Daimon
 * @version 3.0
 * @since 3/23/2012
 */

public final class PNG {

    private static final byte[] HEADChunk = { 
        (byte) 0x89, 0x50, 0x4E, 0x47,
               0x0D, 0x0A, 0x1A, 0x0A },            // PNG文件署名域（识别该文件是不是PNG文件）
               
        tRNSChunk = { 0x00, 0x00, 0x00, 0x01, 0x74,
                0x52, 0x4E, 0x53, 0x00, 0x40,
                (byte) 0xE6, (byte) 0xD8, 0x66 },

        IENDChunk = { 0x00, 0x00, 0x00, 0x00, 0x49,
                0x45, 0x4E, 0x44, (byte) 0xAE,
                0x42, 0x60, (byte) 0x82 };          // 图像结束数据（文件结尾）

    private static int IDATPOS;                     // 数据块的位置

    private static int[] crc_table;                 // CRC表

    /**
     * 生成的byte[]数组可直接用于外部存储为.png格式的图片文件，看图软件可直接打开<br>
     * Reminder:can't revert
     */

    public static byte[] image2bytes(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int offset = 0;
        byte[] buffer = new byte[(w * 4 + 1) * h + offset];
        getImageBufferForImageARGB8888(image, buffer, w, h, offset);
        System.gc();

        byte[] data;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            writePNG(dos, w, h, buffer, null, false, offset);
            data = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (baos != null)
            {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                baos = null;
            }

            if (dos != null)
            {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dos = null;
            }
        }

        writeCRC(data, 8);// 更新IHDR CRC
        writeCRC(data, 33);// 更新PLTE CRC
        writeCRC(data, IDATPOS);// 更新IDAT CRC
        return data;
    }

    /**
     * 取得图片的ARGB信息并组合成图像数据块（IDAT）
     */

    private static void getImageBufferForImageARGB8888(Bitmap image, byte[] bs,
            int w, int h, int offset) {
        // 非隔行扫描，数据按照行来存储，为了区分第一行，在每一行的前面加上0
        int n = offset;
        int[] a = new int[w];
        for (int i = 0; i < h; i++)
        {
            image.getPixels(a, 0, w, 0, i, w, 1);
            for (int j = 0; j < w; j++)
            {
                int argb = a[j];
                int A = (argb & 0xff000000) >> 24;
                int r = (argb & 0xff0000) >> 16;
                int g = (argb & 0xff00) >> 8;
                int b = argb & 0xff;
                if (j % w == 0)
                {
                    n += 1;
                }

                bs[n] = (byte) r;
                bs[n + 1] = (byte) g;
                bs[n + 2] = (byte) b;
                bs[n + 3] = (byte) A;
                n += 4;
            }
        }
    }

    /**
     * 按照PNG图片格式将数据写入到流中
     */

    private static void writePNG(DataOutputStream dos, int width, int height, byte[] buffer,
            byte[] colors, boolean transparent, int offset) throws IOException {
        int adler = adler32(1, buffer, offset, buffer.length - offset);
        // 数据块压缩信息
        byte[] lenNlen = { 0, (byte) 0xfa, 0x7e, 0x05, (byte) 0x81 };
        IDATPOS = 0;
        // PNG文件署名域
        dos.write(HEADChunk);
        IDATPOS += HEADChunk.length;
        // 文件头IHDR
        dos.writeInt(13);// 长度
        dos.writeInt(1229472850);// IHDR类型码
        dos.writeInt(width);// 宽度
        dos.writeInt(height);// 高度
        dos.writeByte(8);// 图像深度（PNG8）
        if (colors == null)
        {
            dos.writeByte(6);// 带alpha通道数据的真彩色图像
        }
        else
        {
            dos.writeByte(3);// 索引彩色图像
        }

        dos.writeByte(0);// 压缩方法（无损压缩）
        dos.writeByte(0);// 滤波器方法
        dos.writeByte(0);// 隔行扫描方法（非隔行扫描）
        dos.writeInt(0);// CRC
        IDATPOS += 25;
        // 调色板PLTE
        if (colors != null)
        {
            dos.writeInt(colors.length);// 长度
            dos.writeInt(1347179589);// PLTE类型码
            dos.write(colors);
            dos.writeInt(0);// CRC
            IDATPOS += colors.length + 12;
        }
        // tRNS
        if (transparent)
        {
            dos.write(tRNSChunk);
            IDATPOS += tRNSChunk.length;
        }
        // 图像数据块IDAT（无压缩的LZ77压缩块）
        byte[] pixels = buffer;
        int bufferLength = pixels.length - offset;
        int blockLength = 32506;
        int blockNum = 1;
        if (bufferLength % blockLength == 0)
        {
            blockNum = bufferLength / blockLength;
        }
        else
        {
            blockNum = bufferLength / blockLength + 1;
        }

        int IDATChunkLen = (bufferLength + 6 + blockNum * 5);
        dos.writeInt(IDATChunkLen);// 长度
        dos.writeInt(1229209940);// IDAT类型码
        dos.writeShort(0x78da);// 压缩信息（固定）
        for (int i = 0; i < blockNum; i++)
        {
            int off = i * blockLength;
            int len = bufferLength - off;
            if (len >= blockLength)
            {
                len = blockLength;
                lenNlen[0] = 0;
            }
            else
            {
                lenNlen[0] = 1;
            }

            int msb = len & 0xff;
            int lsb = len >>> 8;
            lenNlen[1] = (byte) msb;
            lenNlen[2] = (byte) lsb;
            lenNlen[3] = (byte) (msb ^ 0xff);
            lenNlen[4] = (byte) (lsb ^ 0xff);
            dos.write(lenNlen);// 压缩块的LEN和NLEN信息
            dos.write(pixels, off + offset, len);// 压缩数据
        }

        dos.writeInt(adler);// Adler32信息
        dos.writeInt(0);// CRC
        // 图像结束数据IEND
        dos.write(IENDChunk);
    }

    /**
     * 取得数据块的Adler32信息
     */

    private static int adler32(long adler, byte[] bs, int index, int length) {
        int base = 65521;
        int max = 5552;
        if (bs == null)
        {
            return 1;
        }

        long s1 = adler & 0xffff;
        long s2 = (adler >> 16) & 0xffff;
        int k;
        while (length > 0)
        {
            k = length < max ? length : max;
            length -= k;
            while (k >= 16)
            {
                for (int i = 0; i < 16; i++)
                {
                    s1 += bs[index++] & 0xff;
                    s2 += s1;
                }

                k -= 16;
            }

            if (k != 0)
            {
                do
                {
                    s1 += bs[index++] & 0xff;
                    s2 += s1;
                } while (--k != 0);
            }

            s1 %= base;
            s2 %= base;
        }

        return (int) ((s2 << 16) | s1);
    }

    /**
     * 写入CRC信息
     */

    private static void writeCRC(byte[] data, int pos) {
        int len = ((data[pos] & 0xff) << 24)
                | ((data[pos + 1] & 0xff) << 16)
                | ((data[pos + 2] & 0xff) << 8)
                | (data[pos + 3] & 0xff);
        int sum = CRCChecksum(data, pos + 4, 4 + len);
        pos += 8 + len;
        data[pos] = (byte) ((sum & 0xff000000) >> 24);
        data[pos + 1] = (byte) ((sum & 0xff0000) >> 16);
        data[pos + 2] = (byte) ((sum & 0xff00) >> 8);
        data[pos + 3] = (byte) (sum & 0xff);
    }

    /**
     * 循环冗余检测
     * 
     * @param bs 检测数据
     * @param offset 数据偏移
     * @param length 数据长度
     * @return 计算得到的值
     */

    private static int CRCChecksum(byte[] bs, int offset, int length) {
        if (crc_table == null)
        {
            makeCRC_table();
        }

        int c = 0xffffff;
        for (int i = offset; i < offset + length; i++)
        {
            c = crc_table[(c ^ bs[i]) & 0xff] ^ (c >>> 8);
        }

        return c ^ 0xffffffff;
    }

    /**
     * 生成冗余查询表
     */

    private static void makeCRC_table() {
        crc_table = new int[256];
        int c, n, k;
        for (n = 0; n < 256; n++)
        {
            c = n;
            for (k = 0; k < 8; k++)
            {
                if ((c & 1) != 0)
                {
                    c = 0xedb88320 ^ (c >>> 1);
                }
                else
                {
                    c >>>= 1;
                }
            }

            crc_table[n] = c;
        }
    }
}