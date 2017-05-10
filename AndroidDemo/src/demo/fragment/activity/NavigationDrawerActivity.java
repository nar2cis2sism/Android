package demo.fragment.activity;

import static demo.activity.FragmentLayoutActivity.DETAILS;
import static demo.activity.FragmentLayoutActivity.TITLES;
import static demo.activity.FragmentLayoutActivity.DetailsFragment.ARG_PISITION;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import demo.android.R;
import engine.android.util.AndroidUtil;

public class NavigationDrawerActivity extends Activity {
    
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    ActionBar mActionBar;

    private static final String SAVE_TITLE = "TITLE";
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_bar_drawer_activity);

        setupActionBar();
        setupDrawerLayout();
        setupDrawerList();
        
        mDrawerTitle = getTitle();
        if (savedInstanceState == null)
        {
            selectItem(0);
        }
        else
        {
            mTitle = savedInstanceState.getCharSequence(SAVE_TITLE);
        }
    }
    
    private void setupActionBar() {
        // enable ActionBar app icon to behave as action to toggle nav drawer
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                0,                     /* "open drawer" description for accessibility */
                0)                     /* "close drawer" description for accessibility */
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                NavigationDrawerActivity.this.onDrawerStateChanged(true);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            
            @Override
            public void onDrawerClosed(View drawerView) {
                NavigationDrawerActivity.this.onDrawerStateChanged(false);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    
    private void setupDrawerList() {
        mDrawerList = (ListView) findViewById(R.id.drawer);
        
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, 
                R.layout.action_bar_drawer_listitem, android.R.id.text1, TITLES));
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
    
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                selectItem(position);
            }
        });
    }
    
    @Override
    public void setTitle(CharSequence title) {
        mActionBar.setTitle(title);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        
        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        shareActionProvider.setShareIntent(getShareIntent());
        
        return super.onCreateOptionsMenu(menu);
    }
    
    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        return intent;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerHelper.isDrawerOpen();
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        
        // Handle action buttons
        switch (item.getItemId())
        {
            case R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, mActionBar.getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null)
                {
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(this, "Sorry, there\'s no web browser available", Toast.LENGTH_LONG).show();
                }
                
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
        
        onDrawerStateChanged(mDrawerHelper.isDrawerOpen());
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content"
     */
    
    public static class ContentFragment extends Fragment {
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if (container == null)
            {
                return null;
            }
            
            ScrollView sv = new ScrollView(getActivity());
            
            TextView tv = new TextView(getActivity());
            int padding = AndroidUtil.dp2px(getActivity(), 4);
            tv.setPadding(padding, padding, padding, padding);
            tv.setText(DETAILS[getShownIndex()]);
            
            sv.addView(tv);
            return sv;
        }
        
        public int getShownIndex()
        {
            return getArguments().getInt(ARG_PISITION);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putCharSequence(SAVE_TITLE, mTitle);
    }
    
    public void onDrawerStateChanged(boolean isDrawerOpen) {
        setTitle(isDrawerOpen ? mDrawerTitle : mTitle);
    }

    public void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PISITION, position);
        fragment.setArguments(args);
        
        getFragmentManager().beginTransaction()
        .replace(R.id.content, fragment)
        .commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mTitle = TITLES[position]);
        mDrawerHelper.closeDrawer();
    }

    private DrawerHelper mDrawerHelper = new DrawerHelper();
    
    public class DrawerHelper {
        
        public void openDrawer() {
            mDrawerLayout.openDrawer(mDrawerList);
        }
        
        public void closeDrawer() {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        
        public boolean isDrawerOpen() {
            return mDrawerLayout.isDrawerOpen(mDrawerList);
        }
        
        public void toggle() {
            if (isDrawerOpen())
            {
                closeDrawer();
            }
            else
            {
                openDrawer();
            }
        }
    }
}