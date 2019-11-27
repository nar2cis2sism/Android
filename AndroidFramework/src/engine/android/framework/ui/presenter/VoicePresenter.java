package engine.android.framework.ui.presenter;

import static android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH;
import static android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL;
import static android.speech.RecognizerIntent.EXTRA_PROMPT;
import static android.speech.RecognizerIntent.EXTRA_RESULTS;
import static android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
import static android.speech.tts.TextToSpeech.Engine.ACTION_CHECK_TTS_DATA;
import static android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA;
import static android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA;
import static android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL;
import static android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA;
import static android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME;
import static android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_PASS;
import static android.speech.tts.TextToSpeech.LANG_MISSING_DATA;
import static android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED;

import engine.android.core.BaseFragment;
import engine.android.core.BaseFragment.Presenter;
import engine.android.util.ui.MyValidator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 语音识别及播放
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class VoicePresenter extends Presenter<BaseFragment> {

    private static final int REQUEST_RECOGNIZE  = 1;
    private static final int REQUEST_SPEECH     = 2;

    private Context context;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Context context) {
        this.context = context;
    }

    @Override
    protected void onDestroy() {
        if (tts != null)
        {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /**
     * 查询手机是否支持语音识别
     */
    public boolean isSupportRecognize() {
        Intent intent = new Intent(ACTION_RECOGNIZE_SPEECH);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        return !list.isEmpty();
    }

    /**
     * 语音识别
     */
    public void recognize() {
        Intent intent = new Intent(ACTION_RECOGNIZE_SPEECH);
        // 设置语言模式为自由形式的语音识别
        intent.putExtra(EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL_FREE_FORM);
        // 提示语音开始
        intent.putExtra(EXTRA_PROMPT, "Speech recognition demo");
        // 开始执行我们的Intent、语音识别
        try {
            getCallbacks().startActivityForResult(intent, REQUEST_RECOGNIZE);
        } catch (Exception e) {
            // 手机不支持语音识别，跳转到下载页面
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=com.google.android.voicesearch"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getCallbacks().startActivity(intent);
        }
    }

    /**
     * 检测TTS(Text-to-speech)语音朗读数据是否可用
     */
    public void checkTTS() {
        getCallbacks().startActivityForResult(new Intent(ACTION_CHECK_TTS_DATA), REQUEST_SPEECH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RECOGNIZE:
                if (resultCode == Activity.RESULT_OK)
                {
                    try {
                        ArrayList<String> matches = data.getStringArrayListExtra(EXTRA_RESULTS);
                        System.out.println(matches);
                        if (matches != null)
                        {
                            String s = URLEncoder.encode(matches.toString(), "GB2312");
                            System.out.println(s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case REQUEST_SPEECH:
                switch (resultCode) {
                    case CHECK_VOICE_DATA_PASS:
                        // TTS已经安装并且可用
                        tts = new TextToSpeech(context, listener);
                        break;
                    case CHECK_VOICE_DATA_FAIL:
                        // 检查数据失败
                        onCheckTTS(false);
                        break;
                    case CHECK_VOICE_DATA_BAD_DATA:
                        // 需要的语音数据已损坏
                    case CHECK_VOICE_DATA_MISSING_DATA:
                        // 缺少需要语言的语音数据
                    case CHECK_VOICE_DATA_MISSING_VOLUME:
                        // 缺少需要语言的发音数据

                        // 这三种情况都表明数据有错,重新下载安装需要的数据
                        getCallbacks().startActivity(new Intent(ACTION_INSTALL_TTS_DATA));
                        break;
                }

                break;
        }
    }

    private final OnInitListener listener = new OnInitListener() {

        @Override
        public void onInit(int status) {
            onCheckTTS(status == TextToSpeech.SUCCESS);
        }
    };

    private void onCheckTTS(boolean success) {
        onTTSCallback(success ? tts : (tts = null));
    }

    public void speak(String text) {
        if (tts == null)
        {
            return;
        }

        Locale loc = Locale.ENGLISH;
        if (MyValidator.validate(text, MyValidator.CHINESE))
        {
            loc = Locale.CHINESE;
        }

        int result = tts.setLanguage(loc);
        if (result == LANG_MISSING_DATA
        ||  result == LANG_NOT_SUPPORTED)
        {
            // 不支持语言
            tts.setLanguage(Locale.ENGLISH);
            text = "Language is not supported";
        }

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onTTSCallback(TextToSpeech tts) {}
}