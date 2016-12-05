package engine.android.util.secure;

import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A SharedPreferences implementation where the preferences map is persisted as
 * a single secure String
 * 
 * @author Daimon
 * @version N
 * @since 3/22/2012
 */
public class SecureSharedPreferences implements SharedPreferences {

    private static final String secure_key = "secure_key";

    private final SharedPreferences sp;

    private final Map<String, Object> map;

    public SecureSharedPreferences(SharedPreferences sp) {
        Map<String, Object> map = load(this.sp = sp);
        this.map = map != null ? map : new HashMap<String, Object>();
    }

    private Map<String, Object> load(SharedPreferences sp) {
        Map<String, Object> map = null;
        String s = sp.getString(secure_key, null);
        if (s != null)
        {
            try {
                map = deserialize(decrypt(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            map = new HashMap<String, Object>(sp.getAll());
        }

        return map;
    }

    @Override
    public synchronized Map<String, ?> getAll() {
        return map;
    }

    @Override
    public synchronized String getString(String key, String defValue) {
        String value = (String) map.get(key);
        return value != null ? value : defValue;
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>) map.get(key);
        return value != null ? value : defValues;
    }

    @Override
    public synchronized int getInt(String key, int defValue) {
        Integer value = (Integer) map.get(key);
        return value != null ? value : defValue;
    }

    @Override
    public synchronized long getLong(String key, long defValue) {
        Long value = (Long) map.get(key);
        return value != null ? value : defValue;
    }

    @Override
    public synchronized float getFloat(String key, float defValue) {
        Float value = (Float) map.get(key);
        return value != null ? value : defValue;
    }

    @Override
    public synchronized boolean getBoolean(String key, boolean defValue) {
        Boolean value = (Boolean) map.get(key);
        return value != null ? value : defValue;
    }

    @Override
    public synchronized boolean contains(String key) {
        return map.containsKey(key);
    }

    @Override
    public SharedPreferences.Editor edit() {
        return new Editor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Standard Java serialization of the SharedPreferences map<br>
     * User can customize
     */
    protected byte[] serialize(Map<String, Object> map) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            oos.writeObject(map);
        } finally {
            oos.close();
        }

        return baos.toByteArray();
    }

    /**
     * Standard Java deserialization of the SharedPreferences map<br>
     * User can customize
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> deserialize(byte[] data) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        try {
            return (Map<String, Object>) ois.readObject();
        } finally {
            ois.close();
        }
    }

    protected String encrypt(byte[] data) throws Exception {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    protected byte[] decrypt(String s) throws Exception {
        return Base64.decode(s, Base64.DEFAULT);
    }

    private class Editor implements SharedPreferences.Editor {

        private final List<Edit> list = new LinkedList<Edit>();

        @Override
        public android.content.SharedPreferences.Editor putString(String key, String value) {
            list.add(new Put(key, value));
            return this;
        }

        @Override
        public android.content.SharedPreferences.Editor putInt(String key, int value) {
            list.add(new Put(key, value));
            return this;
        }

        @Override
        public android.content.SharedPreferences.Editor putLong(String key, long value) {
            list.add(new Put(key, value));
            return this;
        }

        @Override
        public android.content.SharedPreferences.Editor putFloat(String key, float value) {
            list.add(new Put(key, value));
            return this;
        }

        @Override
        public android.content.SharedPreferences.Editor putBoolean(String key, boolean value) {
            list.add(new Put(key, value));
            return this;
        }

        @Override
        public android.content.SharedPreferences.Editor remove(String key) {
            list.add(new Remove(key));
            return this;
        }

        @Override
        public android.content.SharedPreferences.Editor clear() {
            list.add(new Clear());
            return this;
        }

        @Override
        public boolean commit() {
            boolean commit = false;
            synchronized (SecureSharedPreferences.this) {
                Map<String, Object> save = new HashMap<String, Object>(map);
                for (Edit edit : list)
                {
                    edit.edit(map);
                }

                list.clear();
                try {
                    byte[] data = serialize(map);
                    commit = sp.edit()
                            .clear()
                            .putString(secure_key, encrypt(data))
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (!commit)
                    {
                        map.clear();
                        map.putAll(save);
                    }
                }
            }

            return commit;
        }

        public void apply() {
            commit();
        }

        public android.content.SharedPreferences.Editor putStringSet(
                String arg0, Set<String> arg1) {
            list.add(new Put(arg0, arg1));
            return this;
        }
    }

    private static abstract class Edit {

        abstract void edit(Map<String, Object> map);
    }

    private static class Put extends Edit {

        private final String key;

        private final Object value;

        Put(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        void edit(Map<String, Object> map) {
            map.put(key, value);
        }
    }

    private static class Remove extends Edit {

        private final String key;

        Remove(String key) {
            this.key = key;
        }

        @Override
        void edit(Map<String, Object> map) {
            map.remove(key);
        }
    }

    private static class Clear extends Edit {

        @Override
        void edit(Map<String, Object> map) {
            map.clear();
        }
    }
}