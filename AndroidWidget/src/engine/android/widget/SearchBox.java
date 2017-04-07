package engine.android.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import engine.android.util.listener.MyTextWatcher;

/**
 * 搜索框
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class SearchBox extends LinearLayout implements OnClickListener {

    private ImageView search_icon;
    
    private EditText search_text;

    private ImageView search_voice;

    private ImageView search_clear;
    
    private boolean showVoiceIcon = true;
    
    public SearchBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        search_icon = (ImageView) findViewById(R.id.search_icon);
        search_text = (EditText) findViewById(R.id.search_text);
        search_voice = (ImageView) findViewById(R.id.search_voice);
        search_clear = (ImageView) findViewById(R.id.search_clear);
        
        setupView();
    }
    
    private void setupView() {
        new ClearAction();
        search_voice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        voiceRecognize();
    }
    
    private void voiceRecognize() {
        // TO DO: 语音识别
    }
    
    public void showSearchIcon(boolean show) {
        search_icon.setVisibility(show ? VISIBLE : GONE);
    }
    
    public void showVoiceIcon(boolean show) {
        search_voice.setVisibility((showVoiceIcon = show) ? VISIBLE : GONE);
    }
    
    public EditText getSearchEditText() {
        return search_text;
    }
    
    public void setSearchProvider(final SearchProvider searchProvider) {
        search_text.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                searchProvider.search(s.toString());
            }
        });
    }
    
    public interface SearchProvider {
        
        void search(CharSequence constraint);
    }
    
    private class ClearAction extends MyTextWatcher implements OnClickListener {
        
        public ClearAction() {
            search_clear.setOnClickListener(this);
            search_text.addTextChangedListener(this);
        }

        @Override
        protected void changeFromEmpty(String after) {
            if (showVoiceIcon)  search_voice.setVisibility(View.GONE);
            search_clear.setVisibility(View.VISIBLE);
        }

        @Override
        protected void changeToEmpty(String before) {
            if (showVoiceIcon) search_voice.setVisibility(View.VISIBLE);
            search_clear.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            search_text.setText(null);
        }
    }
}