package demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import demo.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends Activity {

	/**需要声明权限<uses-permission android:name="android.permission.INTERNET" />**/
	private WebView webView;							//浏览器视图
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		
		webView = (WebView) findViewById(R.id.wv);
		WebSettings set = webView.getSettings();
		set.setJavaScriptEnabled(true);//设置支持javaScript
		set.setSaveFormData(false);//不保存表单数据
		set.setSavePassword(false);//不保存密码
		set.setSupportZoom(false);//不支持页面缩放
		
		//addJavascriptInterface方法中要绑定的Java对象及方法要运行在另外的线程中，
		//不能运行在构造他的线程中 
		webView.addJavascriptInterface(new JavaScript(this, webView), "js");
		//拦截webView中的链接点击事件
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println(url);
				return super.shouldOverrideUrlLoading(view, url);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				System.out.println(errorCode + ":" + description + "---" + failingUrl);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
		//本地Html文件存放在assets目录里
		webView.loadUrl("file:///android_asset/index.html");
	}
	
	@Override
	public void onBackPressed() {
		//如果不处理，会直接退出浏览器
		if (webView.canGoBack())
    	{
    		webView.goBack();
    	}
    	else
    	{
    		super.onBackPressed();
    	}
	}
	
	public static final class JavaScript {
	    
	    private final Context context;
	    
	    private final WebView webView;
	    
	    private final Handler handle = new Handler();             //事件处理器
	    
	    public JavaScript(Context context, WebView webView) {
	        this.context = context;
	        this.webView = webView;
        }
		
		/**
		 * 获取所有联系人
		 */
		
		public void getContacts()
		{
			handle.post(new Runnable(){

				@Override
				public void run() {
					//可以通过访问SQLLite数据库得到联系人 
					List<Contact> list = new ArrayList<Contact>();
					list.add(new Contact(1, "张三", "12345"));
					list.add(new Contact(2, "李四", "67890"));
					//调用javascript显示联系人
					webView.loadUrl("javascript:show(" + buildJson(list) + ")");
				}});
		}
		
		/**
		 * 生成Json格式的数据
		 * @param list 联系人列表
		 * @return
		 */
		
		private String buildJson(List<Contact> list)
		{
			try {
				JSONArray array = new JSONArray();
				for (Contact c : list)
				{
					JSONObject item = new JSONObject();
					item.put("id", c.getId());
					item.put("name", c.getName());
					item.put("phone", c.getPhone());
					array.put(item);
				}
				
				return array.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return "";
		}
		
		/**
		 * 拨打电话
		 * @param phone 电话号码
		 */
		
		public void call(final String phone)
		{
			handle.post(new Runnable(){

				@Override
				public void run() {
					context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
				}});
		}
	    
	    private static final class Contact {
	        
	        private long id;                        //标识
	        
	        private String name;                    //名字
	        
	        private String phone;                   //电话
	        
	        public Contact(long id, String name, String phone) {
	            this.id = id;
	            this.name = name;
	            this.phone = phone;
	        }

	        public long getId() {
	            return id;
	        }

	        public String getName() {
	            return name;
	        }

	        public String getPhone() {
	            return phone;
	        }
	    }
	}
}