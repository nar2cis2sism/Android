package demo.activity.example;

import engine.android.core.Forelet;

public class WeiboActivity extends Forelet {
//	
//	OAuthUtil oauth;
//	AsyncImageLoader loader;
//	List<JSONObject> list;
//	
//	int pageSize = 20;
//	int pageTime;
//	
//	ListView lv;
//	WeiboAdapter adapter;
//	
//	View footer;
//	TextView more;
//	View loading;
//	
//	boolean isOver;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.weibo_activity);
//		
//		oauth = new OAuthUtil("801343432", "a56de8210878c1b40d9b8d374459a143");
//		loader = new AsyncImageLoader();
//		list = new ArrayList<JSONObject>();
//		
//		lv = (ListView) findViewById(R.id.lv);
//		
//		footer = getLayoutInflater().inflate(R.layout.weibo_footer, null);
//		more = (TextView) footer.findViewById(R.id.more);
//		loading = footer.findViewById(R.id.loading);
//		more.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				more.setVisibility(View.GONE);
//				loading.setVisibility(View.VISIBLE);
//				refresh();
//			}});
//		lv.addFooterView(footer);
//		
//		lv.setAdapter(adapter = new WeiboAdapter());
//		
//		showProgress(new TaskProgress("请稍等", "正在读取数据中!"));
//		refresh();
//	}
//	
//	@Override
//	protected void onDestroy() {
//		if (loader != null)
//		{
//			loader.release();
//		}
//		
//		super.onDestroy();
//	}
//	
//	private void refresh()
//	{
//	    execute(1, new TaskExecutor() {
//            
//            @Override
//            public Object onExecute() {
////              String s = getPreferences(MODE_PRIVATE).getString("weibo", null);
////              if (s == null)
////              {
////                  s = getHome();
////                  if (s != null)
////                  {
////                      Editor e = getPreferences(MODE_PRIVATE).edit();
////                      e.putString("weibo", s);
////                      e.commit();
////                  }
////                  else
////                  {
////                      return null;
////                  }
////              }
////              
////              return parseHome(s);
//                return parseHome(getHome());
//            }
//            
//            @Override
//            public void onCancel() {
//                // TODO Auto-generated method stub
//            }
//        });
//	}
//	
//	@Override
//	protected void handleTaskCallback(int taskId, Object result) {
//	    switch (taskId) {
//            case 1:
//                if (Boolean.TRUE.equals(result))
//                {
//                    if (isOver)
//                    {
//                        lv.removeFooterView(footer);
//                    }
//                    
//                    adapter.notifyDataSetChanged();
//                    more.setVisibility(View.VISIBLE);
//                    loading.setVisibility(View.GONE);
//                }
//                else
//                {
//                    openMessageDialog("提示", "读取数据出错", "确定");
//                }
//                
//                hideProgress();
//                
//                break;
//
//            default:
//                break;
//        }
//	}
//	
//	private String getHome()
//	{
//		String url = "http://open.t.qq.com/api/statuses/home_timeline";
//    	List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
//    	list.add(new BasicNameValuePair("format", "json"));
//    	list.add(new BasicNameValuePair("pageflag", pageTime == 0 ? "0" : "1"));
//    	list.add(new BasicNameValuePair("pagetime", String.valueOf(pageTime)));
//    	list.add(new BasicNameValuePair("reqnum", String.valueOf(pageSize)));
//
//    	try {
//			HttpConnector conn = new HttpConnector(url + "?" + oauth.sign("f772da059c84492eb3f24026fbd3aeed", "938245d2043e61380726e184d9a8375d", url, list, HttpGet.METHOD_NAME));
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
//	private boolean parseHome(String s)
//	{
//		try {
//			System.out.println(s);
//			JSONObject json = new JSONObject(s);
//			int ret = json.getInt("ret");
//			if (ret == 0)
//			{
//				JSONObject data = json.getJSONObject("data");
//				if (data.getInt("hasnext") == 1)
//				{
//					isOver = true;
//				}
//				
//				JSONArray info = data.getJSONArray("info");
//				if (info != null)
//				{
//					for (int i = 0, len = info.length(); i < len; i++)
//					{
//						list.add(data = info.optJSONObject(i));
//					}
//					
//					if (data != null)
//					{
//						pageTime = Util.getInt(data.getString("timestamp"), 0);
//					}
//					
//					return true;
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return false;
//	}
//	
//	private String getTime(String time)
//	{
//		long l = System.currentTimeMillis() / 1000 - Long.parseLong(time);
//		if (l > 24 * 60 * 60)
//		{
//			return l / (24 * 60 * 60) + "天前";
//		}
//		else if (l > 60 * 60)
//		{
//			return l / ( 60 * 60) + "小时前";
//		}
//		else if (l > 60)
//		{
//			return l / ( 60) + "分钟前";
//		}
//		else
//		{
//			return "刚刚";
//		}
//	}
//	
//	private void replace(SpannableStringBuilder span, Object style, List<Map<String, Object>> list)
//	{
//		for (Map<String, Object> map : list)
//		{
//			span.setSpan(style, (Integer) map.get("start"), (Integer) map.get("end"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		}
//	}
//	
//	class WeiboAdapter extends BaseAdapter {
//
//		@Override
//		public int getCount() {
//			return list.size();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return list.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			WeiboHolder holder = null;
//			if (convertView == null)
//			{
//				convertView = getLayoutInflater().inflate(R.layout.weibo_listitem, null);
//				holder = new WeiboHolder();
//				holder.head = (ImageView) convertView.findViewById(R.id.head);
//				holder.nick = (TextView) convertView.findViewById(R.id.nick);
//				holder.vip = (ImageView) convertView.findViewById(R.id.vip);
//				holder.hasImage = (ImageView) convertView.findViewById(R.id.hasImage);
//				holder.time = (TextView) convertView.findViewById(R.id.time);
//				holder.text = (TextView) convertView.findViewById(R.id.text);
//				holder.source = (TextView) convertView.findViewById(R.id.source);
//				convertView.setTag(holder);
//			}
//			else
//			{
//				holder = (WeiboHolder) convertView.getTag();
//			}
//			
//			JSONObject data = list.get(position);
//			if (data != null)
//			{
//				try {
//					//头像
//					final ImageView head = holder.head;
//					String headUrl = data.getString("head");
//					if (!TextUtils.isEmpty(headUrl))
//					{
//						Bitmap image = loader.loadImage(headUrl + "/100", new ImageCallback(){
//
//							@Override
//							public void imageLoaded(String url, Bitmap image) {
//								if (image != null)
//								{
//									head.setImageBitmap(image);
//								}
//							}
//
//							@Override
//							public Bitmap imageLoading(String url) {
//								Bitmap image = Util.downloadImage(url);
//								if (image != null)
//								{
//									image = ImageUtil.getRoundImage(image, 20);
//								}
//								
//								return image;
//							}});
//						if (image != null)
//						{
//							head.setImageBitmap(image);
//						}
//						else
//						{
//							head.setImageResource(R.drawable.weibo_head);
//						}
//					}
//					else
//					{
//						head.setImageResource(R.drawable.weibo_head);
//					}
//					
//					//昵称
//					holder.nick.setText(data.getString("nick"));
//					
//					//VIP
//					if (data.getInt("isvip") != 1)
//					{
//						//非vip隐藏vip标志
//						holder.vip.setVisibility(View.GONE);
//					}
//					else
//					{
//						holder.vip.setVisibility(View.VISIBLE);
//					}
//					
//					//图片
//					if (!"null".equals(data.getString("image")))
//					{
//						holder.hasImage.setImageResource(R.drawable.weibo_hasimage);
//					}
//					else
//					{
//						holder.hasImage.setImageDrawable(null);
//					}
//					
//					//时间
//					holder.time.setText(getTime(data.getString("timestamp")));
//					
//					//内容
//					holder.text.setText(data.getString("origtext"));
//					
//					//转载
//					holder.source.setText(null);
//					holder.source.setVisibility(View.INVISIBLE);
//					if (!"null".equals(data.getString("source")))
//					{
//						JSONObject source = data.getJSONObject("source");
//						if (source != null)
//						{
//							String text;
//							boolean isVip = source.getInt("isvip") == 1;
//							if (isVip)
//							{
//								text = "@" + source.getString("nick") + "======: " + source.getString("origtext");
//							}
//							else
//							{
//								text = "@" + source.getString("nick") + ": " + source.getString("origtext");
//							}
//							
//							SpannableStringBuilder span = new SpannableStringBuilder(text);
//							replace(span, new StyleSpan(Typeface.BOLD_ITALIC), MyValidator.parse(text, Pattern.compile("@.*: ")));
//							replace(span, new ForegroundColorSpan(Color.argb(255, 33, 92, 110)), MyValidator.parse(text, Pattern.compile("#.*#")));
//							replace(span, new URLSpan("http://www.baidu.com"), MyValidator.parse(text, MyValidator.WEB_URL));
//							if (isVip)
//							{
//								replace(span, new ImageSpan(WeiboActivity.this, R.drawable.weibo_vip), MyValidator.parse(text, Pattern.compile("======")));
//							}
//							
//							holder.source.setText(span);
//							holder.source.setVisibility(View.VISIBLE);
//						}
//					}
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			return convertView;
//		}
//	}
//	
//	static class WeiboHolder {
//		ImageView head;
//		TextView nick;
//		ImageView vip;
//		ImageView hasImage;
//		TextView time;
//		TextView text;
//		TextView source;
//	}
}