package demo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.activity.FragmentLayoutActivity;
import demo.android.R;

public class TitlesFragment extends ListFragment {
    
    private static final String STATE_POSITION = "position";
    private int mPosition = ListView.INVALID_POSITION;
    
    private Callbacks mCallbacks;
    public interface Callbacks {
        
        /**
         * Design for port/land orientation change on device.
         */
        public void onItemSelected(int position);
        
        public void onItemClick(int position);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TitlesFragment() {}
    
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        log("onInflate");
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        log("onAttach");
        super.onAttach(activity);
        
        if (activity instanceof Callbacks)
        {
            mCallbacks = (Callbacks) activity;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        log("onCreate");
        super.onCreate(savedInstanceState);
        
        setListAdapter(new ArrayAdapter<String>(getActivity(), 
                R.layout.fragment_layout_listitem, android.R.id.text1, FragmentLayoutActivity.TITLES));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        log("onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        log("onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }
    
    public void setListItemCheckEnabled(boolean enable) {
        // If true, the list view highlights the selected item
        getListView().setChoiceMode(enable ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        log("onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        log("onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_POSITION))
        {
            selectPostion(savedInstanceState.getInt(STATE_POSITION));
        }
    }
    
    private void selectPostion(int position) {
        if (position == ListView.INVALID_POSITION)
        {
            // Should not be arrived.
            getListView().setItemChecked(mPosition, false);
        }
        else
        {
            getListView().setItemChecked(position, true);
        }
        
        mPosition = position;
        if (mCallbacks != null)
        {
            mCallbacks.onItemSelected(position);
        }
    }

    @Override
    public void onResume() {
        log("onResume");
        super.onResume();
    }
    
    @Override
    public void onPause() {
        log("onPause");
        super.onPause();
    }
    
    @Override
    public void onDestroyView() {
        log("onDestroyView");
        super.onDestroyView();
    }
    
    @Override
    public void onDestroy() {
        log("onDestroy");
        super.onDestroy();
    }
    
    @Override
    public void onDetach() {
        log("onDetach");
        super.onDetach();

        // Reset the active callbacks interface.
        mCallbacks = null;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mPosition != ListView.INVALID_POSITION)
        {
            outState.putInt(STATE_POSITION, mPosition);
        }
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mPosition = position;
        if (mCallbacks != null)
        {
            mCallbacks.onItemClick(position);
        }
    }
    
    private void log(String content) {
        FragmentLayoutActivity.log("(" + getClass().getSimpleName() + ")" + content);
    }
}