package demo.oauth.util;

/**
 * OAuth认证信息
 * @author Daimon
 * @version 3.0
 * @since 3/23/2012
 */

public class OAuthInfo {
	
	private String name;							//用户账号
	
	private String token;							//访问令牌
	
	private String tokenSecret;						//访问密钥

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
			   .append("name:")
			   .append(name)
			   .append("\n")
			   .append("token:")
			   .append(token)
			   .append("\n")
			   .append("tokenSecret:")
			   .append(tokenSecret)
			   .append("\n").toString();
	}
}