package engine.android.framework.ui.presenter;

import engine.android.core.BaseFragment;
import engine.android.core.BaseFragment.Presenter;
import engine.android.util.AndroidUtil;
import engine.android.util.file.FileManager;
import engine.android.util.image.ImageUtil;
import engine.android.util.image.ImageUtil.ImageDecoder;
import engine.android.util.manager.SDCardManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 提供选取图片功能
 * 
 * @author Daimon
 * @since 6/6/2014
 */
@SuppressLint("InlinedApi")
public class PhotoPresenter extends Presenter<BaseFragment> {
    
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int REQUEST_CROP_PHOTO = 3;
    
    private File photoDir;
    
    private Uri takePhoto_output;
    private Uri cropPhoto_output;
    private CropAttribute crop_attribute;
    
    private final PhotoCallback callback;
    
    public interface PhotoCallback {
        
        void onPhotoCapture(PhotoInfo info);
    }
    
    public PhotoPresenter(PhotoCallback callback) {
        this.callback = callback;
    }
    
    @Override
    protected void onCreate(Context context) {
        photoDir = FileManager.getCacheDir(context, true);
    }
    
    @Override
    protected void onDestroy() {
        FileManager.clearDir(photoDir);
    }
    
    /**
     * 拍照
     * 
     * @param saveToFile True:原图会保存到文件内,False:返回缩略图数据
     */
    public void takePhoto(boolean saveToFile, CropAttribute attr) {
        crop_attribute = attr;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        if (saveToFile)
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhoto_output
            = Uri.fromFile(new File(photoDir, System.currentTimeMillis() + ".jpg")));
        }
        else
        {
            takePhoto_output = null;
        }
        
        getCallbacks().startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    /**
     * 从相册中选取图片
     */
    public void pickPhoto(CropAttribute attr) {
        crop_attribute = attr;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
        .setType("image/*")
        .addCategory(Intent.CATEGORY_OPENABLE)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        getCallbacks().startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }
    
    /**
     * 裁剪属性设置
     */
    public static final class CropAttribute {
        
        private int aspectX,aspectY;
        private int outputX,outputY;
        private boolean scale,circleCrop;
        private boolean saveToFile;
        
        public CropAttribute() {
            aspectX = aspectY = 1;
            outputX = outputY = 200;
            scale = true;
        }

        /**
         * 设置裁剪比例
         */
        public CropAttribute setAspectRatio(int aspectX, int aspectY) {
            this.aspectX = aspectX;
            this.aspectY = aspectY;
            return this;
        }
        
        /**
         * 设置输出图像尺寸
         */
        public CropAttribute setOutputSize(int outputX, int outputY) {
            this.outputX = outputX;
            this.outputY = outputY;
            return this;
        }
        
        /**
         * 设置图片能否缩放
         */
        public CropAttribute setScale(boolean scale) {
            this.scale = scale;
            return this;
        }
        
        /**
         * 设置裁剪形状是否为圆形
         */
        public CropAttribute setCircleCrop(boolean circleCrop) {
            this.circleCrop = circleCrop;
            return this;
        }
        
        /**
         * 有些手机裁剪出来的图片小于预定值，
         * 如需要固定尺寸，需要保存到SD卡文件内
         */
        public CropAttribute saveToFile() {
            saveToFile = true;
            return this;
        }
    }

    /**
     * 裁剪图片
     */
    private void cropPhoto(PhotoInfo info, CropAttribute attr) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        
        if (info.uri != null)
        {
            intent.setDataAndType(info.uri, "image/*");
        }
        else
        {
            intent.setType("image/*");
            intent.putExtra("data", info.getData());
        }

        intent.putExtra("crop", "true");
        
        intent.putExtra("aspectX", attr.aspectX);
        intent.putExtra("aspectY", attr.aspectY);
        
        intent.putExtra("outputX", attr.outputX);
        intent.putExtra("outputY", attr.outputY);
        
        intent.putExtra("scale", attr.scale);
        intent.putExtra("circleCrop", attr.circleCrop);
        intent.putExtra("noFaceDetection", true);
        // 去除黑边
        intent.putExtra("scaleUpIfNeeded", true);
        
        if (attr.saveToFile)
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropPhoto_output
            = Uri.fromFile(new File(photoDir, System.currentTimeMillis() + ".jpg")));
        }
        else
        {
            cropPhoto_output = null;
            intent.putExtra("return-data", true);
        }

        getCallbacks().startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || callback == null)
        {
            return;
        }
        
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
            {
                PhotoInfo info = new PhotoInfo();
                if (takePhoto_output != null)
                {
                    info.uri = takePhoto_output;
                }
                else if (data != null)
                {
                    info.uri = data.getData();
                    info.data = data.getExtras();
                }
                
                if (crop_attribute != null)
                {
                    cropPhoto(info, crop_attribute);
                }
                else
                {
                    callback.onPhotoCapture(info);
                }

                break;
            }
            case REQUEST_PICK_PHOTO:
                if (data != null && data.getData() != null)
                {
                    PhotoInfo info = new PhotoInfo();
                    info.uri = DocumentUtil.getUri(getCallbacks().getContext(), data.getData());
                    
                    if (crop_attribute != null)
                    {
                        cropPhoto(info, crop_attribute);
                    }
                    else
                    {
                        callback.onPhotoCapture(info);
                    }
                }
                
                break;
            case REQUEST_CROP_PHOTO:
                PhotoInfo info = new PhotoInfo();
                if (cropPhoto_output != null)
                {
                    info.uri = cropPhoto_output;
                }
                else if (data != null)
                {
                    info.uri = data.getData();
                    info.data = data.getExtras();
                }
                
                callback.onPhotoCapture(info);
                break;
        }
    }
    
    /**
     * 照片信息
     */
    public static final class PhotoInfo implements Parcelable {
        
        Uri uri;
        
        Bundle data;
        
        PhotoInfo() {}
        
        private Bitmap getData() {
            if (data != null)
            {
                return data.getParcelable("data");
            }
            
            return null;
        }
        
        public File getPhotoFile() {
            if (uri != null && ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
            {
                return new File(uri.getPath());
            }
            
            return null;
        }
        
        public Bitmap getPhoto(ContentResolver cr) {
            if (uri != null)
            {
                return getImage(cr, uri);
            }
            
            return getData();
        }
        
        /**
         * 缩放相片到指定宽高
         */
        public Bitmap getPhoto(ContentResolver cr, int width, int height, 
                boolean fitXY) {
            if (uri != null)
            {
                return getThumbnail(cr, uri, width, height, fitXY);
            }
            
            Bitmap image = getData();
            if (image != null)
            {
                if (fitXY)
                {
                    image = ImageUtil.zoom(image, width, height);
                }
                else
                {
                    int zoomSize = calculateZoomSize(image, width, height);
                    if (zoomSize > 1)
                    {
                        int newW = image.getWidth() / zoomSize;
                        int newH = image.getHeight() / zoomSize;
                        image = ImageUtil.zoom(image, newW, newH);
                    }
                }
            }
            
            return image;
        }

        private PhotoInfo(Parcel source) {
            uri = source.readParcelable(null);
            data = source.readBundle();
        }

        public static final Parcelable.Creator<PhotoInfo> CREATOR
        = new Creator<PhotoInfo>() {

            @Override
            public PhotoInfo createFromParcel(Parcel source) {
                return new PhotoInfo(source);
            }

            @Override
            public PhotoInfo[] newArray(int size) {
                return new PhotoInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(uri, 0);
            dest.writeBundle(data);
        }
    }
    
    public static Bitmap getImage(ContentResolver cr, Uri uri) {
        try {
            int angle = getImageOrientation(cr, uri);
            Bitmap image = Media.getBitmap(cr, uri);
            return ImageUtil.rotate(image, angle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static Bitmap getThumbnail(ContentResolver cr, Uri uri, 
            int width, int height, boolean fitXY) {
        try {
            int angle = getImageOrientation(cr, uri);
            if ((angle / 90) % 2 != 0)
            {
                width  = width ^ height;
                height = width ^ height;
                width  = width ^ height;
            }
            
            AssetFileDescriptor fd = cr.openAssetFileDescriptor(uri, "r");
            Bitmap image = ImageDecoder.decodeFileDescriptor(
                    fd.getFileDescriptor(), 
                    width, 
                    height, 
                    fitXY);
            fd.close();
            
            return ImageUtil.rotate(image, angle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static int getImageOrientation(ContentResolver cr, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
        {
            return readJPEGDegree(uri.getPath());
        }
        
        int angle = 0;

        String[] columns = new String[]{Media.ORIENTATION};
        Cursor cursor = Media.query(cr, uri, columns);
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                angle = cursor.getInt(0);
            }
            
            cursor.close();
        }
        
        return angle;
    }
    
    /**
     * 读取JPEG图片属性：旋转的角度
     * 
     * @param path 图片绝对路径
     */
    public static int readJPEGDegree(String path) {
        int angle = 0;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return angle;
    }

    public static int calculateZoomSize(Bitmap image, int width, int height) {
        int w = image.getWidth();
        int h = image.getHeight();
        // 计算缩放比例
        int zoomSize = 1;
        int scaleW = Math.round(w * 1.0f / width);
        int scaleH = Math.round(h * 1.0f / height);
        if (scaleW > 1 && scaleH > 1)
        {
            zoomSize = Math.min(scaleW, scaleH);
            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image.
            final float totalPixels = w * h;
            // Anything more than 2x the requested pixels we'll sample down further.
            final float totalPixelsCap = width * height * 2;

            while (totalPixels / (zoomSize * zoomSize) > totalPixelsCap)
            {
                zoomSize++;
            }
        }

        return zoomSize;
    }
}

class DocumentUtil {
    
    /**
     * 为了兼容4.4版本返回路径
     */
    public static Uri getUri(Context context, Uri uri) {
        boolean isKitKat = AndroidUtil.getVersion() >= 19;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri))
        {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri))
            {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type))
                {
                    return Uri.fromFile(SDCardManager.openSDCardFile(split[1]));
                }
                
                //  TODO handle non-primary volumes  
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri))
            {
                String id = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), 
                        Long.valueOf(id));
                return Uri.fromFile(new File(getDataColumn(context, contentUri, null, null)));
            }
            // MediaProvider
            else if (isMediaDocument(uri))
            {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                
                Uri contentUri = null;
                if ("image".equals(type))
                {
                    contentUri = Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type))
                {
                    contentUri = Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type))
                {
                    contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
                }
                
                return ContentUris.withAppendedId(contentUri, Long.valueOf(split[1]));
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            // Return the remote address
            if (isGooglePhotosUri(uri))
            {
                return Uri.fromFile(new File(uri.getLastPathSegment()));
            }
            
            return uri;
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri;
        }
      
        return uri;
    }
    
    /** 
     * Get the value of the data column for this Uri. This is useful for 
     * MediaStore Uris, and other file-based ContentProviders. 
     * 
     * @param context The context. 
     * @param uri The Uri to query. 
     * @param selection (Optional) Filter used in the query. 
     * @param selectionArgs (Optional) Selection arguments used in the query. 
     * @return The value of the _data column, which is typically a file path. 
     */  
    private static String getDataColumn(Context context, Uri uri, 
            String selection, String[] selectionArgs) {
        String path = null;

        String[] columns = {"_data"};
        Cursor cursor = context.getContentResolver().query(
                uri, columns, selection, selectionArgs, null);
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                path = cursor.getString(0);
            }
            
            cursor.close();
        }
        
        return path;
    }  
    
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

/**
 * 参考API4.4文档
 */
class DocumentsContract {
    
    private static final String PATH_DOCUMENT = "document";
    private static final String PATH_TREE = "tree";
    
    private DocumentsContract() {}

    public static boolean isDocumentUri(Context context, Uri uri) {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() == 2 && PATH_DOCUMENT.equals(paths.get(0))) {
            return isDocumentsProvider(context, uri.getAuthority());
        }
        if (paths.size() == 4 && PATH_TREE.equals(paths.get(0))
                && PATH_DOCUMENT.equals(paths.get(2))) {
            return isDocumentsProvider(context, uri.getAuthority());
        }
        return false;
    }

    private static boolean isDocumentsProvider(Context context, String authority) {
        final List<ProviderInfo> infos = context.getPackageManager()
                .queryContentProviders(null, 0, 0);
        for (ProviderInfo info : infos) {
            if (authority.equals(info.authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract the {@link Document#COLUMN_DOCUMENT_ID} from the given URI.
     *
     * @see #isDocumentUri(Context, Uri)
     */
    public static String getDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() >= 2 && PATH_DOCUMENT.equals(paths.get(0))) {
            return paths.get(1);
        }
        if (paths.size() >= 4 && PATH_TREE.equals(paths.get(0))
                && PATH_DOCUMENT.equals(paths.get(2))) {
            return paths.get(3);
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }
}