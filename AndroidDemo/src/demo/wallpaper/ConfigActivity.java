package demo.wallpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import demo.android.R;

public class ConfigActivity extends PreferenceActivity {
    
    CheckBoxPreference dynamic;
    
    SharedPreferences sp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_settings);
        
        dynamic = (CheckBoxPreference) findPreference("livewallpaper_dynamic");
        
        sp = getSharedPreferences("wallpaper", MODE_PRIVATE);
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        sp.edit().putBoolean("dynamic", dynamic.isChecked()).commit();
        return true;
    }
}