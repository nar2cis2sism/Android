package demo.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import demo.preference.annotation.Injector;


public class PreferenceHelper {
    
    private final PreferenceFactory factory;
    
    public PreferenceHelper(Object obj) {
        if (obj instanceof PreferenceFragment)
        {
            factory = new PreferenceFactoryFragmentImpl((PreferenceFragment) obj);
        }
        else if (obj instanceof PreferenceActivity)
        {
            factory = new PreferenceFactoryActivityImpl((PreferenceActivity) obj);
        }
        else
        {
            throw new IllegalArgumentException();
        }
        
        Injector.injectPreference(this);
    }
    
    public void setupPreference(String key) {}

    protected void save(SharedPreferences sp) {}

    protected void restore(SharedPreferences sp) {}

    public final Preference findPreference(CharSequence key) {
        return factory.findPreference(key);
    }
    
    /**
     * Gets a SharedPreferences instance that preferences managed by this will
     * use.
     * 
     * @return A SharedPreferences instance pointing to the file that contains
     *         the values of preferences that are managed by this.
     */
    public static final SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static interface PreferenceFactory {
    
        /**
         * Finds a {@link Preference} based on its key.
         *
         * @param key The key of the preference to retrieve.
         * @return The {@link Preference} with the key, or null.
         * @see PreferenceGroup#findPreference(CharSequence)
         */
        public Preference findPreference(CharSequence key);
    
    }
    
    private static class PreferenceFactoryActivityImpl implements PreferenceFactory {
        
        private final PreferenceActivity activity;
        
        public PreferenceFactoryActivityImpl(PreferenceActivity activity) {
            this.activity = activity;
        }

        @Override
        public Preference findPreference(CharSequence key) {
            return activity.findPreference(key);
        }
    }
    
    private static class PreferenceFactoryFragmentImpl implements PreferenceFactory {
        
        private final PreferenceFragment fragment;
        
        public PreferenceFactoryFragmentImpl(PreferenceFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public Preference findPreference(CharSequence key) {
            return fragment.findPreference(key);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     * 
     * @see #sBindPreferenceSummaryToValueListener
     */
    public static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(
                new BindPreferenceSummaryToValueListener(preference.getOnPreferenceChangeListener()));

        // Trigger the listener immediately with the preference's
        // current value.
        preference.getOnPreferenceChangeListener().onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static class BindPreferenceSummaryToValueListener implements OnPreferenceChangeListener {
        
        private final OnPreferenceChangeListener listener;
        
        public BindPreferenceSummaryToValueListener(OnPreferenceChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("Silent");
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            
            if (listener != null)
            {
                return listener.onPreferenceChange(preference, newValue);
            }
            
            return true;
        }
    }
}