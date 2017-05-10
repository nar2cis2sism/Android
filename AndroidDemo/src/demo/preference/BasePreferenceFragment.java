package demo.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

public abstract class BasePreferenceFragment extends PreferenceFragment {
    
    private PreferenceHelper helper;
    
    /**
     * User-define.
     * Only for dual-pane settings UI.
     */
    protected abstract PreferenceHelper help();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("PreferenceFragment:onCreate");
        super.onCreate(savedInstanceState);
        
        initPreferenceScreen();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        System.out.println("PreferenceFragment:onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        
        setupPreference(helper = help());
    }
    
    /**
     * Only for dual-pane settings UI.
     */
    protected abstract void initPreferenceScreen();

    /**
     * Only for dual-pane settings UI.
     */
    protected abstract void setupPreference(PreferenceHelper helper);
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("PreferenceFragment:onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        
        helper.restore(PreferenceHelper.getSharedPreferences(getActivity()));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        helper.save(PreferenceHelper.getSharedPreferences(getActivity()));
    }
}