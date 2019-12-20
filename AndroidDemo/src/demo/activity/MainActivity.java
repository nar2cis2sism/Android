package demo.activity;

import engine.android.core.ApplicationManager;
import engine.android.util.AndroidUtil;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.activity.effect.EffectActivity;
import demo.activity.example.ExampleActivity;
import demo.activity.test.TestActivity;
import demo.aidl.AidlActivity;
import demo.android.R;
import demo.lockscreen.LockService;

import java.util.LinkedList;
import java.util.List;

/**
 * 搜索资料
 * Daimon:**
 */

public class MainActivity extends Activity {
    
    private boolean isUserInteraction = true;
    private boolean isUserBackKey;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AndroidUtil.getVersion() >= 7)
        {
            //如果是通过快捷方式进入的，此方法可获取shortcut的坐标
            System.out.println("shortcut position:" + getIntent().getSourceBounds());
        }
        
        //For security to hide the screenshot of recent app list.
        if (AndroidUtil.getVersion() >= 11)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        
        setContentView(R.layout.main);
        
//        if (!hasShortcut())
//        {
//            addShortcut();            
//        }
        
        ListView lv = (ListView) findViewById(R.id.list);
        List<String> list = new LinkedList<String>();
        list.add("Intent");
        list.add("定位");
        list.add("地图");
        list.add("控件");
        list.add("通知");
        list.add("浏览器");
        list.add("手势");
        list.add("设置");
        list.add("人脸识别");
        list.add("蓝牙操作（需要2部手机）");
        list.add("手机铃声");
        list.add("Fragment示例");
        list.add("电池信息");
        list.add("二维码示例");
        list.add("吹一吹，摇一摇");
        list.add("图形");
        list.add("视频");
        list.add("WebService");
        list.add("手电筒");
        list.add("来电拦截");
        list.add("AIDL（接口描述语言）");
        list.add("程序卸载问卷调查");
        list.add("运行第三方程序");
        list.add("增量升级（省流量更新）");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					// Intent
					startActivity(new Intent(MainActivity.this, IntentActivity.class));
					overridePendingTransition(R.anim.zoomin_in, R.anim.zoomin_out);
					break;
                case 1:
					// 定位
//					startActivity(new Intent(MainActivity.this, LocationActivity.class));
					break;
                case 2:
					// 地图
//					startActivity(new Intent(MainActivity.this, MyMapActivity.class));
					break;
                case 3:
					// 控件
					startActivity(new Intent(MainActivity.this, ViewActivity.class));
					break;
                case 4:
					// 通知
					startActivity(new Intent(MainActivity.this, NotificationActivity.class));
					break;
                case 5:
					// 浏览器
					startActivity(new Intent(MainActivity.this, WebViewActivity.class));
					break;
                case 6:
					// 手势
					startActivity(new Intent(MainActivity.this, GesturesActivity.class));
					break;
                case 7:
					// 设置
					startActivity(new Intent(MainActivity.this, SettingActivity.class));
					break;
                case 8:
					// 人脸识别
					startActivity(new Intent(MainActivity.this, FaceActivity.class));
					break;
                case 9:
					// 蓝牙操作
					startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
					break;
                case 10:
					// 手机铃声
					startActivity(new Intent(MainActivity.this, RingtoneActivity.class));
					break;
                case 11:
					// Fragment示例
					startActivity(new Intent(MainActivity.this, FragmentLayoutActivity.class));
					break;
                case 12:
                    // 电池信息
                    startActivity(new Intent(MainActivity.this, BatteryActivity.class));
                    break;
                case 13:
                    // 二维码示例
                    startActivity(new Intent(MainActivity.this, QRCodeActivity.class));
                    break;
                case 14:
                    // 吹一吹，摇一摇
                    startActivity(new Intent(MainActivity.this, SensorActivity.class));
                    break;
                case 15:
                    // 图形
                    startActivity(new Intent(MainActivity.this, ShapeDrawble1.class));
                    break;
                case 16:
                    // 视频
                    startActivity(new Intent(MainActivity.this, MediaActivity.class));
                    break;
                case 17:
                    // WebService
                    startActivity(new Intent(MainActivity.this, WebServiceActivity.class));
                    break;
                case 18:
                    // 手电筒
                    startActivity(new Intent(MainActivity.this, Flashlight.class));
                    break;
                case 19:
                    // 来电拦截
                    startActivity(new Intent(MainActivity.this, CallForwarding.class));
                    break;
                case 20:
                    // AIDL（接口描述语言）
                    startActivity(new Intent(MainActivity.this, AidlActivity.class));
                    break;
                case 21:
                    // 程序卸载问卷调查
                    startActivity(new Intent(MainActivity.this, UninstallActivity.class));
                    break;
                case 22:
                    // 运行第三方程序
                    startActivity(new Intent(MainActivity.this, ThirdPartyActivity.class));
                    break;
                case 23:
                    // 增量升级（省流量更新）
                    startActivity(new Intent(MainActivity.this, AppUpgradeActivity.class));
                    break;

				default:
					break;
				}
			}});
    }
    
    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas)
    {
        //For security to hide the screenshot of recent app list. (Compatible prior to API3.0)
        return true;
    }
    
    @Override
    protected void onDestroy() {
        deleteShortcut();
    	stopService(new Intent(this, LockService.class));
    	super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        if (isUserInteraction)
        {
            isUserInteraction = false;
            ApplicationManager.showMessage("再按一次退出程序");
        }
        else
        {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        isUserBackKey = event.getKeyCode() == KeyEvent.KEYCODE_BACK;

        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public void onUserInteraction() {
        if (isUserBackKey)
        {
            isUserBackKey = false;
        }
        else
        {
            isUserInteraction = true;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, 0, 0, "Effect");
    	menu.add(0, 1, 0, "Example");
    	menu.add(0, 2, 0, "Test");
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case 0:
			//Demo
	    	startActivity(new Intent(MainActivity.this, EffectActivity.class));
			break;
		case 1:
			//Example
	    	startActivity(new Intent(MainActivity.this, ExampleActivity.class));
			break;
        case 2:
            //Test
            startActivity(new Intent(MainActivity.this, TestActivity.class));
            break;

		default:
			break;
		}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    /**
     * 为程序创建桌面快捷方式
     * 需要声明权限<uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
     */
    
    public void addShortcut()
    {
    	Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
    	//快捷方式的名称
    	shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
    	//快捷方式的图标
    	shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
    	//不允许重复创建
    	shortcut.putExtra("duplicate", false);
    	//指定当前的Activity为快捷方式启动的对象
    	//这里必须为Intent设置一个action，可以任意(但安装和卸载时该参数必须一致)
    	shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(this, this.getClass()).setAction(Intent.ACTION_MAIN));
    	
    	sendBroadcast(shortcut);
    }
    
    /**
     * 删除桌面快捷方式
     * 需要声明权限<uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT" />
     */
    
    public void deleteShortcut()
    {
    	Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
    	//快捷方式的名称
    	shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
    	//指定当前的Activity为快捷方式启动的对象
    	//这里必须为Intent设置一个action，可以任意(但安装和卸载时该参数必须一致)
    	shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(this, this.getClass()).setAction(Intent.ACTION_MAIN));
    	
    	sendBroadcast(shortcut);
    }
    
    /**
     * 判断是否已有桌面快捷方式
     * 需要声明权限<uses-permission android:name="com.android.launcher.permission.READ_SETTINGS" />
     */
    
    public boolean hasShortcut()
    {
        final ContentResolver cr = getContentResolver();
        final String AUTHORITY = AndroidUtil.getAuthorityFromPermission(this, "com.android.launcher.permission.READ_SETTINGS");
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI, null, "title=?", new String[]{getString(R.string.app_name)}, null);
        try {
            if (c != null && c.getCount() > 0)
            {
                return true;
            }
            
            return false;
        } finally {
            if (c != null)
            {
                c.close();
            }
        }
    }
}