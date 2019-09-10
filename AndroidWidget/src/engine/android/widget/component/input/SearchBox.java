package engine.android.widget.component.input;

import engine.android.util.listener.MyTextWatcher;
import engine.android.widget.R;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * 搜索框<br>
 * PS:使用布局search_box解析
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class SearchBox extends LinearLayout implements OnClickListener {

    private ImageView search_icon;
    private EditText search_text;
    private ImageView search_voice;
    private ImageView search_clear;
    
    public SearchBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        search_icon = (ImageView) findViewById(R.id.search_icon);
        search_text = (EditText) findViewById(R.id.search_text);
        search_voice = (ImageView) findViewById(R.id.search_voice);
        search_clear = (ImageView) findViewById(R.id.search_clear);
        
        search_voice.setOnClickListener(this);
        new InputAction(search_text).bindClear(search_clear);
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
        search_voice.setVisibility(show ? VISIBLE : GONE);
    }
    
    public EditText getSearchEditText() {
        return search_text;
    }
    
    /**
     * 设置搜索器
     */
    public void setSearchProvider(final SearchProvider searchProvider) {
        search_text.addTextChangedListener(new MyTextWatcher() {
            
            @Override
            public void afterTextChanged(Editable s) {
                searchProvider.search(s.toString(), false);
            }
        });
        search_text.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    searchProvider.search(v.getText().toString(), true);
                    return true;
                }
                
                return false;
            }
        });
    }
    
    public interface SearchProvider {
        
        /**
         * @param key 搜索关键字
         * @param imeAction 键盘上的搜索按钮点击事件
         */
        void search(String key, boolean imeAction);
    }
    
    private static class InputAction extends InputBox.InputAction {

        public InputAction(EditText input) {
            super(input);
        }
        
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // Do nothing.
        }
    }
}