package demo.aidl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import demo.android.R;

import java.util.ArrayList;
import java.util.List;

public class AidlActivity extends Activity implements OnClickListener {
	
	private TextView text;
	
	private IAidl aidl;											//远程调用接口
	private ServiceConnection conn = new ServiceConnection(){	//远程调用连接

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//从远程service中获得AIDL实例化对象
			aidl = IAidl.Stub.asInterface(service);
			System.out.println("bind success");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			aidl = null;
			System.out.println("bind fail");
		}};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aidl_);
		
		text = (TextView) findViewById(R.id.TextView01);
		findViewById(R.id.Button01).setOnClickListener(this);
		findViewById(R.id.Button02).setOnClickListener(this);
		findViewById(R.id.Button03).setOnClickListener(this);
		findViewById(R.id.Button04).setOnClickListener(this);
		findViewById(R.id.Button05).setOnClickListener(this);
		findViewById(R.id.Button06).setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindService(conn);
			stopService(new Intent(this, AidlService.class));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.Button01:
				bindAidl();
				break;
			case R.id.Button02:
				text.setText("远程结果：" + aidl.getAccountBalance());
				break;
			case R.id.Button03:
				List<String> names = new ArrayList<String>();
				names.add("李彬彬");
				aidl.setOwnerNames(names);
				break;
			case R.id.Button04:
				String[] customerList = new String[1];
				aidl.getCustomerList("向华", customerList);
				text.setText("远程结果：" + customerList[0]);
				break;
			case R.id.Button05:
				aidl.showTest();
				break;
			case R.id.Button06:
				startAidl();
				break;

			default:
				break;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 绑定AIDL，启动远程连接，连接成功后才能调用接口
	 * 如服务没启动则自动创建
	 */
	
	private void bindAidl()
	{
		bindService(new Intent(IAidl.class.getName()), conn, BIND_AUTO_CREATE);
	}
	
	/**
	 * 启动AIDL，如在服务器端则需手动启动服务
	 */
	
	private void startAidl()
	{
		startService(new Intent(this, AidlService.class));
	}
}