package demo.webservice;

/**
 * 国内手机号码归属地查询WEB服务
 */

public class MobileService {
	
	/**
	以下是 HTTP POST 请求和响应示例。所显示的占位符需替换为实际值。

	POST /WebServices/MobileCodeWS.asmx/getMobileCodeInfo HTTP/1.1
	Host: webservice.webxml.com.cn
	Content-Type: application/x-www-form-urlencoded
	Content-Length: length

	mobileCode=string&userID=string
	HTTP/1.1 200 OK
	Content-Type: text/xml; charset=utf-8
	Content-Length: length

	<?xml version="1.0" encoding="utf-8"?>
	<string xmlns="http://WebXml.com.cn/">string</string>
	**/
	
	/**
	 * 获得国内手机号码归属地省份、地区和手机卡类型信息
	 * @param mobilePhone 手机号码，最少前7位数字
	 * @return 手机号码：省份 城市 手机卡类型
	 */
	
	public static String getMobileCodeInfo(String mobilePhone)
	{
		String url = "http://webservice.webxml.com.cn/WebServices/MobileCodeWS.asmx/getMobileCodeInfo";
		String content = String.format("mobileCode=%s&userID=", mobilePhone);
//		String[] strs = WebService.callService(url, content);
//		if (strs != null && strs.length == 1)
//		{
//			return new MobileInfo(strs[0]).toString();
//		}
		
		return null;
	}
	
	static class MobileInfo {
		
		private String mobilePhone;					//手机号码
		
		private String province;					//省份
		
		private String city;						//城市
		
		private String cardType;					//手机卡类型
		
		public MobileInfo(String s) {
			int index = s.indexOf("：");
			if (index != -1)
			{
				mobilePhone = s.substring(0, index);
			}
			
			String[] strs = s.substring(index + 1).split(" ");
			if (strs.length == 3)
			{
				province = strs[0];
				city = strs[1];
				cardType = strs[2];
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("手机号码：" + mobilePhone + "\n");
			sb.append("省份：" + province + "\n");
			sb.append("城市：" + city + "\n");
			sb.append("手机卡类型：" + cardType + "\n");
			return sb.toString();
		}

		public String getMobilePhone() {
			return mobilePhone;
		}

		public void setMobilePhone(String mobilePhone) {
			this.mobilePhone = mobilePhone;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getCardType() {
			return cardType;
		}

		public void setCardType(String cardType) {
			this.cardType = cardType;
		}
	}
}