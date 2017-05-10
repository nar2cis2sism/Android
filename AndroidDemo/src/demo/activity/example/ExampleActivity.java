package demo.activity.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.android.R;
import demo.library.LibraryActivity;

import java.util.LinkedList;
import java.util.List;

public class ExampleActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ListView lv = (ListView) findViewById(R.id.list);
        List<String> list = new LinkedList<String>();
        list.add("登录界面");
        list.add("会话界面");
        list.add("自定义刷新界面");
        list.add("自定义标签界面");
        list.add("闪烁心形绘制");
        list.add("Http联网界面");
        list.add("OAuth认证界面");
        list.add("腾讯微博界面");
        list.add("J2EE测试界面");
        list.add("多线程断点续传界面");
        list.add("自定义二级列表界面");
        list.add("联系人界面");
        list.add("邮件列表");
        list.add("城市快速查找");
        list.add("文件浏览器");
        list.add("线性图");
        list.add("涂鸦");
        list.add("计算器");
        list.add("柱状图");
        list.add("水平ListView");
        list.add("日期时间选择");
        list.add("注册界面");
        list.add("开源库");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					//登录界面
					startActivity(new Intent(ExampleActivity.this, LoginActivity.class));
					break;
				case 1:
					//会话界面
					startActivity(new Intent(ExampleActivity.this, SessionActivity.class));
					break;
				case 2:
					//自定义刷新界面
					startActivity(new Intent(ExampleActivity.this, MyRefreshActivity.class));
					break;
				case 3:
					//自定义标签界面
					startActivity(new Intent(ExampleActivity.this, MyTabActivity.class));
					break;
				case 4:
					//闪烁心形绘制
					startActivity(new Intent(ExampleActivity.this, HeartActivity.class));
					break;
				case 5:
					//Http联网界面
					startActivity(new Intent(ExampleActivity.this, HttpConnectActivity.class));
					break;
				case 6:
					//OAuth认证界面
					startActivity(new Intent(ExampleActivity.this, OAuthActivity.class));
					break;
				case 7:
					//腾讯微博界面
					startActivity(new Intent(ExampleActivity.this, WeiboActivity.class));
					break;
				case 8:
					//J2EE测试界面
					startActivity(new Intent(ExampleActivity.this, J2EETestActivity.class));
					break;
				case 9:
					//多线程断点续传界面
					startActivity(new Intent(ExampleActivity.this, DownloadActivity.class));
					break;
				case 10:
					//自定义二级列表界面
					startActivity(new Intent(ExampleActivity.this, MyExpandableListActivity.class));
					break;
				case 11:
					//联系人界面
					startActivity(new Intent(ExampleActivity.this, ContactsActivity.class));
					break;
				case 12:
					//邮件列表
					startActivity(new Intent(ExampleActivity.this, PinnedHeaderActivity.class));
					break;
                case 13:
                    //城市快速查找
                    startActivity(new Intent(ExampleActivity.this, FirstLetterActivity.class));
                    break;
                case 14:
                    //文件浏览器
                    startActivity(new Intent(ExampleActivity.this, FileBrowser.class));
                    break;
                case 15:
                    //线性图
                    startActivity(new Intent(ExampleActivity.this, XYChartActivity.class));
                    break;
                case 16:
                    //涂鸦
                    startActivity(new Intent(ExampleActivity.this, ScrawlActivity.class));
                    break;
                case 17:
                    //计算器
                    startActivity(new Intent(ExampleActivity.this, CalculatorActivity.class));
                    break;
                case 18:
                    //柱状图
                    startActivity(new Intent(ExampleActivity.this, HistogramChartActivity.class));
                    break;
                case 19:
                    //水平ListView
                    startActivity(new Intent(ExampleActivity.this, HorizontalActivity.class));
                    break;
                case 20:
                    //日期时间选择
                    startActivity(new Intent(ExampleActivity.this, DateTimePickerActivity.class));
                    break;
                case 21:
                    //注册界面
                    startActivity(new Intent(ExampleActivity.this, RegisterActivity.class));
                    break;
                case 22:
                    //开源库
                    startActivity(new Intent(ExampleActivity.this, LibraryActivity.class));
                    break;

				default:
					break;
				}
			}});
    }
}