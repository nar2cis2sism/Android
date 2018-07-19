package engine.android.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.provider.MediaStore;

import engine.android.util.image.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 缩略图工具类
 * 
 * @author Daimon
 * @version 3.0
 * @since 3/26/2012
 */

public final class ThumbUtil {

    /**
     * 加载图片
     * 
     * @param pathName 图片文件名称
     * @param scale 是否进行缩放，设置为true可有效节约内存
     */

    public static Bitmap load(String pathName, boolean scale) {
        if (scale)
        {
            Options opt = new Options();
            opt.inSampleSize = 2;						// 设置缩小比例
            return BitmapFactory.decodeFile(pathName, opt);
        }
        else
        {
            return BitmapFactory.decodeFile(pathName);
        }
    }

    /**
     * 加载固定尺寸的图片（为了节约内存，做了一些处理）
     * 
     * @param pathName 图片文件路径
     * @param width,height 图片显示尺寸
     */

    public static Bitmap load(String pathName, int width, int height) {
        Options opt = new Options();
        opt.inJustDecodeBounds = true;				// 设置为true表示我们只读取Bitmap的宽高等信息，不读取像素

        BitmapFactory.decodeFile(pathName, opt);	// 获取尺寸信息
        // 计算缩放比例
        int scaleW = (int) Math.ceil(opt.outWidth * 1.0 / width);
        int scaleH = (int) Math.ceil(opt.outHeight * 1.0 / height);
        if (scaleW > 1 && scaleH > 1)
        {
            if (scaleW > scaleH)
            {
                opt.inSampleSize = scaleW;
            }
            else
            {
                opt.inSampleSize = scaleH;
            }
        }

        opt.inJustDecodeBounds = false;
        return ImageUtil.zoom(BitmapFactory.decodeFile(pathName, opt), width, height);
    }

    /**
     * 获取所有缩略图
     */

    public static List<Bitmap> getThumbnail(ContentResolver r) {
        List<Bitmap> list = new ArrayList<Bitmap>();
        Cursor cursor = queryThumbnail(r, null, null);
        while (cursor.moveToNext())
        {
            String path = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            list.add(load(path, 100, 100));
        }

        cursor.close();
        return list;
    }

    /**
     * 查询缩略图
     */

    private static Cursor queryThumbnail(ContentResolver r, 
            String selection, String[] selectionArgs) {
        String[] columns = new String[] { 
                MediaStore.Images.Thumbnails._ID,           // 缩略图ID
                MediaStore.Images.Thumbnails.DATA,          // 缩略图数据
                MediaStore.Images.Thumbnails.IMAGE_ID };    // 原图ID

        return r.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                columns, selection, selectionArgs, 
                MediaStore.Images.Thumbnails.DEFAULT_SORT_ORDER);
    }

    /**
     * 通过缩略图ID查询
     */

    public static Bitmap queryThumbnailById(ContentResolver r, int thumbId) {
        String selection = MediaStore.Images.Thumbnails._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(thumbId) };
        Cursor cursor = queryThumbnail(r, selection, selectionArgs);
        if (cursor.moveToFirst())
        {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Thumbnails.DATA));
            cursor.close();
            return load(path, 100, 100);
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    /**
     * 获取所有原图
     */

    public static List<Bitmap> getImage(ContentResolver r) {
        List<Bitmap> list = new ArrayList<Bitmap>();
        Cursor cursor = queryImage(r, null, null);
        while (cursor.moveToNext())
        {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATA));
            list.add(load(path, false));
        }

        cursor.close();
        return list;
    }

    /**
     * 查询原图
     */

    private static Cursor queryImage(ContentResolver r, String selection, String[] selectionArgs) {
        String[] columns = new String[] { 
                MediaStore.Images.Media._ID,            // 原图ID
                MediaStore.Images.Media.DATA,           // 原图数据
                MediaStore.Images.Media.DISPLAY_NAME }; // 原图文件名称

        return r.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, selection, selectionArgs, 
                MediaStore.Images.Media.DEFAULT_SORT_ORDER);
    }

    /**
     * 通过原图ID查询
     */

    public static Bitmap queryImageById(ContentResolver r, int imageId) {
        String selection = MediaStore.Images.Media._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(imageId) };
        Cursor cursor = queryImage(r, selection, selectionArgs);
        if (cursor.moveToFirst())
        {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATA));
            cursor.close();
            return load(path, false);
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    /**
     * 通过缩略图ID获取原图片
     */

    public static Bitmap getImageByThumbId(ContentResolver r, int thumbId) {
        String selection = MediaStore.Images.Thumbnails._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(thumbId) };
        Cursor cursor = queryThumbnail(r, selection, selectionArgs);
        if (cursor.moveToFirst())
        {
            int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Thumbnails.IMAGE_ID));
            cursor.close();
            return queryImageById(r, imageId);
        }
        else
        {
            cursor.close();
            return null;
        }
    }
}