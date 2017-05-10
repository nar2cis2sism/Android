package demo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import engine.android.util.io.IOUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BluetoothActivity extends ListActivity implements OnItemClickListener {
	
	static final java.util.UUID UUID = java.util.UUID.randomUUID();
	
	BluetoothAdapter b_adapter;
	
	List<BluetoothDevice> b_list;
	
	BluetoothServer b_server;
	
	BluetoothClient b_client;
	
	BroadcastReceiver b_receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				b_list.add(device);
				refresh();
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()))
			{
				System.out.println("搜索完毕");
			}
		}};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		registerReceiver(b_receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		registerReceiver(b_receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		
		if ((b_adapter = BluetoothAdapter.getDefaultAdapter()) == null)
		{
			//设备不支持蓝牙
			Dialog dialog = new AlertDialog.Builder(this)
	        .setTitle("提示")
	        .setMessage("设备不支持蓝牙")
	        .setPositiveButton("确定",
	        new DialogInterface.OnClickListener()
	        {
	            public void onClick(DialogInterface dialog, int whichButton)
	            {
	                finish();
	            }
	        }).create();
			
			dialog.show();
		}
		else if (!b_adapter.isEnabled())
		{
			//未开启蓝牙
			Dialog dialog = new AlertDialog.Builder(this)
	        .setTitle("蓝牙未开启")
	        .setMessage("是否现在开启")
	        .setPositiveButton("是",
	        new DialogInterface.OnClickListener()
	        {
	            public void onClick(DialogInterface dialog, int whichButton)
	            {
	            	//询问用户
	                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 100);
	            }
	        })
	        .setNegativeButton("否",
	        new DialogInterface.OnClickListener()
	        {
	            public void onClick(DialogInterface dialog, int whichButton)
	            {
	                finish();
	            }
	        }).create();
			
			dialog.show();
		}
		else
		{
			show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100)
		{
			if (resultCode == RESULT_CANCELED)
			{
				//强制开启
				b_adapter.enable();
            	//使蓝牙设备能被发现
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				//缺省120秒，(0-3600)
				intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(intent, 200);
			}
			else
			{
				show();
			}
		}
		else if (requestCode == 200)
		{
			if (resultCode == RESULT_CANCELED)
			{
				finish();
			}
			else
			{
				show();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(b_receiver);
		if (b_client != null)
		{
			b_client.close();
		}
		
		if (b_server != null)
		{
			b_server.close();
		}
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("搜索");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//扫描设备
		b_adapter.startDiscovery();
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void show()
	{
		System.out.println("我的蓝牙地址：" + b_adapter.getAddress());
		b_list = new ArrayList<BluetoothDevice>();
		Set<BluetoothDevice> set = b_adapter.getBondedDevices();
		if (!set.isEmpty())
		{
			b_list.addAll(set);
			refresh();
		}
		
		getListView().setOnItemClickListener(this);
	}
	
	private void refresh()
	{
		String[] from = new String[]{"name", "address"};
		int[] to = new int[]{android.R.id.text1, android.R.id.text2};
		SimpleAdapter adapter = new SimpleAdapter(this, getData(), android.R.layout.simple_list_item_2, from, to);
		setListAdapter(adapter);
	}
	
	private List<Map<String, String>> getData()
	{
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		for (BluetoothDevice device : b_list)
		{
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", device.getName());
			map.put("address", device.getAddress());
			list.add(map);
		}
		
		return list;
	}
	
	class BluetoothServer implements Runnable {
		
		BluetoothServerSocket server;
		
		public BluetoothServer() {
			try {
				server = b_adapter.listenUsingRfcommWithServiceRecord(getPackageName(), UUID);
				new Thread(this).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				BluetoothSocket socket = server.accept();
				if (socket != null)
				{
					socket.getOutputStream().write("服务器回应".getBytes());
					System.out.println("服务器收到:" + new String(IOUtil.readStream(socket.getInputStream())));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void close()
		{
			if (server != null)
			{
				try {
					server.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class BluetoothClient implements Runnable {
		
		BluetoothSocket socket;
		
		public BluetoothClient(BluetoothDevice device) {
			try {
				socket = device.createRfcommSocketToServiceRecord(UUID);
				new Thread(this).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				socket.connect();
				System.out.println("客户端收到:" + new String(IOUtil.readStream(socket.getInputStream())));
				socket.getOutputStream().write("客户端回应".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void close()
		{
			if (socket != null)
			{
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		b_adapter.cancelDiscovery();
		BluetoothDevice device = b_list.get(position);
		if (device != null)
		{
			//连接设备
			b_server = new BluetoothServer();
			b_client = new BluetoothClient(device);
		}
	}
}