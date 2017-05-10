package demo.activity.example;

import engine.android.core.Forelet;

/**
 * OAuth认证界面
 * @author yanhao
 * @version 1.0
 */

public class OAuthActivity extends Forelet {
//	
//	TextView tv;
//	
//	OAuthUtil oauth;
//	OAuthInfo info;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		tv = new TextView(this);
//		tv.setGravity(Gravity.CENTER);
//		tv.setTextSize(20);
//		
//		setContentView(tv);
//		
//		//http://dev.open.t.qq.com
//		oauth = new OAuthUtil("801343432", "a56de8210878c1b40d9b8d374459a143");
//		
//		if ((info = restore()) != null)
//		{
//			show();
//		}
//		else
//		{
//		    showProgress(settingProgress().setMessage("正在打开授权页面..."));
//		    execute(1, new TaskExecutor() {
//                
//                @Override
//                public Object onExecute() {
//                    oauth.init("https://open.t.qq.com/cgi-bin/request_token", 
//                            "http://open.t.qq.com/cgi-bin/authorize", 
//                            "https://open.t.qq.com/cgi-bin/access_token");
//                    return oauth.getAuthUrl("oauth://oauth");
//                }
//                
//                @Override
//                public void onCancel() {
//                    // TODO Auto-generated method stub
//                }
//            });
//		}
//	}
//	
//	@Override
//	protected void handleTaskCallback(int taskId, Object result) {
//	    switch (taskId) {
//            case 1:
//                if (result != null)
//                {
//                    System.out.println("授权地址：" + result.toString());
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result.toString())));
//                }
//                else
//                {
//                    tv.setText("获取授权地址失败");
//                }
//
//                hideProgress();
//                
//                break;
//            case 2:
//                if (result instanceof OAuthInfo)
//                {
//                    save(info = (OAuthInfo) result);
//                    show();
//                }
//                else
//                {
//                    tv.setText("获取认证信息失败");
//                }
//                
//                hideProgress();
//                
//                break;
//            case 3:
//                if (result != null && parseUserInfo(result.toString()))
//                {
//                    tv.setOnTouchListener(null);
//                }
//                else
//                {
//                    tv.setText("获取个人信息失败");
//                }
//                
//                hideProgress();
//                
//                break;
//            case 4:
//                if (result != null && parseVerify(result.toString()))
//                {
//                    tv.setText("账户已注册");
//                }
//                else
//                {
//                    tv.setText("账户未注册");
//                }
//                
//                setContentView(tv);
//                hideProgress();
//                
//                break;
//        }
//	}
//	
//	@Override
//	protected void onNewIntent(Intent intent) {
//		if ("oauth".equals(intent.getScheme()))
//    	{
//    		final String verifier = oauth.getVerifier(intent.getData());
//    		if (!TextUtils.isEmpty(verifier))
//    		{
//    			System.out.println("验证码：" + verifier);
//
//                showProgress(new TaskProgress("正在获取认证信息..."));
//                execute(2, new TaskExecutor() {
//                    
//                    @Override
//                    public Object onExecute() {
//                        return oauth.getOAuthInfo(verifier);
//                    }
//                    
//                    @Override
//                    public void onCancel() {
//                        // TODO Auto-generated method stub
//                    }
//                });
//    		}
//    		else
//    		{
//    			tv.setText("获取验证码失败");
//    		}
//    	}
//	}
//	
//	private void save(OAuthInfo info)
//	{
//		Editor e = getPreferences(MODE_PRIVATE).edit();
//		e.putBoolean("OAuth", true);
//		e.putString("name", info.getName());
//		e.putString("token", info.getToken());
//		e.putString("secret", info.getTokenSecret());
//		e.commit();
//	}
//	
//	private OAuthInfo restore()
//	{
//		SharedPreferences sp = getPreferences(MODE_PRIVATE);
//		if (sp.contains("OAuth"))
//		{
//			OAuthInfo info = new OAuthInfo();
//			info.setName(sp.getString("name", null));
//			info.setToken(sp.getString("token", null));
//			info.setTokenSecret(sp.getString("secret", null));
//			return info;
//		}
//		else
//		{
//			return null;
//		}
//	}
//	
//	private void show()
//	{
//		System.out.println(info);
//		tv.setText(info.toString());
//		tv.append("\n点击获取个人信息");
//		tv.setOnTouchListener(new OnTouchListener(){
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//                showProgress(new TaskProgress("正在获取个人信息"));
//                execute(3, new TaskExecutor() {
//                    
//                    @Override
//                    public Object onExecute() {
//                        String url = "http://open.t.qq.com/api/user/info";
//                        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
//                        list.add(new BasicNameValuePair("format", "json"));
//                        return getRequest(url, list);
//                    }
//                    
//                    @Override
//                    public void onCancel() {
//                        // TODO Auto-generated method stub
//                    }
//                });
//				
//				return true;
//			}});
//	}
//	
//	private boolean parseUserInfo(String s)
//	{
//		try {
//			System.out.println(s);
//			JSONObject json = new JSONObject(s);
//			int ret = json.getInt("ret");
//			if (ret == 0)
//			{
//				JSONObject data = json.getJSONObject("data");
//				String nick = data.getString("nick");
//				String head = data.getString("head");
//				if (TextUtils.isEmpty(nick) || TextUtils.isEmpty(head))
//				{
//					return false;
//				}
//				
//				Button b = new Button(this);
//				Bitmap image = Util.downloadImage(head + "/100");
//				if (image != null)
//				{
//					b.setBackgroundDrawable(new BitmapDrawable(image));
//				}
//				
//				b.setText(nick);
//				b.setTextColor(Color.BLUE);
//				b.setTextSize(20);
//				
//				b.setOnClickListener(new OnClickListener(){
//
//					@Override
//					public void onClick(View v) {
//		                showProgress(new TaskProgress("正在验证账户是否合法"));
//		                execute(4, new TaskExecutor() {
//		                    
//		                    @Override
//		                    public Object onExecute() {
//                                String url = "http://open.t.qq.com/api/user/verify";
//                                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
//                                list.add(new BasicNameValuePair("format", "json"));
//                                list.add(new BasicNameValuePair("name", info.getName()));
//                                return postRequest(url, list);
//		                    }
//		                    
//		                    @Override
//		                    public void onCancel() {
//		                        // TODO Auto-generated method stub
//		                    }
//		                });
//					}});
//				
//				setContentView(b);
//				
//				return true;
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return false;
//	}
//	
//	private boolean parseVerify(String s)
//	{
//		try {
//			System.out.println(s);
//			JSONObject json = new JSONObject(s);
//			int ret = json.getInt("ret");
//			if (ret == 0)
//			{
//				JSONObject data = json.getJSONObject("data");
//				return data.getBoolean("verify");
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return false;
//	}
//	
//	private String getRequest(String url, List<BasicNameValuePair> list)
//	{
//		try {
//			HttpConnector conn = new HttpConnector(url + "?" + oauth.sign(info.getToken(), info.getTokenSecret(), url, list, HttpGet.METHOD_NAME));
//			HttpEntity entity = conn.connect();
//			if (entity != null)
//			{
//				return EntityUtils.toString(entity);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
//	
//	private String postRequest(String url, List<BasicNameValuePair> list)
//	{
//		try {
//			HttpConnector conn = new HttpConnector(url, 
//					new StringEntity(oauth.sign(info.getToken(), info.getTokenSecret(), url, list, HttpPost.METHOD_NAME)));
//			conn.setHttpClient(new HttpConnector.HttpClientBuilder().setUseExpectContinue(false).build());
//			HttpEntity entity = conn.connect();
//			if (entity != null)
//			{
//				return EntityUtils.toString(entity);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
}