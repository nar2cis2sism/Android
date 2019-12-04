//package demo.activity;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnCancelListener;
//import android.content.Intent;
//import android.location.Address;
//import android.location.GpsStatus;
//import android.location.GpsStatus.Listener;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//import demo.android.R;
//import engine.android.util.manager.MyLocationManager;
//import engine.android.util.manager.MyWifiManager;
//
//public class LocationActivity extends Activity {
//	
//	private TextView tv;
//	
//	private Button bt;
//	
//	private MyLocationManager lm;
//	
//	private MyWifiManager wm;
//	
//	private LocationListener listener;
//	
//	private int index = 1;
//	
//	private String provider;
//	
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.location);
//        
//        tv = (TextView) findViewById(R.id.tv);
//        
//        lm = new MyLocationManager(this);
//        wm = new MyWifiManager(this);
//        provider = lm.getLocationProvider();
//        if (provider == null)
//        {
//        	//GPS未开启
//        	showDialog(0);
//        }
//        else
//        {
//            if (provider.equals(LocationManager.GPS_PROVIDER))
//            {
//                //GPS定位
//                lm.registerStateListener(new Listener(){
//
//                    @Override
//                    public void onGpsStatusChanged(int event) {
//                        if (event == GpsStatus.GPS_EVENT_FIRST_FIX)
//                        {
//                            GpsStatus g = lm.getGpsStatus();
//                            tv.append("卫星定位时间：" + g.getTimeToFirstFix());
//                            int[] num = lm.getSatelliteNumber(g);
//                            tv.append("，卫星数量：" + num[0] + ";" + num[1] + "\n");
//                        }
//                    }});
//                lm.registerLocationListener(provider, 60000, 1, listener = new LocationListener(){
//
//                    @Override
//                    public void onLocationChanged(Location location) {
//                        index++;
//                        show(location);
//                    }
//
//                    @Override
//                    public void onProviderDisabled(String provider) {
//                        // TODO Auto-generated method stub
//                        
//                    }
//
//                    @Override
//                    public void onProviderEnabled(String provider) {
//                        // TODO Auto-generated method stub
//                        
//                    }
//
//                    @Override
//                    public void onStatusChanged(String provider, int status,
//                            Bundle extras) {
//                        // TODO Auto-generated method stub
//                        
//                    }});
//            }
//
//            tv.append(provider + "\n");
//        	show(lm.getLocation(provider));
//        }
//
//        bt = (Button) findViewById(R.id.bt);
//        bt.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				index++;
//				show(lm.getLocation(provider));
//			}});
//    }
//    
//    @Override
//    protected void onDestroy() {
//    	lm.unregisterStateListener();
//    	lm.unregisterLocationListener(listener);
//    	super.onDestroy();
//    }
//    
//    @Override
//    protected Dialog onCreateDialog(int id) {
//    	Dialog dialog = new AlertDialog.Builder(this)
//        .setTitle("未开启GPS功能")
//        .setMessage("请选择下面的操作")
//        .setOnCancelListener(new OnCancelListener(){
//
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				finish();
//			}})
//        .setPositiveButton("开启GPS功能",
//        new DialogInterface.OnClickListener()
//        {
//            public void onClick(DialogInterface dialog, int whichButton)
//            {
//            	//打开系统设置中的我的位置界面,手动开启或关闭GPS
//                startActivityForResult(new Intent(
//                		android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
//            }
//        })
//        .setNegativeButton("使用基站定位",
//        new DialogInterface.OnClickListener()
//        {
//            public void onClick(DialogInterface dialog, int whichButton)
//            {
//            	//基站定位
//            	bt.setEnabled(false);
//            	show(lm.callGear(lm.getCellInfo(), wm.getWifiInfo()));
//            }
//        }).create();
//    	
//    	return dialog;
//    }
//    
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    	if (requestCode == 100)
//    	{
//    		provider = lm.getLocationProvider();
//            if (provider == null)
//            {
//        		tv.append("未开启GPS");
//            	bt.setEnabled(false);
//            }
//            else
//            {
//            	tv.append(provider + "\n");
//            	show(lm.getLocation(provider));
//            }
//    	}
//    }
//    
//    private void show(Location location)
//    {
//    	tv.append("第" + index + "次尝试\n");
//    	if (location == null)
//    	{
//    		tv.append("定位失败");
//    	}
//    	else
//    	{
//    		tv.append(location.toString() + "\n");
//    		Address address = lm.getAddress(location);
//    		if (address != null)
//    		{
//    			tv.append(address.toString());
//    		}
//    	}
//    	
//    	tv.append("\n\n");
//    }
//}