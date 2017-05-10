package demo.android.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具
 * 
 * @author Daimon
 * @version 3.0
 * @since 11/2/2012
 */

public final class QRCodeUtil {

    /**
     * 生成二维码图片
     * 
     * @param text 编码信息
     * @param width,height 图片大小
     */

    public static Bitmap encode(String text, int width, int height) {
        if (TextUtils.isEmpty(text))
        {
            return null;
        }

        try {
            Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
            // 设置编码类型
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            QRCodeWriter writer = new QRCodeWriter();
            /*
             * 第一个参数：编码文本 第二个参数：条形码样式－》二维码 第三个参数：图片宽度 第四个参数：图片高度 第五个参数：map保存编码设置
             */
            BitMatrix bm = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            int w = bm.getWidth();
            int h = bm.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    if (bm.get(x, y))
                    {
                        // 黑点
                        pixels[y * w + x] = Color.BLACK;
                    }
                    else
                    {
                        pixels[y * w + x] = Color.WHITE;
                    }
                }
            }

            return Bitmap.createBitmap(pixels, w, h, Config.ARGB_8888);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 解析二维码图片
     * 
     * @return 解码信息
     */

    public static String decode(Bitmap image) {
        if (image == null)
        {
            return null;
        }

        try {
            int w = image.getWidth();
            int h = image.getHeight();
            int[] pixels = new int[w * h];
            image.getPixels(pixels, 0, w, 0, 0, w, h);

            RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
            BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
            // 设置解码类型
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");

            QRCodeReader reader = new QRCodeReader();
            return reader.decode(bb, hints).getText();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}