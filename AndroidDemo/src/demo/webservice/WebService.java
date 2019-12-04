//package demo.webservice;
//
//import android.util.Xml;
//
//import engine.android.http.HttpConnector;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.entity.StringEntity;
//import org.xmlpull.v1.XmlPullParser;
//
//import java.io.InputStream;
//import java.util.LinkedList;
//import java.util.List;
//
//public class WebService {
//	
//	/**
//	 * 调用Web Service服务
//	 * @param url 服务地址
//	 * @param content 传送内容
//	 * @return 返回内容
//	 */
//	
//	public static String[] callService(String url, String content)
//	{
//		try {
//			StringEntity se = new StringEntity(content);
//			se.setContentType("application/x-www-form-urlencoded");
//			HttpConnector conn = new HttpConnector(url, se);
//			HttpEntity entity = conn.connect();
//			if (entity != null)
//			{
//				//输出回包字符串
////				System.out.println(EntityUtils.toString(entity));
//				return parse(entity.getContent());
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
//	
//	public static String[] parse(InputStream is) throws Exception
//	{
//		List<String> list = new LinkedList<String>();
//		XmlPullParser xml = Xml.newPullParser();
//		xml.setInput(is, null);
//		if (xml.getEventType() == XmlPullParser.START_DOCUMENT)
//		{
//			while (xml.next() == XmlPullParser.START_TAG)
//			{
//				if ("string".equals(xml.getName()))
//				{
//					list.add(xml.nextText());
//				}
//			}
//		}
//		
//		return list.toArray(new String[list.size()]);
//	}
//}