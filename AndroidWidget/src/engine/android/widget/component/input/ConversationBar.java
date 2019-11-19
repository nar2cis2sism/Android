package engine.android.widget.component.input;

import engine.android.util.listener.MyTextWatcher;
import engine.android.util.os.AudioUtil;
import engine.android.util.os.AudioUtil.AudioRecorder;
import engine.android.util.ui.UIUtil;
import engine.android.widget.R;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

/**
 * 会话栏<br>
 * PS:使用布局conversation_bar解析
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class ConversationBar extends RelativeLayout {
    
    private static final int MODE_INPUT = 0;
    private static final int MODE_VOICE = 1;
    
    private ImageView voice;
    private EditText input;
    private Button record;
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
        send = (Button) findViewById(R.id.send);
        
        Listener listener = new Listener();
        voice.setOnClickListener(listener);
        input.addTextChangedListener(listener);
        record.setOnTouchListener(listener);
        send.setOnClickListener(listener);
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void switchMode(int mode) {
        setupMode(this.mode == mode ? MODE_INPUT : mode);
    }
    
    private void setupMode(int mode) {
        this.mode = mode;
        voice.setImageResource(mode == MODE_VOICE ?
                R.drawable.conversation_keyboard : R.drawable.conversation_voice);
        showRecord(mode == MODE_VOICE);
        showSend(mode != MODE_VOICE && !TextUtils.isEmpty(input.getText()));
    }
    
    private void showRecord(boolean shown) {
        input.setVisibility(shown ? GONE : VISIBLE);
        record.setVisibility(shown ? VISIBLE : GONE);
        if (shown)
        {
            input.clearFocus();
            UIUtil.hideSoftInput(input);
        }
        else
        {
            input.requestFocus();
            UIUtil.showSoftInput(input, 0);
        }
    }
    
    private void showSend(boolean shown) {
        send.setEnabled(shown);
        UIUtil.setupAlpha(send);
    }
    
    private class Listener extends MyTextWatcher implements OnClickListener, OnTouchListener, Runnable {

        @Override
        public void onClick(View v) {
            if (v == voice)
            {
                switchMode(MODE_VOICE);
            }
            else if (v == send)
            {
                if (callback != null)
                {
                    callback.onSendMessage(input.getText().toString());
                    input.setText(null);
                }
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

        private final AudioRecorder recorder = AudioUtil.record();
        private boolean isRecording;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (callback == null) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true);
                    record.setText(R.string.conversation_record_pressed);
                    // 开始录音
                    isRecording = true;
                    v.postDelayed(this, 200);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    
                    Rect rect = new Rect();
                    v.getLocalVisibleRect(rect);
                    
                    if (isRecording && !rect.contains(x, y))
                    {
                        record.setText(R.string.conversation_record_cancel);
                        // 取消录音
                        isRecording = false;
                    }
                    else if (!isRecording && rect.contains(x, y))
                    {
                        // 恢复录音
                        record.setText(R.string.conversation_record_pressed);
                        isRecording = true;
                    }
                    
                    break;
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    record.setText(R.string.conversation_record_normal);
                    // 结束录音
                    v.removeCallbacks(this);
                    File recordFile = recorder.stop();
                    if (isRecording)
                    {
                        callback.onRecordVoice(recordFile);
                    }
                    else if (recordFile != null)
                    {
                        recordFile.delete();
                    }
                    
                    break;
            }
            
            return true;
        }

        @Override
        public void run() {
            recorder.start();
        }
    }
    
    public interface Callback {
        
        /**
         * 发送一条消息
         */
        void onSendMessage(String message);
        
        /**
         * 录制一段语音
         */
        void onRecordVoice(File recordFile);
    }
}