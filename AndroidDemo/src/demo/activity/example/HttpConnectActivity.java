package demo.activity.example;

import android.os.Bundle;
import android.widget.TextView;

import engine.android.core.Forelet;
import engine.android.http.HttpConnector;
import engine.android.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.util.EntityUtils;

/**
 * Http联网界面
 */

public class HttpConnectActivity extends Forelet {
    
    TextView tv;
    
    String text;
    
    private static final int TASK_1 = 1;
    private static final int TASK_2 = 2;
    
    HttpConnector conn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		tv = new TextView(this);
		setContentView(tv);
		
//		if (savedInstanceState == null)
//		{
//		    showProgress(new ProgressSetting().setMessage("重定向..."));
//		    executeTask(new Task(taskExecutor, taskCallback));
//		    execute(TASK_1, conn = new HttpConnector("http://r.dangdang.com/url_for_hao123.php")
//            .setProxy(this)
//            .setTimeout(5000));
//		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    showText();
	}
	
//	@Override
//	protected void handleTaskCallback(int taskId, Object result) {
//	    switch (taskId) {
//            case TASK_1:
//                text = "连接地址：" + Util.getString(conn.getTargetHost(), "Null")
//                + "\n" + getString((HttpEntity) result);
//                
//                showText();
//                
//                conn = new HttpConnector("https://martinreichart.com/_tmpdata/login_valid.json")
//                .setProxy(this)
//                .setTimeout(5000);
//                
//                SchemeRegistry sr = conn.getHttpClient().getConnectionManager().getSchemeRegistry();
//                SSLSocketFactory ssl = (SSLSocketFactory) sr.get("https").getSocketFactory();
//                ssl.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//                
//                showProgress(settingProgress().setMessage("SSL验证..."));
//                execute(TASK_2, conn, 3000);
//            
//                break;
//            case TASK_2:
//                text = "连接地址：" + Util.getString(conn.getTargetHost(), "Null")
//                + "\n" + getString((HttpEntity) result);
//                
//                showText();
//                hideProgress();
//            
//                break;
//        }
//	}
	
	private String getString(HttpEntity entity) {
	    String s = "Null";
	    
	    if (entity != null)
	    {
	        try {
                s = EntityUtils.toString(entity).substring(0, 5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
	    }
	    
	    return s;
	}
	
	private void showText() {
	    tv.setText(text);
	}
}