package engine.android.framework.net.http.util;

import engine.android.util.secure.Blowfish;

public class EntityUtil {
    
    private static final boolean encrypt = true;
    
    private static Blowfish encryptor;
    
    static
    {
        if (encrypt)
        {
            encryptor = new Blowfish();
            encryptor.setKey("I'm super man".getBytes());
        }
    }
    
    public static String toString(byte[] data) {
        if (encryptor != null)
            data = encryptor.decrypt(data);
        
        return new String(data);
    }
    
    public static byte[] toByteArray(String entity) {
        byte[] data = entity.getBytes();
        if (encryptor != null)
            data = encryptor.encrypt(data);
        
        return data;
    }
}