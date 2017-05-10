package demo.fragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import demo.android.R;
import demo.fragment.AppListFragment.AppEntry;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListFragment extends ListFragment implements LoaderCallbacks<List<AppEntry>> {
	
	//This is the Adapter being used to display the list's data.
	AppListAdapter adapter;
	
	//If non-null, this is the current filter the user has provided.
	String searchFilter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//Give some text to display if there is no data.
		setEmptyText("No applications");
		
		//Create an empty adapter we will use to display the loaded data.
		setListAdapter(adapter = new AppListAdapter(getActivity()));
		
		//Start out with a progress indicator.
		setListShown(false);
		
		getLoaderManager().initLoader(0, null, this);
		
		setHasOptionsMenu(true);
	}
	
	/**
	 * need actionbarsherlock support
	 */
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		View searchView = SearchViewCompat.newSearchView(getActivity());
		if (searchView != null)
		{
			SearchViewCompat.setOnQueryTextListener(searchView, new OnQueryTextListenerCompat() {
				@Override
				public boolean onQueryTextChange(String newText) {
					searchFilter = !TextUtils.isEmpty(newText) ? newText : null;
					adapter.getFilter().filter(searchFilter);
//					getLoaderManager().restartLoader(0, null, AppListFragment.this);
					return true;
				}
				
				@Override
				public boolean onQueryTextSubmit(String query) {
				    return true;
				}
			});

            item.setActionView(searchView);
		}
	}
	
	public static class AppListAdapter extends ArrayAdapter<AppEntry> {

		public AppListAdapter(Context context) {
			super(context, R.layout.viewpager_listitem);
		}
		
		public void setData(List<AppEntry> data)
		{
			clear();
			if (data != null)
			{
			    addAll(data);
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			
			if (convertView == null)
			{
				view = LayoutInflater.from(getContext()).inflate(R.layout.viewpager_listitem, parent, false);
			}
			else
			{
				view = convertView;
			}
			
			AppEntry entry = getItem(position);
            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(entry.getIcon());
            ((TextView) view.findViewById(R.id.text)).setText(entry.getLabel());
            
            return view;
		}
	}
	
	/**
	 * This class holds the per-item data in our Loader.
	 */
	
	public static class AppEntry {

        private final AppListLoader loader;
        private final ApplicationInfo info;
        private final File apk;
        private String label;
        private Drawable icon;
        private boolean isMounted;
        
        public AppEntry(AppListLoader loader, ApplicationInfo info) {
			this.loader = loader;
			this.info = info;
			apk = new File(info.sourceDir);
		}
        
        public void loadLabel()
        {
        	if (label == null || !isMounted)
			{
				if (!apk.exists())
				{
					isMounted = false;
					label = info.packageName;
				}
				else
				{
					isMounted = true;
					CharSequence cs = info.loadLabel(loader.pm);
					label = cs != null ? cs.toString() : info.packageName;
				}
			}
        }

		public String getLabel() {
			return label;
		}

		public Drawable getIcon() {
			if (icon == null)
			{
				if (apk.exists())
				{
					icon = info.loadIcon(loader.pm);
					return icon;
				}
				else
				{
					isMounted = false;
				}
			}
			else if (!isMounted)
			{
				if (apk.exists())
				{
					isMounted = true;
					icon = info.loadIcon(loader.pm);
					return icon;
				}
			}
			else
			{
				return icon;
			}
			
			return loader.getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	/**
     * A custom Loader that loads all of the installed applications.
     */
	
	public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
		
		final PackageManager pm;
		final ConfigChange config = new ConfigChange();
		
        List<AppEntry> apps;
        AppObserver appObserver;

		public AppListLoader(Context context) {
			super(context);
			
			pm = context.getPackageManager();
		}

		@Override
		public List<AppEntry> loadInBackground() {
			List<ApplicationInfo> apps = pm.getInstalledApplications(
					PackageManager.GET_UNINSTALLED_PACKAGES | 
					PackageManager.GET_DISABLED_COMPONENTS);
			if (apps == null)
			{
				apps = new ArrayList<ApplicationInfo>();
			}
			
			List<AppEntry> list = new ArrayList<AppEntry>(apps.size());
			for (ApplicationInfo info : apps)
			{
				AppEntry entry = new AppEntry(this, info);
				entry.loadLabel();
				list.add(entry);
			}
			
			//Sort the list.
			Collections.sort(list, ALPHA_COMPARATOR);
			
			return list;
		}
		
		/**
		 * Perform alphabetical comparison of application entry objects.
		 */
		
		private static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
			
			private final Collator collator = Collator.getInstance();
			
			@Override
			public int compare(AppEntry object1, AppEntry object2) {
				return collator.compare(object1.getLabel(), object2.getLabel());
			}
		};
		
		/**
		 * Called when there is new data to deliver to the client.
		 */
		
		public void deliverResult(List<AppEntry> data) {
			if (isReset())
			{
				//An async query came in while the loader is stopped. We don't need the result.
				if (data != null)
				{
					releaseResources(data);
				}
				
				return;
			}
			
			List<AppEntry> oldApps = apps;
			apps = data;
			
			if (isStarted())
			{
				//If the Loader is currently started, we can immediately deliver its results.
				super.deliverResult(data);
			}
			
			//At this point we can release the resources associated with
            //'oldApps' if needed; now that the new result is delivered we
            //know that it is no longer in use.
			if (oldApps != null && oldApps != data)
			{
				releaseResources(oldApps);
			}
		};
		
		/**
		 * Handles a request to start the Loader.
		 */
		
		protected void onStartLoading() {
			if (apps != null)
			{
				//If we currently have a result available, deliver it immediately.
				deliverResult(apps);
			}
			
			//Start watching for changes in the app data.
			if (appObserver == null)
			{
				appObserver = new AppObserver(this);
			}
			
			//Has something interesting in the configuration changed since we last built the app list?
			boolean configChange = config.applyConfig(getContext().getResources());
			
			if (takeContentChanged() || apps == null || configChange)
			{
				//If the data has changed since the last time it was loaded
                //or is not currently available, start a load.
				forceLoad();
			}
		};
		
		/**
         * Handles a request to stop the Loader.
         */
		
		@Override
		protected void onStopLoading() {
			//Attempt to cancel the current load task if possible.
			cancelLoad();
		}
		
		/**
         * Handles a request to cancel a load.
         */
		
		@Override
		public void onCanceled(List<AppEntry> data) {
			releaseResources(data);
		}
		
		/**
         * Handles a request to completely reset the Loader.
         */
		
		@Override
		protected void onReset() {
			super.onReset();
			
			//Ensure the loader is stopped
			onStopLoading();
			
			if (apps != null)
			{
				releaseResources(apps);
				apps = null;
			}
			
			//Stop monitoring for changes.
			if (appObserver != null)
			{
				getContext().unregisterReceiver(appObserver);
				appObserver = null;
			}
		}
		
		/**
		 * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
		 */
		
		private void releaseResources(List<AppEntry> data)
		{
			//For something like a Cursor, we would close it here.
		}
	}

	@Override
	public Loader<List<AppEntry>> onCreateLoader(int arg0, Bundle arg1) {
		return new AppListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<AppEntry>> arg0, List<AppEntry> arg1) {
		//Set the new data in the adapter.
		adapter.setData(arg1);
		
		//The list should now be shown.
		if (isResumed())
		{
			setListShown(true);
		}
		else
		{
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> arg0) {
		//Clear the data in the adapter.
		adapter.setData(null);
	}
	
	/**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the app list.
     */
	
	public static class ConfigChange {
		
		private final Configuration config = new Configuration();
		private int density;
		
		public boolean applyConfig(Resources res)
		{
			int configChange = config.updateFrom(res.getConfiguration());
			boolean densityChange = density != res.getDisplayMetrics().densityDpi;
			if (densityChange 
					|| (configChange 
							& (ActivityInfo.CONFIG_LOCALE 
							|  ActivityInfoCompat.CONFIG_UI_MODE 
							|  ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0)
			{
				density = res.getDisplayMetrics().densityDpi;
				return true;
			}
			
			return false;
		}
	}
	
	/**
     * Helper class to look for interesting changes to the installed apps
     * so that the loader can be updated.
     */
	
	public static class AppObserver extends BroadcastReceiver {
		
		private final AppListLoader loader;
		
		public AppObserver(AppListLoader loader) {
			this.loader = loader;
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addDataScheme("package");
			loader.getContext().registerReceiver(this, filter);
			
			//Register for events related to sdcard installation.
			filter = new IntentFilter();
			filter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			filter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			loader.getContext().registerReceiver(this, filter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			//Tell the loader about the change.
			loader.onContentChanged();
		}
	}
}