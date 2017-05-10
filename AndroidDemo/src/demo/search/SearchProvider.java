package demo.search;

import android.content.SearchRecentSuggestionsProvider;

public class SearchProvider extends SearchRecentSuggestionsProvider {
	
	public static final String AUTHORITY = "searchprovider";
	
	public static final int MODE = DATABASE_MODE_QUERIES;
	
	public SearchProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}