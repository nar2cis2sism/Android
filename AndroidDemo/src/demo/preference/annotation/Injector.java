package demo.preference.annotation;

import android.text.TextUtils;

import demo.preference.PreferenceHelper;

import java.lang.reflect.Field;

public class Injector {
    
    public static void injectPreference(PreferenceHelper helper)
    {
        try {
            for (Class<?> c = helper.getClass(); c != PreferenceHelper.class; c = c.getSuperclass())
            {
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields)
                {
                    InjectPreference injectPreference = field.getAnnotation(InjectPreference.class);
                    if (injectPreference != null)
                    {
                        String key = injectPreference.key();
                        if (TextUtils.isEmpty(key))
                        {
                            key = field.getName();
                        }
                        
                        Object preference = helper.findPreference(key);
                        if (preference != null)
                        {
                            field.setAccessible(true);
                            field.set(helper, preference);
                        }
                    }
                }
            }
        } catch (Exception e) {
//            throw new InjectException(e);
        }
    }
}