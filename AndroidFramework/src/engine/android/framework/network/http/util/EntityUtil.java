package engine.android.framework.network.http.util;

import android.text.TextUtils;

import engine.android.util.secure.Blowfish;

public class EntityUtil {
    
    private static final boolean encrypt = true;
    
    private static final Blowfish encryptor;
    
    static
    {
        if (encrypt)
        {
            encryptor = new Blowfish();
            encryptor.setKey("I'm super man".getBytes());
        }
    }
    
    public static String toString(byte[] data) {
        if (data == null) return "";
        if (encryptor != null) data = encryptor.decrypt(data);
        return new String(data);
    }
    
    public static byte[] toByteArray(String entity) {
        if (TextUtils.isEmpty(entity)) return new byte[0];
        
        byte[] data = entity.getBytes();
        if (encryptor != null) data = encryptor.encrypt(data);
        return data;
    }
}