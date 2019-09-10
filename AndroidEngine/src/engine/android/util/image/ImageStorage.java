package engine.android.util.image;

import engine.android.util.file.FileManager;
import engine.android.util.file.FileUtils;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片存储工具（存储于文件目录内）
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class ImageStorage {

    private final ConcurrentHashMap<String, File> map
    = new ConcurrentHashMap<String, File>();

    private final File storageDir;

    public ImageStorage(File storageDir) {
        this.storageDir = storageDir;
    }

    public boolean put(String key, Bitmap value) {
        if (value != null)
        {
            File file = getImageFile(key);
            synchronized (file) {
                return saveImage(file, value);
            }
        }

        return false;
    }

    public boolean put(String key, byte[] data) {
        if (data != null)
        {
            File file = getImageFile(key);
            synchronized (file) {
                return saveImage(file, data);
            }
        }

        return false;
    }

    public boolean put(String key, InputStream is) {
        if (is != null)
        {
            File file = getImageFile(key);
            synchronized (file) {
                return saveImage(file, is);
            }
        }

        return false;
    }

    public Bitmap get(String key) {
        File file = getImageFile(key);
        synchronized (file) {
            return file.exists() ? readImage(file) : null;
        }
    }

    public void remove(String key) {
        File file = getImageFile(key);
        synchronized (file) {
            FileManager.delete(file);
        }
    }

    public void clear() {
        FileManager.clearDir(storageDir);
    }

    public File getStorageDir() {
        return storageDir;
    }

    private File getImageFile(String key) {
        File file = map.get(key);
        if (file == null)
        {
            file = map.putIfAbsent(key, new File(storageDir, generateImageName(key)));
            if (file == null)
            {
                file = map.get(key);
            }
        }

        return file;
    }

    protected String generateImageName(String key) {
        return HexUtil.encode(CryptoUtil.SHA1(key.getBytes()));
    }

    protected boolean saveImage(File file, Bitmap image) {
        try {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                return image.compress(image.hasAlpha() ? CompressFormat.PNG : CompressFormat.JPEG, 100, fos);
            } finally {
                if (fos != null)
                {
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    protected boolean saveImage(File file, byte[] data) {
        return FileManager.writeFile(file, data, false);
    }

    protected boolean saveImage(File file, InputStream is) {
        return FileUtils.copyToFile(is, file);
    }

    protected Bitmap readImage(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}