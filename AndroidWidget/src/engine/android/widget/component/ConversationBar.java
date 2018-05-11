package engine.android.widget.component;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import engine.android.util.listener.MyTextWatcher;
import engine.android.widget.R;

/**
 * 会话栏
 * PS:使用布局conversation_bar解析
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class ConversationBar extends LinearLayout {
    
    private static final int MODE_NONE = 0;
    private static final int MODE_VOICE = 1;
    private static final int MODE_EMOTION = 2;
    private static final int MODE_MORE = 3;
    
    private ImageView voice;
    private EditText input;
    private Button record;
    private ImageView emotion;
    private ImageView more;
    private Button send;
    
    private int mode;
    private Callback callback;

    public ConversationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        voice = (ImageView) findViewById(R.id.voice);
        input = (EditText) findViewById(R.id.input);
        record = (Button) findViewById(R.id.record);
        emotion = (ImageView) findViewById(R.id.emotion);
        more = (ImageView) findViewById(R.id.more);
        send = (Button) findViewById(R.id.send);
        
        Listener listener = new Listener();
        voice.setOnClickListener(listener);
        input.addTextChangedListener(listener);
        emotion.setOnClickListener(listener);
        more.setOnClickListener(listener);
        send.setOnClickListener(listener);
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void switchMode(int mode) {
        if (this.mode == mode)
        {
            setupMode(MODE_NONE);
        }
        else
        {
            setupMode(mode);
        }
    }
    
    private void setupMode(int mode) {
        this.mode = mode;
        voice.setImageResource(mode == MODE_VOICE ?
                R.drawable.conversation_keyboard : R.drawable.conversation_voice);
        showRecord(mode == MODE_VOICE);
        emotion.setImageResource(mode == MODE_EMOTION ?
                R.drawable.conversation_keyboard : R.drawable.conversation_emotion);
        showSend(mode != MODE_VOICE && mode != MODE_MORE && !TextUtils.isEmpty(input.getText()));
    }
    
    private void showRecord(boolean shown) {
        input.setVisibility(shown ? GONE : VISIBLE);
        record.setVisibility(shown ? VISIBLE : GONE);
    }
    
    private void showSend(boolean shown) {
        more.setVisibility(shown ? GONE : VISIBLE);
        send.setVisibility(shown ? VISIBLE : GONE);
    }
    
    private class Listener extends MyTextWatcher implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == voice)
            {
                switchMode(MODE_VOICE);
            }
            else if (v == emotion)
            {
                switchMode(MODE_EMOTION);
            }
            else if (v == more)
            {
                switchMode(MODE_MORE);
                input.clearFocus();
            }
            else if (v == send)
            {
                if (callback != null) callback.onSendMessage(input.getText());
                input.setText(null);
            }
        }

        @Override
        protected void changeFromEmpty(String after) {
            showSend(true);
        }

        @Override
        protected void changeToEmpty(String before) {
            showSend(false);
        }
    }
    
    public interface Callback {
        
        public void onSendMessage(CharSequence message);
    }
}