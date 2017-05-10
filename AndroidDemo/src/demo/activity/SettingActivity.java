package demo.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.RingtonePreference;

import demo.android.R;
import demo.android.util.SystemUtil;
import demo.preference.BasePreferenceActivity;
import demo.preference.BasePreferenceFragment;
import demo.preference.annotation.InjectPreference;
import engine.android.util.AndroidUtil;

import java.util.List;

public class SettingActivity extends BasePreferenceActivity {
    
    public static interface PreferenceKey {
        
        public static final String KEY_IS_UPLOAD = "isUpload";
        public static final String KEY_UPLOAD_ADDRESS = "uploadAddress";
        public static final String KEY_RINGTONE = "ringtone";
        public static final String KEY_LIST = "list";
    }
    
    static final class PreferenceHelper extends demo.preference.PreferenceHelper implements PreferenceKey {
        
        @InjectPreference
        CheckBoxPreference isUpload;                    //是否上传
        
        @InjectPreference
        EditTextPreference uploadAddress;               //上传地址
        
        @InjectPreference
        RingtonePreference ringtone;                    //电话铃声
        
        @InjectPreference
        ListPreference list;

        public PreferenceHelper(Object obj) {
            super(obj);
        }
        
        @Override
        public void setupPreference(String key) {
            if (KEY_IS_UPLOAD.equals(key))
            {
                isUpload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        uploadAddress.setEnabled(isUpload.isChecked());
                        return true;
                    }
                });
            }
            else if (KEY_UPLOAD_ADDRESS.equals(key))
            {
                bindPreferenceSummaryToValue(uploadAddress);
            }
            else if (KEY_RINGTONE.equals(key))
            {
                bindPreferenceSummaryToValue(ringtone);
            }
            else if (KEY_LIST.equals(key))
            {
                bindPreferenceSummaryToValue(list);
            }
        }
        
        @Override
        protected void restore(SharedPreferences sp) {
            if (!sp.contains(KEY_UPLOAD_ADDRESS))
            {
                String defaultValue = "http://www.163.com";
                
                uploadAddress.setText(defaultValue);
                sp.edit().putString(KEY_UPLOAD_ADDRESS, defaultValue).commit();
                setupPreference(KEY_UPLOAD_ADDRESS);
            }
        }
    }

    @Override
    protected boolean isDualPane(Context context) {
        return SystemUtil.isTablet(this) && AndroidUtil.getVersion() >= Build.VERSION_CODES.HONEYCOMB;
    }

    @Override
    protected PreferenceHelper help() {
        return new PreferenceHelper(this);
    }

    @Override
    protected void initPreferenceScreen() {
        addPreferencesFromResource(R.xml.setting);
        
        PreferenceCategory category = new PreferenceCategory(this);
        category.setTitle("设置示例");
        getPreferenceScreen().addPreference(category);
        addPreferencesFromResource(R.xml.setting2);
    }

    @Override
    protected void setupPreference(demo.preference.PreferenceHelper helper) {
        helper.setupPreference(PreferenceKey.KEY_IS_UPLOAD);
        helper.setupPreference(PreferenceKey.KEY_UPLOAD_ADDRESS);
        helper.setupPreference(PreferenceKey.KEY_RINGTONE);
        helper.setupPreference(PreferenceKey.KEY_LIST);
    }

    @Override
    protected void buildHeadersForDualPane(List<Header> target) {
        loadHeadersFromResource(R.xml.setting_headers, target);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //清除设置
//        PreferenceHelper.getSharedPreferences(this).edit().clear().commit();
    }
    
    public static class 电话录音Preference extends BasePreferenceFragment implements PreferenceKey {

        @Override
        protected demo.preference.PreferenceHelper help() {
            return new PreferenceHelper(this);
        }

        @Override
        protected void initPreferenceScreen() {
            addPreferencesFromResource(R.xml.setting1);
        }

        @Override
        protected void setupPreference(demo.preference.PreferenceHelper helper) {
            helper.setupPreference(KEY_IS_UPLOAD);
            helper.setupPreference(KEY_UPLOAD_ADDRESS);
            helper.setupPreference(KEY_RINGTONE);
        }
    }
    
    public static class 设置示例Preference extends BasePreferenceFragment implements PreferenceKey {

        @Override
        protected demo.preference.PreferenceHelper help() {
            return new PreferenceHelper(this);
        }

        @Override
        protected void initPreferenceScreen() {
            addPreferencesFromResource(R.xml.setting2);
        }

        @Override
        protected void setupPreference(demo.preference.PreferenceHelper helper) {
            helper.setupPreference(KEY_LIST);
        }
    }
}