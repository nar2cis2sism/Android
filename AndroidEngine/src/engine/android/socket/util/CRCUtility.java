package engine.android.socket.util;

/**
 * =============================================================================<br>
 * 类型名称: CRCUtility<br>
 * 类型版本: V1.0<br>
 * 类型描述: CRC校验生成工具类 <br>
 * 类型作者: Dennis <br>
 * 创建日期: 2010-08-12 <br>
 * 类型备注: <br>
 * =============================================================================<br>
 * 修改日期 修改前版本 修改后版本 修改人 备注<br>
 * =============================================================================
 */

public final class CRCUtility {

    public static int[] CRC16table = { 0xf1c0, 0xf248, 0x553e, 0xae68, 0xc753, 0x6269, 0x9a19,
            0x7fed, 0xb010, 0x4d44, 0x6d07, 0x9ec0, 0x578c, 0xbb57, 0x07f1, 0x3d1f, 0x6944, 0x1f29,
            0x014d, 0xce4a, 0x08b5, 0x6f29, 0xdb33, 0x0c96, 0x1e8b, 0x2045, 0x90b0, 0x676f, 0xb3c1,
            0x9316, 0xcc1f, 0x8e54, 0xc1ea, 0x65a2, 0xa28b, 0xe271, 0x5801, 0x9c97, 0x636e, 0x31f1,
            0xc563, 0x06cb, 0x1145, 0xac9b, 0x38ed, 0xeadc, 0xbecb, 0xc577, 0xf853, 0x49f0, 0x25e6,
            0x7cbf, 0x9424, 0xd1b9, 0xa882, 0x71b2, 0x571b, 0x45f9, 0x58ed, 0x4545, 0x33b1, 0xd356,
            0xf677, 0xd606, 0xb103, 0x10bb, 0xcc60, 0x53ef, 0x608f, 0x5bcb, 0x6458, 0xa920, 0x485a,
            0xb492, 0xd323, 0x5cc6, 0xf96f, 0x9c72, 0x5d16, 0x3655, 0xd0e1, 0x89c5, 0x198d, 0xe965,
            0xb1b7, 0x1c39, 0x6790, 0x44f9, 0x7069, 0x103f, 0x4338, 0xc585, 0xbcce, 0x8327, 0x2a91,
            0x661d, 0xa5b9, 0xcac7, 0x1218, 0x6e6b, 0x6996, 0xe1b2, 0x3bfc, 0x79b6, 0x39b6, 0xd112,
            0x6ace, 0x81cf, 0x7239, 0xcc8d, 0x2f46, 0x1518, 0x9ebd, 0x1f35, 0xca3e, 0x7b97, 0x0428,
            0xb3db, 0x9723, 0xa54b, 0x6253, 0x0a2e, 0x005e, 0x6517, 0xc461, 0xbd05, 0xee83, 0xf766,
            0x9500, 0x87f5, 0x4451, 0x261e, 0x53f0, 0x7980, 0x9cbf, 0xbad8, 0x4c77, 0x20bb, 0xf5b3,
            0xfd02, 0x18b7, 0x3e5a, 0x890f, 0x84d0, 0xa3fa, 0xc444, 0x9f36, 0xe02e, 0x4e70, 0xc951,
            0xf13f, 0x7bea, 0xdefc, 0x647e, 0x0e6d, 0xa714, 0xa3f3, 0xb406, 0x77a2, 0xb725, 0x9207,
            0x034f, 0x94e7, 0x5abd, 0xe8b4, 0xe576, 0x9c46, 0x4e42, 0xf5df, 0xdfc3, 0xc680, 0xd4d5,
            0x8e90, 0x7123, 0x1569, 0x5b4f, 0xc8e8, 0x0c3f, 0x48f3, 0x504d, 0x03c8, 0xda9b, 0xbb2a,
            0xb03f, 0x62c4, 0x066e, 0x88b2, 0x05d5, 0x294d, 0x7f9e, 0xfa83, 0xafd5, 0xde40, 0xe0be,
            0x66f9, 0xb991, 0x693d, 0x7b30, 0x0376, 0xa964, 0x7d70, 0x465e, 0x3520, 0xebda, 0x31ad,
            0xecb4, 0x2686, 0xdae9, 0xac17, 0x9c32, 0x9130, 0x6e08, 0xd7a9, 0x780d, 0x1568, 0x1792,
            0x444d, 0xdd86, 0xf7b9, 0x8315, 0x2678, 0x9ae3, 0xfafa, 0x392f, 0xf95b, 0x9833, 0x1ee2,
            0x9be5, 0x1f23, 0x27ae, 0x9e74, 0x64f4, 0x0ce9, 0xc452, 0x6ec1, 0xa54c, 0xac38, 0xadbd,
            0x05dc, 0xa5f1, 0xb25b, 0xad01, 0x2aed, 0xd3df, 0x4dcb, 0xa5a7, 0x4bbf, 0x05b7, 0xc477,
            0xed46, 0x2150, 0xc427, 0x01bd, 0x0059, 0x9c1d, 0xa457 };

    /**
     * 计算数据{data}区域[0..clen-1]段的CRC值 <br>
     * CRC结果在data[clen]和data[clen+1]两个数据位
     */
    public final static byte[] generate(byte[] data, int clen) {
        int crcs = calculate(data, clen);
        if (clen + 2 > data.length) {
            byte[] news = new byte[clen + 2];
            System.arraycopy(data, 0, news, 0, clen);
            news[clen] = (byte) ((crcs >>> 0) & 0xff);
            news[clen + 1] = (byte) ((crcs >>> 8) & 0xff);
            return news;
        }
        else {
            data[clen] = (byte) ((crcs >>> 0) & 0xff);
            data[clen + 1] = (byte) ((crcs >>> 8) & 0xff);
            return data;
        }
    }

    /**
     * 计算数据{data}区域[boff..boff+clen-1]段的CRC值 <br>
     * CRC结果在data[boff+clen]和data[boff+clen+1]两个数据位
     */
    public final static byte[] generate(byte[] data, int boff, int clen) {
        int crcs = calculate(data, boff, clen);
        if (clen + boff + 2 > data.length) {
            byte[] news = new byte[clen + boff + 2];
            System.arraycopy(data, 0, news, 0, data.length);
            news[clen + boff] = (byte) ((crcs >>> 0) & 0xff);
            news[clen + boff + 1] = (byte) ((crcs >>> 8) & 0xff);
            return news;
        }
        else {
            data[clen + boff] = (byte) ((crcs >>> 0) & 0xff);
            data[clen + boff + 1] = (byte) ((crcs >>> 8) & 0xff);
            return data;
        }
    }

    /**
     * 验证{data}区域[0..clen-1]段的CRC值 <br>
     * CRC保存在data[clen]和data[clen+1]两个数据位
     */
    public final static boolean validate(byte[] data, int clen) {
        return validate(data, 0, clen);
    }

    /**
     * 验证{data}区域[boff..boff+clen-1]段的CRC值 <br>
     * CRC保存在data[boff+clen]和data[boff+clen+1]两个数据位
     */
    public final static boolean validate(byte[] data, int boff, int clen) {
        if (clen + boff + 2 > data.length) {
            return false;
        }
        else {
            int crcs = calculate(data, boff, clen);
            int bool = (data[clen + boff] & 0xff) - ((crcs >>> 0) & 0xff);
            return bool == 0 && ((data[clen + boff + 1] & 0xff) == ((crcs >>> 8) & 0xff));
        }
    }

    /**
     * 计算{data}区域[0..len-1]段的CRC值
     */
    public final static int calculate(byte[] data, int len) {
        return calculate(data, 0, len);
    }

    /**
     * 计算{data}区域[boff..boff+len-1]段的CRC值
     */
    public final static int calculate(byte[] data, int boff, int len) {
        int crcIndex = 0, crcEntry = 0;
        for (int idx = boff; idx < boff + len; idx++) {
            crcEntry = CRC16table[crcIndex & 0xF | (data[idx] & 0x0f) << 0x4];
            crcIndex = (crcIndex >>> 0x4 ^ crcEntry);
            crcEntry = CRC16table[crcIndex & 0xF | data[idx] & 0xf0];
            crcIndex = (crcIndex >>> 0x4 ^ crcEntry);
        }
        return crcIndex;
    }
}
