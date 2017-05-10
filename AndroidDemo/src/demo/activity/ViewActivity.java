package demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import demo.android.R;
import demo.android.util.SystemUtil;
import demo.widget.GIFView;
import demo.widget.image.CircularImageView;
import engine.android.core.ApplicationManager;
import engine.android.core.Session;
import engine.android.util.ui.MyPasswordTransformationMethod;

import java.util.Locale;

public class ViewActivity extends Activity {
	
	LinearLayout layout;
	
	int level;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//设定调整音量为媒体音量，这样当程序中没有播放声音时调整音量就不会默认调整手机音量了
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);
		
		文本标记();
		Html文本();
		工具例解();
		Gif图片播放();
		自定义Progress();
        
        CircularImageView civ = new CircularImageView(this);
        civ.setImageResource(R.drawable.img0200);
        layout.addView(civ, new LayoutParams(100, 100));
        
        final EditText et = new EditText(this);
        et.setError("输入错误");
        et.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (et.getTransformationMethod() == MyPasswordTransformationMethod.getInstance())
                {
                    et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    et.setTransformationMethod(MyPasswordTransformationMethod.getInstance());
                }
            }
        });
        layout.addView(et, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        
        Button btn = new Button(this);
        btn.setText("切换语言(使用隐藏API)," + getString(com.android.internal.R.string.copy) + "(访问Internal包)");
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v)
            {
                final Locale locale = getResources().getConfiguration().locale;
                
                Session session = ApplicationManager.getSession();
                if (!session.hasAttribute("Locale"))
                {
                    session.setAttribute("Locale", locale);
                }
                
                if (locale.equals(Locale.US))
                {
                    SystemUtil.changeLocale(Locale.CHINA);
                    System.out.println(locale.getDisplayName(locale) + "->" + Locale.CHINA.getDisplayName(Locale.CHINA));
                }
                else
                {
                    SystemUtil.changeLocale(Locale.US);
                    System.out.println(locale.getDisplayName(locale) + "->" + Locale.US.getDisplayName(Locale.US));
                }
            }
        });
        addView(btn);
		
		final ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.level_list);
        addView(iv);
		final Handler handler = ApplicationManager.getHandler();
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				iv.setImageLevel(level++ % 20);
				handler.postDelayed(this, 100);
			}
		});
	}
	
	private void addView(View view)
	{
		layout.addView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	@Override
	public void onBackPressed()
	{
	    final Locale locale = (Locale) ApplicationManager.getSession().getAttribute("Locale");
	    if (locale == null || locale.equals(getResources().getConfiguration().locale))
	    {
	        super.onBackPressed();
	    }
	    else
	    {
	        SystemUtil.changeLocale(locale);
	    }
	}
	
	/**
	 * 使用反射技术加载drawable文件夹下的图片
	 * @param drawableName 图片名称（例:icon.png取icon）
	 * @return 如无此图片则返回Null
	 */
	
	public Bitmap load(String drawableName)
	{
		int resourceId = getResources().getIdentifier(getPackageName() + ":drawable/" + drawableName, null, null);
		if (resourceId == 0)
		{
			return null;
		}
		
		return BitmapFactory.decodeResource(getResources(), resourceId);
	}
	
	/**
	 * 设置静音
	 * @param mute
	 */
	
	public void setMute(boolean mute)
	{
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		audioManager.setStreamMute(AudioManager.STREAM_MUSIC , mute);
	}
	
	public void 对话框()
	{
		Dialog dialog = new AlertDialog.Builder(this)
        .setInverseBackgroundForced(true)//背景色和前景色交换
        .create();
		
		dialog.show();
	}
	
	/**
	1、BackgroundColorSpan 背景色 
    2、ClickableSpan 文本可点击，有点击事件
    3、ForegroundColorSpan 文本颜色（前景色）
    4、MaskFilterSpan 修饰效果，如模糊(BlurMaskFilter)、浮雕(EmbossMaskFilter)
    5、MetricAffectingSpan 父类，一般不用
    6、RasterizerSpan 光栅效果
    7、StrikethroughSpan 删除线（中划线）
    8、SuggestionSpan 相当于占位符
    9、UnderlineSpan 下划线
    10、AbsoluteSizeSpan 绝对大小（文本字体）
    11、DynamicDrawableSpan 设置图片，基于文本基线或底部对齐。
    12、ImageSpan 图片
    13、RelativeSizeSpan 相对大小（文本字体）
    14、ReplacementSpan 父类，一般不用
    15、ScaleXSpan 基于x轴缩放
    16、StyleSpan 字体样式：粗体、斜体等
    17、SubscriptSpan 下标（数学公式会用到）
    18、SuperscriptSpan 上标（数学公式会用到）
    19、TextAppearanceSpan 文本外貌（包括字体、大小、样式和颜色）
    20、TypefaceSpan 文本字体
    21、URLSpan 文本超链接
	 */
	
	public void 文本标记()
	{
		TextView tv = new TextView(this);
		addView(tv);
		
		SpannableStringBuilder span = new SpannableStringBuilder(getString(R.string.xliff, "各", 48, 2.28));
		//文本颜色标记
		span.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//背景颜色标记
		span.setSpan(new BackgroundColorSpan(Color.WHITE), 1, 2, 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//样式标记
		span.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 2, 3, 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//图片标记
		span.setSpan(new ImageSpan(this, R.drawable.icon), 3, 4, 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//链接标记
		span.setSpan(new URLSpan("geo:0,0?q=http://www.baidu.com"), 5, 9, 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(span);
		//支持链接跳转
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		//另一种方式
//		Linkify.addLinks(tv, Linkify.ALL);
		//可继承ClickableSpan实现自定义标记处理
	}
	
	public void Html文本()
	{
		TextView tv = new TextView(this);
		tv.setText(Html.fromHtml("中文粗体"));
		addView(tv);
		
		/**
		 * <a href="...">  定义链接内容
		 * <b> 定义粗体文字
		 * <big> 定义大字体的文字
		 * <br>  定义换行
		 * <div align="...">
		 * <em> 强调标签
		 * <font size="..." color="..." face="...">
		 * <h1>---------<h6>
		 * <i>  定义斜体文字
		 * <img src="...">
		 * <p>    段落标签,里面可以加入文字,列表,表格等
		 * <small> 定义小字体的文字
		 * <strong>  重点强调标签
		 * <sub>  下标标签
		 * <sup>  上标标签
		 * <u>  定义带有下划线的文字
		 */
		
		//将中文设置成粗体的方法是
		tv.getPaint().setFakeBoldText(true);
	}
	
	public void 组件示例()
	{
		//继承自AutoCompleteTextView，延长AutoCompleteTextView的长度，可输入多项
		MultiAutoCompleteTextView tv = new MultiAutoCompleteTextView(this);
		//用户正在输入时，tokenizer设置将用于确定文本相关范围内
		tv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
	}
	
	public void 工具例解()
	{
		//自定义字体
		Paint paint = new Paint();
		paint.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/samplefont.ttf"));
		
		TextView tv = new TextView(this);
		tv.setText("自定义字体:custom font");
		tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/samplefont.ttf"));
		addView(tv);
	}
	
	public void Gif图片播放()
	{
		GIFView gif = new GIFView(this);
		gif.setImageResource(R.raw.gif);
		addView(gif);
	}
	
	public void 自定义Progress()
	{
		ProgressBar progress = new ProgressBar(this);
		progress.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress));
		addView(progress);
	}
    
    public static void 有用API_demo()
    {
    	/**
    	 * com.example.android.apis.app
    	 * 联系人QuickContactsDemo
    	 * 壁纸SetWallpaperActivity
    	 * 闹钟AlarmController
    	 * 自定义设置界面MyPreference
    	 */
    	
    	/**
    	 * com.example.android.apis.graphics
    	 * 图形操作
    	 */
    	
    	/**
    	 * com.example.android.apis.os
    	 * 摩斯码MorseCodeConverter
    	 */
    	
    	/**
    	 * com.example.android.apis.animation
    	 * 布局动画及3D旋转Transition3d
    	 */
    	
    	/**
    	 * com.example.android.apis.view
    	 * 自定义TextView LabelView
    	 * 布局动画LayoutAnimation1-7
    	 */
        
        //DownloadManager
    }
}