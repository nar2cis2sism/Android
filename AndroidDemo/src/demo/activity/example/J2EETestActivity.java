package demo.activity.example;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import engine.android.core.ApplicationManager;
import engine.android.core.Forelet;
import engine.android.http.HttpConnector;
import engine.android.http.HttpConnector.HttpConnectionListener;
import engine.android.util.io.IOUtil;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class J2EETestActivity extends Forelet implements OnItemClickListener, HttpConnectionListener {
	
	private static final String J2EE_URL = "http://yanhao.meibu.net:8080/yanhao/android.do";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ListView lv = new ListView(this);
        List<String> list = new LinkedList<String>();
        list.add("网络测试");
        list.add("下载文件");
        list.add("上传文件");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(this);
        
        setContentView(lv);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:
			//网络测试
			try {
					StringEntity entity = new StringEntity("检查网络", HTTP.UTF_8);
					
					showProgress(new ProgressSetting().setMessage("正在检查网络..."));
					HttpConnector conn = new HttpConnector(J2EE_URL, entity)
                    .setRemark("检查网络")
                    .setName("check")
                    .setListener(this);
					executeTask(new HttpTask(conn));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
			break;
		case 1:
			//下载文件
            showProgress(new ProgressSetting().setMessage("正在下载文件..."));
            HttpConnector conn = new HttpConnector(J2EE_URL)
            .setRemark("下载文件")
            .setName("download")
            .setListener(this);
            executeTask(new HttpTask(conn));
			break;
		case 2:
			//上传文件
			File file = getFileStreamPath("360.apk");
			if (file.exists())
			{
				try {
					MultipartEntity entity = new MultipartEntity();
					entity.addPart("name", new StringBody("360卫士", Charset.forName(HTTP.UTF_8)));
					entity.addPart("file", new FileBody(file));

		            showProgress(new ProgressSetting().setMessage("正在上传文件..."));
		            conn = new HttpConnector(J2EE_URL, entity)
		            .setRemark("上传文件")
		            .setName("upload")
		            .setListener(this);
		            executeTask(new HttpTask(conn));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			else
			{
				openMessageDialog("提示", "请先下载文件", "确定");
			}
				
			break;

		default:
			break;
		}
	}

	@Override
	public void connectAfter(HttpConnector conn, HttpResponse response) {
		String name = conn.getName();
		if ("check".equals(name))
		{
			try {
				show(EntityUtils.toString(response.getEntity(), HTTP.UTF_8));
			} catch (Exception e) {
				show(e.toString());
			}
		}
		else if ("download".equals(name))
		{
			try {
				InputStream is = response.getEntity().getContent();
				FileOutputStream fos = openFileOutput("360.apk", MODE_PRIVATE);
				IOUtil.writeStream(is, fos);
				fos.close();
				show("下载成功");
			} catch (Exception e) {
				show(e.toString());
			}
		}
		else if ("upload".equals(name))
		{
			Header header = response.getFirstHeader("type");
			String type = header == null ? "Null" : header.getValue();
			show(type);
		}
		
		hideProgress();
	}

	@Override
	public void connectBefore(HttpConnector conn, HttpRequest request) {
		conn.setProxy(this);
		request.setHeader("type", conn.getName());
	}

	@Override
	public void connectError(HttpConnector conn, Exception e) {
		show(e.toString());
		hideProgress();
	}
	
	private void show(final String s)
	{
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
			    ApplicationManager.showMessage(s);
			}});
	}
	
	private static class HttpTask extends Task {

        public HttpTask(final HttpConnector conn) {
            super(new TaskExecutor() {
                
                @Override
                public Object doExecute() {
                    return conn.connect();
                }
                
                @Override
                public void cancel() {
                    conn.cancel();
                }
            }, null);
        }
	}
}