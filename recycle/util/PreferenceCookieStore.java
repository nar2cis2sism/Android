package engine.android.http.util.extra;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import engine.android.util.secure.HexUtil;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存到Preference的Cookie实现
 * <未验证>
 * 
 * @author Daimon
 * @version 3.0
 * @since 7/29/2013
 */

public class PreferenceCookieStore implements CookieStore {

    private static final String COOKIE_PREFS = "COOKIE_PREFS";

    private static final String COOKIE_KEY_NAMES = "NAMES";
    private static final String COOKIE_KEY_PREFIX = "COOKIE_";

    private final SharedPreferences cookiePrefs;
    private final ConcurrentHashMap<String, Cookie> cookies;

    public PreferenceCookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE);
        cookies = new ConcurrentHashMap<String, Cookie>();

        // Load any previously stored cookies into the store
        String storedCookieNames = cookiePrefs.getString(COOKIE_KEY_NAMES, null);
        if (storedCookieNames != null)
        {
            String[] cookieNames = storedCookieNames.split(",");
            for (String name : cookieNames)
            {
                String serializedCookie = cookiePrefs.getString(COOKIE_KEY_PREFIX + name, null);
                if (serializedCookie != null)
                {
                    Cookie cookie = deserialize(serializedCookie);
                    if (cookie != null)
                    {
                        cookies.put(name, cookie);
                    }
                }
            }
        }

        // Clear out expired cookies
        clearExpired(new Date());
    }

    @Override
    public void addCookie(Cookie cookie) {
        String name = cookie.getName();
        // Save cookie into local store, or remove if expired
        if (cookie.isExpired(new Date()))
        {
            cookies.remove(name);
        }
        else
        {
            cookies.put(name, cookie);
        }

        // Save cookie into persistent store
        cookiePrefs.edit()
                .putString(COOKIE_KEY_NAMES, TextUtils.join(",", cookies.keySet()))
                .putString(COOKIE_KEY_PREFIX + name, serialize(cookie))
                .commit();
    }

    @Override
    public void clear() {
        // Clear cookies from persistent store
        Editor editor = cookiePrefs.edit();
        for (String name : cookies.keySet())
        {
            editor.remove(COOKIE_KEY_PREFIX + name);
        }

        editor.remove(COOKIE_KEY_NAMES);
        editor.commit();

        // Clear cookies from local store
        cookies.clear();
    }

    @Override
    public boolean clearExpired(Date date) {
        boolean clearExpired = false;
        Editor editor = cookiePrefs.edit();
        for (Entry<String, Cookie> entry : cookies.entrySet())
        {
            String name = entry.getKey();
            Cookie cookie = entry.getValue();
            if (cookie.isExpired(date))
            {
                cookies.remove(name);
                editor.remove(COOKIE_KEY_PREFIX + name);
                clearExpired = true;
            }
        }

        if (clearExpired)
        {
            // Update names in persistent store
            editor.putString(COOKIE_KEY_NAMES, TextUtils.join(",", cookies.keySet()));
        }

        editor.commit();
        return clearExpired;
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    private static String serialize(Cookie cookie) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(new SerializableCookie(cookie));
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return HexUtil.encode(baos.toByteArray());
    }

    private static Cookie deserialize(String s) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                    HexUtil.decode(s)));
            return ((SerializableCookie) ois.readObject()).getCookie();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class SerializableCookie implements Serializable {

        private static final long serialVersionUID = -3362365305823477531L;

        private final Cookie cookie;

        private BasicClientCookie basicCookie;

        public SerializableCookie(Cookie cookie) {
            this.cookie = cookie;
        }

        public Cookie getCookie() {
            if (basicCookie != null)
            {
                return basicCookie;
            }

            return cookie;
        }

        private void writeObject(ObjectOutputStream out) throws IOException
        {
            out.writeUTF(cookie.getName());
            out.writeUTF(cookie.getValue());
            out.writeUTF(cookie.getComment());
            out.writeUTF(cookie.getDomain());
            out.writeObject(cookie.getExpiryDate());
            out.writeUTF(cookie.getPath());
            out.writeInt(cookie.getVersion());
            out.writeBoolean(cookie.isSecure());
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
            basicCookie = new BasicClientCookie(in.readUTF(), in.readUTF());
            basicCookie.setComment(in.readUTF());
            basicCookie.setDomain(in.readUTF());
            basicCookie.setExpiryDate((Date) in.readObject());
            basicCookie.setPath(in.readUTF());
            basicCookie.setVersion(in.readInt());
            basicCookie.setSecure(in.readBoolean());
        }
    }
}