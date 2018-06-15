package engine.android.util.extra;

import java.util.HashMap;

/**
 * 功能：缓存数据一定时间后过期
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class KeyExpiryMap<K, V> {

    private final HashMap<K, Entry<V>> values;

    public KeyExpiryMap() {
        values = new HashMap<K, Entry<V>>();
    }

    public KeyExpiryMap(int initialCapacity) {
        values = new HashMap<K, Entry<V>>(initialCapacity);
    }

    public KeyExpiryMap(int initialCapacity, float loadFactor) {
        values = new HashMap<K, Entry<V>>(initialCapacity, loadFactor);
    }
    
    public boolean isExpired(K key) {
        return getEntry(key) == null;
    }

    public V put(K key, V value, long expiryTime) {
        if (key == null)
        {
            throw new NullPointerException("Key is not allowed Null.");
        }

        Entry<V> oldEntry = values.put(key, new Entry<V>(value, System.currentTimeMillis(), expiryTime));
        return oldEntry == null ? null : oldEntry.value;
    }

    public V get(K key) {
        Entry<V> value = getEntry(key);
        return value == null ? null : value.value;
    }
    
    private Entry<V> getEntry(K key) {
        Entry<V> value = values.get(key);
        if (value != null && System.currentTimeMillis() - value.putTimeStamp >= value.expireTimeStamp)
        {
            remove(key);
            value = null;
        }

        return value;
    }

    public V remove(K key) {
        Entry<V> oldEntry = values.remove(key);
        return oldEntry == null ? null : oldEntry.value;
    }

    public void clear() {
        values.clear();
    }
    
    @Override
    public String toString() {
        return values.toString();
    }
    
    private static class Entry<V> {
        
        public final V value;
        public final long putTimeStamp;
        public final long expireTimeStamp;
        
        public Entry(V value, long putTimeStamp, long expireTimeStamp) {
            this.value = value;
            this.putTimeStamp = putTimeStamp;
            this.expireTimeStamp = expireTimeStamp;
        }
    }
}