package demo.webservice;

import android.util.Xml;

import engine.android.http.HttpConnector;
import engine.android.http.HttpRequest.StringEntity;
import engine.android.http.HttpResponse;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class WebService {
	
	/**
	 * 调用Web Service服务
	 * @param url 服务地址
	 * @param content 传送内容
	 * @return 返回内容
	 */
	
	public static String[] callService(String url, String content)
	{
		try {
			StringEntity se = new StringEntity(content);
			HttpConnector conn = new HttpConnector(url, se);
			conn.getRequest().setContentType("application/x-www-form-urlencoded");
			HttpResponse resp = conn.connect();
			if (resp != null)
			{
				//输出回包字符串
				System.out.println(resp.getContent());
				return parse(resp.getInputStream());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String[] parse(InputStream is) throws Exception
	{
		List<String> list = new LinkedList<String>();
		XmlPullParser xml = Xml.newPullParser();
		xml.setInput(is, null);
		if (xml.getEventType() == XmlPullParser.START_DOCUMENT)
		{
			while (xml.next() == XmlPullParser.START_TAG)
			{
				if ("string".equals(xml.getName()))
				{
					list.add(xml.nextText());
				}
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
}