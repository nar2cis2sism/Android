package demo.activity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import demo.admin.MyAdmin;
import demo.android.R;
import demo.android.ui.FullScreenProxy;
import demo.android.ui.util.SystemUiHider.OnVisibilityChangeListener;
import engine.android.core.ApplicationManager;
import engine.android.util.AndroidUtil;
import engine.android.util.listener.HomeWatcher;
import engine.android.util.listener.HomeWatcher.HomeListener;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IntentActivity extends Activity implements OnInitListener, HomeListener {

    HomeWatcher homeWatcher;

    FullScreenProxy fullScreenProxy;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intent);

        homeWatcher = new HomeWatcher(this);
        homeWatcher.setHomeListener(this);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        Button button = new Button(this);
        button.setText("打开浏览器");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openWeb("http://www.baidu.com");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("打开地图");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openMap(38.899533, -77.036476);
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("调出拨号界面");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialPhone("15010354625");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("直接拨打电话");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                callPhone("15010354625");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("发送短信（自己输电话号码）");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendSMS("The SMS text");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("发送短信");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendSMS("15010354625", "The SMS text");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("发送多媒体短信");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMMS("The SMS text");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("发送邮件");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendEmail("yanhao@pica.com", "The email body text");
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("播放多媒体");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playMedia();
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("拍照");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                takePhoto(null);
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("语音识别");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                voiceRecognize();
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("查看联系人");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showContacts();
            }
        });
        layout.addView(button);

        button = new Button(this);
        button.setText("打开相册");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });
        layout.addView(button);

        if (AndroidUtil.getVersion() >= 8)
        {
            button = new Button(this);
            button.setText("锁屏");
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    lockScreen();
                }
            });
            layout.addView(button);
        }

        button = new Button(this);
        button.setText("无效占位控件");
        layout.addView(button);

        button = new Button(this);
        button.setText("无效占位控件");
        layout.addView(button);
    }

    /**
     * 打开WEB浏览器
     * 
     * @param url 网址
     */

    public void openWeb(String url)
    {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(url)));
    }

    /**
     * 打开Google地图
     * 
     * @param x 经度
     * @param y 纬度
     */

    public void openMap(double x, double y)
    {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:" + x + "," + y)));
    }

    /**
     * 调出拨号界面
     * 
     * @param number 电话号码
     */

    public void dialPhone(String number)
    {
        startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + number)));
    }

    /**
     * 直接拨打电话 需要加权限<uses-permission android:name="android.permission.CALL_PHONE"
     * />
     * 
     * @param number 电话号码
     */

    public void callPhone(String number)
    {
        startActivity(new Intent(Intent.ACTION_CALL,
                Uri.parse("tel:" + number)));
    }

    /**
     * 发送短信（调出发短信界面，需手动写电话号码）
     * 
     * @param content 短信内容
     */

    public void sendSMS(String content)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("sms_body", content);
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);
    }

    /**
     * 发送短信（调出发短信界面）
     * 
     * @param number 电话号码
     * @param content 短信内容
     */

    public void sendSMS(String number, String content)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
        intent.putExtra("sms_body", content);
        startActivity(intent);
    }

    /**
     * 发送多媒体短信
     * 
     * @param content 短信内容
     */

    public void sendMMS(String content)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("sms_body", content);
        intent.putExtra(Intent.EXTRA_STREAM,
                Uri.parse("content://media/external/images/media/23"));
        intent.setType("image/png");
        startActivity(intent);
    }

    /**
     * 发送Email
     * 
     * @param address 邮箱地址
     * @param content 邮件内容
     */

    public void sendEmail(String address, String content)
    {
        // 直接发送Email到邮箱
        // startActivity(new Intent(Intent.ACTION_SENDTO,
        // Uri.parse("mailto:" + address)));
        // 发送带内容的Email
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, address);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Choose Email Client"));
        // 发送带主题的Email
        // Intent intent = new Intent(Intent.ACTION_SEND);
        // String[] tos = {address};
        // //抄送地址
        // String[] ccs = {address};
        // intent.putExtra(Intent.EXTRA_EMAIL, tos);
        // intent.putExtra(Intent.EXTRA_CC, ccs);
        // intent.putExtra(Intent.EXTRA_TEXT, content);
        // //主题
        // intent.putExtra(Intent.EXTRA_SUBJECT, "The email subject text");
        // intent.setType("message/rfc822");
        // startActivity(Intent.createChooser(intent, "Choose Email Client"));
        // 传送附件
        // Intent intent = new Intent(Intent.ACTION_SEND);
        // intent.putExtra(Intent.EXTRA_SUBJECT, "The email subject text");
        // intent.putExtra(Intent.EXTRA_STREAM, "file:///sdcard/mysong.mp3");
        // intent.setType("audio/mp3");
        // startActivity(Intent.createChooser(intent, "Choose Email Client"));
    }

    /**
     * 播放多媒体
     */

    public void playMedia()
    {
        // Intent intent = new Intent(Intent.ACTION_VIEW);
        // intent.setDataAndType(Uri.parse("file:///sdcard/song.mp3"),
        // "audio/mp3");
        // startActivity(intent);

        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "1")));
    }

    /**
     * 安装应用程序
     * 
     * @param url APK包的路径地址
     */

    public void installAPK(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    /**
     * 卸载应用程序
     * 
     * @param packageName 包名
     */

    public void unistallAPK(String packageName)
    {
        startActivity(new Intent(Intent.ACTION_DELETE,
                Uri.fromParts("package", packageName, null)));
    }

    /**
     * 拍照
     * 
     * @param output 照片输出位置
     */

    public void takePhoto(String output)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);//录像
        if (output != null)
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(output)));
        }

        startActivityForResult(intent, 10);
    }

    /**
     * 语音识别
     */

    public void voiceRecognize()
    {
        // ͨ通过Intent传递语音识别的模式
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // 查询手机是否支持语音识别
        List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, 0);
        if (!infos.isEmpty())
        {
            // 设置语言模式为自由形式的语音识别
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            // 提示语音开始
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
            // 开始执行我们的Intent、语音识别
            startActivityForResult(intent, 11);
        }
        else
        {
            Toast.makeText(this, "手机不支持语音识别", Toast.LENGTH_LONG).show();
            // 需要下载
            intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://market.android.com/details?id=com.google.android.voicesearch"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void openSetting()
    {
        // 打开系统设置中的我的位置界面,手动开启或关闭GPS
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /**
     * 查看联系人
     */

    public void showContacts()
    {
        // startActivity(new Intent(Intent.ACTION_VIEW,
        // ContactsContract.Contacts.CONTENT_URI));
        startActivity(new Intent(Intent.ACTION_VIEW, People.CONTENT_URI));
    }

    /**
     * 选择联系人
     */

    public void chooseContact()
    {
        // Intent intent = new Intent(Intent.ACTION_PICK,
        // ContactsContract.Contacts.CONTENT_URI);
        Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
        startActivityForResult(intent, 12);
    }

    /**
     * 检测TTS(Text-to-speech)语音朗读数据是否可用
     */

    public void checkTTS()
    {
        startActivityForResult(new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA), 13);
    }

    /**
     * 从相册中选取图片
     */

    public void pickPhoto()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);// Daimon:打开其他应用加上这一句
        startActivityForResult(intent, 14);
    }

    /**
     * 裁剪图片
     * 
     * @param photo
     */

    public void cropPhoto(Uri photo)
    {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setData(photo);

        intent.putExtra("crop", "true");
        // 裁剪比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 图像尺寸
        intent.putExtra("outputX", 96);
        intent.putExtra("outputY", 96);
        // 拉伸图片
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, 10);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addContentView(getLayoutInflater().inflate(R.layout.dummy_button, null),
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        final View dummy_button = findViewById(R.id.dummy_button);
        
        dummy_button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                fullScreenProxy.toggle();
            }
        });

        fullScreenProxy = new FullScreenProxy(this);
        fullScreenProxy.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            
            @Override
            public void onVisibilityChange(boolean visible) {
                Animation anim = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, visible ? 1 : 0,
                        Animation.RELATIVE_TO_SELF, visible ? 0 : 1);
                anim.setDuration(getResources().getInteger(
                        android.R.integer.config_shortAnimTime));
                anim.setFillAfter(true);
                dummy_button.startAnimation(anim);
            }
        });

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        fullScreenProxy.delayedHide(100);
    }

    @Override
    public void onAttachedToWindow() {
        // 屏蔽Home键
        try
        {
//            getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);// 4.0以后版本无效，而且可能会报异常
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onAttachedToWindow();
    }

    @Override
    protected void onStart()
    {
        homeWatcher.startWatch();
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        homeWatcher.stopWatch();
        super.onStop();
    }

    @SuppressWarnings("unused")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case 10:
                try {
                    // 获取照片
                    Bitmap image = data.getParcelableExtra("data");
                    System.out.println(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case 11:
                // 取得语音的字符
                try {
                    ArrayList<String> matches = data.getStringArrayListExtra
                            (RecognizerIntent.EXTRA_RESULTS);
                    System.out.println(matches);
                    if (matches != null)
                    {
                        String s = URLEncoder.encode(matches.toString(), "GB2312");
                        System.out.println(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case 12:
                // 取得联系人
                Uri uri = data.getData();
                Cursor c = managedQuery(uri, null, null, null, null);
                if (c != null && c.moveToFirst())
                {
                    String personId = c.getString(c.getColumnIndexOrThrow(People._ID));// 联系人ID
                    String name = c.getString(c.getColumnIndexOrThrow(People.NAME));// 联系人名称
                    // 查询联系人电话号码
                    Cursor phone = getContentResolver().query(Phones.CONTENT_URI, null,
                            Phones.PERSON_ID + "=" + personId, null, null);
                    while (phone.moveToNext())
                    {
                        int type = phone.getInt(phone.getColumnIndexOrThrow(Phones.TYPE));// 电话类型
                        String number = phone.getString(phone.getColumnIndexOrThrow(Phones.NUMBER));// 电话号码
                    }

                    phone.close();
                }

                break;
            case 13:
                // TTS
                switch (resultCode) {
                    case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                        // TTS已经安装并且可用（参考API DEMO的TextToSpeechActivity）
                        tts = new TextToSpeech(this, this);
                        break;
                    case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                        // 检查数据失败
                        break;
                    case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
                        // 需要的语音数据已损坏
                    case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
                        // 缺少需要语言的语音数据
                    case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
                        // 缺少需要语言的发音数据

                        // 这三种情况都表明数据有错,重新下载安装需要的数据
                        startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
                        break;
                }

                break;
            case 14:
                try {
                    uri = data.getData();
                    System.out.println(uri);
                    Bitmap image = Media.getBitmap(getContentResolver(), uri);
                    System.out.println(image);
                    cropPhoto(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ApplicationManager.getHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // 三种获取屏幕尺寸的方法，结果都一样
                Display dis = getWindowManager().getDefaultDisplay();
                int width = dis.getWidth();
                int height = dis.getHeight();
                System.out.println("宽高比：" + width + ":" + height);

                {
                    DisplayMetrics dm = getResources().getDisplayMetrics();// 此方法在1.5上得到的density不正确，需用下面的方式
                    System.out.println("宽高比：" + dm.widthPixels + ":" + dm.heightPixels);
                }

                DisplayMetrics dm = AndroidUtil.getResolution(IntentActivity.this);
                System.out.println("宽高比：" + dm.widthPixels + ":" + dm.heightPixels);

                boolean orientation = AndroidUtil.isLandscape(IntentActivity.this);
                System.out.println("屏幕方向：" + orientation == null ? "不知道" : orientation ? "横屏"
                        : "竖屏");
                System.out.println("状态栏高度：" + AndroidUtil.getStatusBarHeight(IntentActivity.this));
                System.out.println("标题栏高度：" + AndroidUtil.getTitleBarHeight(IntentActivity.this));
                System.out.println("操作栏高度：" + AndroidUtil.getActionBarHeight(IntentActivity.this));
            }
        }, 100);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoomout_in, R.anim.zoomout_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    TextToSpeech tts;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS)
        {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                // 不支持语言
            }
            else
            {
                tts.speak("welcome to use", TextToSpeech.QUEUE_FLUSH, null);
                tts.stop();
                tts.shutdown();
            }
        }
    }

    /**
     * 锁屏（2.2以上可用）
     */

    public void lockScreen()
    {
        try {
            IDevicePolicyManager iDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(
                    AndroidUtil.getServiceIBinder(Context.DEVICE_POLICY_SERVICE));

            // 定义组件的名称
            ComponentName mAdminName = new ComponentName(this, MyAdmin.class);
            // 注册权限
            if (iDevicePolicyManager != null)
            {
                boolean isAdminActive = false;
                try
                {
                    // 判断自定义的广播接收器是不是被注册成deviceadmin的权限
                    isAdminActive = iDevicePolicyManager.isAdminActive(mAdminName);
                } catch (Throwable e)
                {
                    e.printStackTrace();
                }

                if (!isAdminActive)
                {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                    startActivity(intent);
                }

                // 调用服务实现锁屏
                iDevicePolicyManager.lockNow();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHomePressed()
    {
        System.out.println("onHomePressed");
    }

    @Override
    public void onHomeLongPressed()
    {
        System.out.println("onHomeLongPressed");
    }
}