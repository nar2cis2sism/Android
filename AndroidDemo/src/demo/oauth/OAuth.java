package demo.oauth;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @deprecated
 */

public class OAuth {
	
    public static final String VERSION_1_0 = "1.0";
    public static final String ENCODING = "UTF-8";
    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_SIGNATURE = "oauth_signature";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_NONCE = "oauth_nonce";
    public static final String OAUTH_VERSION = "oauth_version";
    public static final String OAUTH_CALLBACK = "oauth_callback";
    public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
    public static final String OAUTH_VERIFIER = "oauth_verifier";
    public static final String HMACSHA1SignatureType = "HmacSHA1";
    public static final String HMACSHA1SignatureMethod = "HMAC-SHA1";
	
	private static final Comparator<BasicNameValuePair> comparator = new Comparator<BasicNameValuePair>(){

		@Override
		public int compare(BasicNameValuePair object1,
				BasicNameValuePair object2) {
			int i = object1.getName().compareTo(object2.getName());
			if (i == 0)
			{
				i = object1.getValue().compareTo(object2.getValue());
			}
			
			return i;
		}};
	
	public static String sign(String url, String method, List<BasicNameValuePair> params, String consumerSecret, String tokenSecret) throws Exception
	{
        String key = encode(consumerSecret) + "&" + encode(tokenSecret);
        SecretKeySpec sks = new SecretKeySpec(key.getBytes(ENCODING), HMACSHA1SignatureType);
        Mac mac = Mac.getInstance(HMACSHA1SignatureType);
        mac.init(sks);
        return new String(Base64.encode(mac.doFinal(generateSignatureBase(url, method, params).getBytes(ENCODING)), Base64.DEFAULT), ENCODING);
	}
	
	private static String generateSignatureBase(String url, String method, List<BasicNameValuePair> params)
	{
		Collections.sort(params, comparator);
		
		return new StringBuilder()
		.append(method.toUpperCase())
		.append("&")
		.append(encode(normalizeRequestUrl(url)))
		.append("&")
		.append(encode(URLEncodedUtils.format(params, ENCODING))).toString();
	}
	
	private static String encode(String s)
	{
		if (TextUtils.isEmpty(s))
		{
			return "";
		}
		
		return Uri.encode(s);
	}
	
	private static String normalizeRequestUrl(String url)
	{
		Uri uri = Uri.parse(url.toLowerCase());
		String path = uri.getPath();
		if (TextUtils.isEmpty(path))
		{
			path = "/";
		}
		
		return new StringBuilder().append(uri.getScheme()).append("://").append(uri.getAuthority()).append(path).toString();
	}
}