package engine.android.util.image;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图片缓存<p>
 * 功能：防止内存溢出
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class ImageCache<Identifier> {

    private static final int DEFAULT_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);

    private static final int DEFAULT_CACHE_CAPACITY = 16;

    final LruCache<Identifier, Bitmap> imageHardCache;              // 图片硬缓存

    final Map<Identifier, ImageReference> imageSoftCache;           // 图片软缓存

    final ReferenceQueue<Bitmap> queue;                             // 垃圾回收队列

    public ImageCache() {
        this(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_CAPACITY);
    }

    /**
     * @param cacheSize 硬缓存空间
     * @param cacheCapacity 软缓存容量
     */
    public ImageCache(final int cacheSize, final int cacheCapacity) {
        imageHardCache = new LruCache<Identifier, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(Identifier key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

            @Override
            protected void entryRemoved(boolean evicted, Identifier key,
                    Bitmap oldValue, Bitmap newValue) {
                if (evicted)
                {
                    // 硬引用缓存区满，将一个最不经常使用的oldvalue推入到软引用缓存区
                    gc();
                    imageSoftCache.put(key, new ImageReference(key, oldValue));
                }
            }

            @Override
            protected Bitmap create(Identifier key) {
                // 硬引用缓存区间中读取失败，从软引用缓存区间读取
                ImageReference ref = imageSoftCache.remove(key);
                if (ref != null)
                {
                    return ref.get();
                }

                return null;
            }
        };

        /** Daimon:LinkedHashMap **/
        Map<Identifier, ImageReference> map = new LinkedHashMap<Identifier, ImageReference>(
                cacheCapacity, .75F, true) {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(
                    java.util.Map.Entry<Identifier, ImageReference> eldest) {
                return size() > cacheCapacity;
            }
        };

        imageSoftCache = Collections.synchronizedMap(map);
        queue = new ReferenceQueue<Bitmap>();
    }

    public void put(Identifier key, Bitmap value) {
        if (value != null) imageHardCache.put(key, value);
    }

    public Bitmap get(Identifier key) {
        return imageHardCache.get(key);
    }

    public void remove(Identifier key) {
        imageHardCache.remove(key);
        imageSoftCache.remove(key);
    }

    @SuppressWarnings("unchecked")
    private void gc() {
        ImageReference r = null;
        while ((r = (ImageReference) queue.poll()) != null)
        {
            imageSoftCache.remove(r.key);
        }
    }

    /**
     * 清空所有缓存图片
     */
    public void clear() {
        imageHardCache.evictAll();
        imageSoftCache.clear();
        while (queue.poll() != null)
        {
            // Do nothing.
        }

        System.gc();
    }

    private class ImageReference extends SoftReference<Bitmap> {

        Identifier key;

        public ImageReference(Identifier key, Bitmap image) {
            // （所引用的对象已经被回收，则将该引用存入队列中）
            super(image, queue);
            this.key = key;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
        .append("hard cache:").append(imageHardCache.snapshot())
        .append("soft cache:").append(imageSoftCache);
        return sb.toString();
    }
}