package com.project.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimon.yueba.R;

import engine.android.util.listener.MyTextWatcher;

public class ConversationBar extends LinearLayout {
    
    private static final int MODE_NONE = 0;
    private static final int MODE_VOICE = 1;
    private static final int MODE_EMOTION = 2;
    private static final int MODE_MORE = 3;
    
    ImageView voice;
    
    EditText input;
    
    Button record;
    
    ImageView emotion;
    
    ImageView more;
    
    Button send;
    
    int mode;
    
    private Callback callback;

    public ConversationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    @Override
    protected void onFinishInflate() {
        voice = (ImageView) findViewById(R.id.voice);
        input = (EditText) findViewById(R.id.input);
        record = (Button) findViewById(R.id.record);
        emotion = (ImageView) findViewById(R.id.emotion);
        more = (ImageView) findViewById(R.id.more);
        send = (Button) findViewById(R.id.send);
        
        voice.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                switchMode(MODE_VOICE);
            }
        });
        
        input.addTextChangedListener(new MyTextWatcher() {
            
            @Override
            protected void changeToEmpty(String before) {
                showSend(false);
            }
            
            @Override
            protected void changeFromEmpty(String after) {
                showSend(true);
            }
        });
        
        emotion.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                switchMode(MODE_EMOTION);
            }
        });
        
        more.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                switchMode(MODE_MORE);
                input.clearFocus();
            }
        });
        
        send.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (callback != null) callback.onSendMessage(input.getText());
                input.setText(null);
            }
        });
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
    
    public interface Callback {
        
        public void onSendMessage(CharSequence message);
    }
}