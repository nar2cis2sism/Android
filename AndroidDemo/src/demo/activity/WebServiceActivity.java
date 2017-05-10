package demo.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.webservice.MobileService;

import engine.android.util.Util;

public class WebServiceActivity extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] strs = {"国内手机号码归属地查询"};
		setListAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, strs));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String title = l.getItemAtPosition(position).toString();
		String message = "查询出错";
		switch (position) {
		case 0:
			message = Util.getString(MobileService.getMobileCodeInfo("15571758107"), message);
			break;

		default:
			break;
		}
		
		new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("确定", null).create().show();
	}
}