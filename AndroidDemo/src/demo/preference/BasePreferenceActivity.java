package demo.preference;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

public abstract class BasePreferenceActivity extends PreferenceActivity {
    
    private PreferenceHelper helper;
    
    private boolean isDualPane;

    /**
     * Determines whether the simplified or a dual-pane settings UI should be shown.
     */
    protected abstract boolean isDualPane(Context context);
    
    /**
     * User-define.
     * Only for single-pane "simplified" settings UI.
     */
    protected abstract PreferenceHelper help();
	
	@Override
	protected void attachBaseContext(Context newBase) {
	    super.attachBaseContext(newBase);
	    isDualPane = isDualPane(newBase);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    System.out.println("PreferenceActivity:onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
        System.out.println("PreferenceActivity:onPostCreate");
	    super.onPostCreate(savedInstanceState);
        
        if (isDualPane)
        {
            return;
        }
        
        initPreferenceScreen();
        
        setupPreference(helper = help());
        
        helper.restore(PreferenceHelper.getSharedPreferences(this));
	}
	
	/**
	 * Only for single-pane "simplified" settings UI.
	 */
	protected abstract void initPreferenceScreen();

    /**
     * Only for single-pane "simplified" settings UI.
     */
	protected abstract void setupPreference(PreferenceHelper helper);
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (!isDualPane)
		{
		    helper.save(PreferenceHelper.getSharedPreferences(this));
		}
	}
	
	@Override
	public boolean onIsMultiPane() {
        System.out.println("onIsMultiPane:" + isDualPane);
	    return isDualPane;
	}
	
	@Override
	public void onBuildHeaders(List<Header> target) {
        System.out.println("onBuildHeaders");
        if (isDualPane)
        {
            buildHeadersForDualPane(target);
        }
	}
	
	/**
	 * @see {@link #loadHeadersFromResource(int, List)}
	 */
    
	protected abstract void buildHeadersForDualPane(List<Header> target);
}