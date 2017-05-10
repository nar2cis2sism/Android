package demo.oauth;

import android.text.TextUtils;

import engine.android.http.HttpConnector;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class OAuthProvider {

    private String requestTokenEndpointUrl;
    private String accessTokenEndpointUrl;
    private String authorizationWebsiteUrl;
    private Map<String, String> responseParameters;
    
    public OAuthProvider(String requestTokenEndpointUrl, String accessTokenEndpointUrl, String authorizationWebsiteUrl) {
    	this.requestTokenEndpointUrl = requestTokenEndpointUrl;
        this.accessTokenEndpointUrl = accessTokenEndpointUrl;
        this.authorizationWebsiteUrl = authorizationWebsiteUrl;
	}
    
    public String retrieveRequestToken(OAuthConsumer consumer, String callbackUrl) throws Exception
    {
    	consumer.setTokenWithSecret(null, null);
    	consumer.addParameter(new BasicNameValuePair(OAuth.OAUTH_CALLBACK, callbackUrl == null ? "null" : callbackUrl));
    	
    	retrieveToken(consumer, requestTokenEndpointUrl);
    	
    	return new StringBuilder()
		.append(authorizationWebsiteUrl)
		.append("?")
		.append(OAuth.OAUTH_TOKEN)
		.append("=")
		.append(consumer.getToken()).toString();
    }
    
    public void retrieveAccessToken(OAuthConsumer consumer, String oauthVerifier) throws Exception
    {
    	if (oauthVerifier != null)
    	{
    		consumer.addParameter(new BasicNameValuePair(OAuth.OAUTH_VERIFIER, oauthVerifier));
    	}
    	
    	retrieveToken(consumer, accessTokenEndpointUrl);
    }
    
    private void retrieveToken(OAuthConsumer consumer, String endpointUrl) throws Exception
	{
		String response = sendRequest(endpointUrl + "?" + consumer.sign(endpointUrl, HttpGet.METHOD_NAME));
		System.out.println("response:" + response);
		Map<String, String> map = parseQueryParameters(response);
		String token = map.remove(OAuth.OAUTH_TOKEN);
	    String secret = map.remove(OAuth.OAUTH_TOKEN_SECRET);
	    setResponseParameters(map);
	    if(token == null || secret == null)
	    {
	    	throw new Exception("Request token or token secret not set in server reply. The service provider you use is probably buggy.");
	    }
	    
	    consumer.setTokenWithSecret(token, secret);
	}

	protected String sendRequest(String endpointUrl) throws Exception
    {
    	return EntityUtils.toString(new HttpConnector(endpointUrl).connect(), OAuth.ENCODING);
    }

    public String getRequestTokenEndpointUrl()
    {
        return requestTokenEndpointUrl;
    }

    public String getAccessTokenEndpointUrl()
    {
        return accessTokenEndpointUrl;
    }

    public String getAuthorizationWebsiteUrl()
    {
        return authorizationWebsiteUrl;
    }
    
    public Map<String, String> getResponseParameters()
    {
    	return responseParameters;
    }
    
    public String getResponseParameter(String name)
    {
    	if (responseParameters != null)
    	{
    		return responseParameters.get(name);
    	}
    	
    	return null;
    }
    
    private void setResponseParameters(Map<String, String> responseParameters) {
		this.responseParameters = responseParameters;
	}
	
	private Map<String, String> parseQueryParameters(String response)
	{
		if (!TextUtils.isEmpty(response))
    	{
        	Map<String, String> map = new HashMap<String, String>();
    		String[] array = response.split("&");
    		if (array != null && array.length > 0)
    		{
    			for (String s : array)
    			{
    				int index = s.indexOf("=");
    				if (index > 0)
    				{
    					map.put(s.substring(0, index), s.substring(index + 1));
    				}
    			}
    		}
        	
        	return map;
    	}
    	
    	return null;
	}
}