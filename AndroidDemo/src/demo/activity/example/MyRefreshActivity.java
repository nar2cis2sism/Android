package demo.activity.example;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ArrayAdapter;

import demo.android.R;
import demo.widget.RefreshListView;
import demo.widget.RefreshListView.OnRefreshListener;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * 自定义刷新界面（仿iphone）
 * @author yanhao
 * @version 1.0
 */

public class MyRefreshActivity extends ListActivity {
	
	String[] strs = {
			"Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam",
            "Abondance", "Ackawi", "Acorn", "Adelost", "Affidelice au Chablis",
            "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
            "Allgauer Emmentaler"};
	
	LinkedList<String> array;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_refresh_activity);
		
		//Set a listener to be invoked when the list should be refreshed.
		((RefreshListView) getListView()).setOnRefreshListener(new OnRefreshListener() {
            
            @Override
            public void doRefresh() {
                //Do work to refresh the list here.
                SystemClock.sleep(2000);
                array.addFirst("Added after refresh...");
                
            }
        });
		
		array = new LinkedList<String>();
		array.addAll(Arrays.asList(strs));
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
		setListAdapter(adapter);
	}
}