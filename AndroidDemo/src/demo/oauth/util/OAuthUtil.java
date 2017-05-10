package demo.oauth.util;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;


import android.net.Uri;

import demo.oauth.OAuth;
import demo.oauth.OAuthConsumer;
import demo.oauth.OAuthProvider;

/**
 * OAuth认证工具
 * @author Daimon
 * @version 3.0
 * @since 3/23/2012
 */

public final class OAuthUtil {
	
	private OAuthConsumer consumer;
	
	private OAuthProvider provider;
	
	/**
	 * @param appKey,appSecret 应用程序有效签名及密钥
	 */
	
	public OAuthUtil(String appKey, String appSecret) {
		consumer = new OAuthConsumer(appKey, appSecret);
	}
	
	/**
	 * 初始化
	 * @param requestTokenEndpointUrl 请求未授权的Request Token--返回未授权的Request Token及Request Token Secret
	 * @param authorizationWebsiteUrl 平台授权页面，请求用户授权--返回已授权的Request Token和Verifier（验证码）
	 * @param accessTokenEndpointUrl 用已授权的Request Token换取Access Token--返回Access Token及Access Token Secret
	 */
	
	public void init(String requestTokenEndpointUrl, String authorizationWebsiteUrl, String accessTokenEndpointUrl)
	{
		provider = new OAuthProvider(requestTokenEndpointUrl, accessTokenEndpointUrl, authorizationWebsiteUrl);
	}
	
	/**
	 * 获取授权网址（需打开浏览器跳转至该页面，请求用户授权）
	 * @param callbackUrl 授权成功后，web应用会重定向到该网址（含返回参数）
	 * @return 出错则返回Null
	 */
	
	public String getAuthUrl(String callbackUrl)
	{
		if (provider == null)
		{
			throw new RuntimeException("未初始化");
		}
		
		try {
			String s = provider.retrieveRequestToken(consumer, callbackUrl);
			String callbackConfirmed = provider.getResponseParameter(OAuth.OAUTH_CALLBACK_CONFIRMED);
			if (Boolean.parseBoolean(callbackConfirmed))
			{
				return s;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 通过平台回调网址解析出验证码
	 * @param uri
	 * @return
	 */
	
	public String getVerifier(Uri uri)
	{
		return uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
	}
	
	/**
	 * 获取认证信息
	 * @param verifier 验证码
	 * @return 出错则返回Null
	 */
	
	public OAuthInfo getOAuthInfo(String verifier)
	{
		if (provider == null)
		{
			throw new RuntimeException("未初始化");
		}
		
		try {
			provider.retrieveAccessToken(consumer, verifier);
			
			OAuthInfo info = new OAuthInfo();
			info.setToken(consumer.getToken());
			info.setTokenSecret(consumer.getTokenSecret());
			info.setName(provider.getResponseParameter("name"));
			
			return info;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 对请求数据进行签名
	 * @param token,tokenSecret 访问令牌及密钥
	 * @param url 请求链接
	 * @param list 请求参数
	 * @param method 请求方式 POST or GET
	 * @return 已签名的请求参数
	 * @throws Exception
	 */
	
	public String sign(String token, String tokenSecret, String url, List<BasicNameValuePair> list, String method) throws Exception
	{
		consumer.setTokenWithSecret(token, tokenSecret);
		if (list != null && !list.isEmpty())
		{
			consumer.addParameter(list);
		}
		
		return consumer.sign(url, method);
	}
}