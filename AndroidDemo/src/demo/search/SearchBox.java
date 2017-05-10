package demo.search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

public class SearchBox extends Activity {
	private final int SEARCH_MENU = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置键盘模式（当按键事件没有被任何控件捕获时），中文输入法无效
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);//调用自定义搜索框进行处理
		/**
		setDefaultKeyMode(DEFAULT_KEYS_DISABLE);//不进行任何处理
		setDefaultKeyMode(DEFAULT_KEYS_DIALER);//传入拨号器进行处理
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);//作为菜单快捷键进行处理（必须先注册）
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_GLOBAL);//调用系统搜索框进行处理（也就是google的web搜索）
		*/
		
		handleSearchQuery(getIntent());
	}

	private void handleSearchQuery(Intent queryIntent) {
		if (Intent.ACTION_SEARCH.equals(queryIntent.getAction())) {
			final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
			onSearch(queryString);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleSearchQuery(intent);
	}

	private void onSearch(String queryString) {
		//自定义搜索数据
		Bundle bundle = getIntent().getBundleExtra(SearchManager.APP_DATA);
		if (bundle != null)
		{
		    Toast.makeText(this, bundle.getString("custom"), Toast.LENGTH_LONG).show();
		}
		else
		{
	        Toast.makeText(this, queryString, Toast.LENGTH_LONG).show();
		}
		
		SearchRecentSuggestions srs = new SearchRecentSuggestions(this, SearchProvider.AUTHORITY, SearchProvider.MODE);
		srs.saveRecentQuery(queryString, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		SearchManager sm = (SearchManager) getSystemService(SEARCH_SERVICE);
		SearchView sv = new SearchView(this);
		
		sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
		sv.setIconifiedByDefault(false);
		
		menu.add(0, SEARCH_MENU, 0, "搜索").setIcon(
				android.R.drawable.ic_menu_search).setActionView(sv);
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SEARCH_MENU:
			onSearchRequested();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	/**
	 * 自定义搜索
	 */
	
	@Override
	public boolean onSearchRequested() {
		Bundle appSearchData = new Bundle();
		appSearchData.putString("custom", "自定义搜索");
		startSearch(null, false, appSearchData, false);
        return true;
	}
	
	/**
	 * 关闭搜索框
	 */
	
	public void onSearchCanceled()
	{
		((SearchManager) getSystemService(SEARCH_SERVICE)).stopSearch();
	}
}