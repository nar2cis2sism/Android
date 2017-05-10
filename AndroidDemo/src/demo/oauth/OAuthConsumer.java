package demo.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class OAuthConsumer {
	
    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String tokenSecret;
    
    private List<BasicNameValuePair> additionalParameters;
    
    public OAuthConsumer(String consumerKey, String consumerSecret) {
    	this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
	}
    
    public void addParameter(BasicNameValuePair pair)
    {
    	if (additionalParameters == null)
    	{
    		additionalParameters = new ArrayList<BasicNameValuePair>();
    	}
    	
    	additionalParameters.add(pair);
    }
    
    public void addParameter(List<BasicNameValuePair> list)
    {
    	if (additionalParameters == null)
    	{
    		additionalParameters = new ArrayList<BasicNameValuePair>();
    	}
    	
    	additionalParameters.addAll(list);
    }
    
    public String sign(String url, String method) throws Exception
    {
    	if (consumerKey == null)
    	{
    		throw new Exception("consumer key not set");
    	}
    	
    	if (consumerSecret == null)
    	{
    		throw new Exception("consumer secret not set");
    	}
    	
    	List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
    	if (additionalParameters != null)
    	{
    		parameters.addAll(additionalParameters);
    		additionalParameters.clear();
    		additionalParameters = null;
    	}

    	parameters.add(new BasicNameValuePair(OAuth.OAUTH_CONSUMER_KEY, consumerKey));
    	parameters.add(new BasicNameValuePair(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMACSHA1SignatureMethod));
    	parameters.add(new BasicNameValuePair(OAuth.OAUTH_TIMESTAMP, generateTimestamp()));
    	parameters.add(new BasicNameValuePair(OAuth.OAUTH_NONCE, generateNonce()));
    	parameters.add(new BasicNameValuePair(OAuth.OAUTH_VERSION, OAuth.VERSION_1_0));
    	
    	if (token != null)
    	{
    		parameters.add(new BasicNameValuePair(OAuth.OAUTH_TOKEN, token));
    	}
    	
    	String signature = OAuth.sign(url, method, parameters, consumerSecret, tokenSecret);
    	parameters.add(new BasicNameValuePair(OAuth.OAUTH_SIGNATURE, signature));
    	
    	return URLEncodedUtils.format(parameters, OAuth.ENCODING);
    }
    
    public void setTokenWithSecret(String token, String tokenSecret)
    {
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    public String getToken()
    {
        return token;
    }

    public String getTokenSecret()
    {
        return tokenSecret;
    }

    public String getConsumerKey()
    {
        return consumerKey;
    }

    public String getConsumerSecret()
    {
        return consumerSecret;
    }

    private String generateTimestamp()
    {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    private String generateNonce()
    {
    	return String.valueOf(new Random().nextInt());
    }
}