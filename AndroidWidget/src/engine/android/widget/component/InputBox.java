package engine.android.widget.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.UIUtil;
import engine.android.widget.R;

/**
 * 输入框
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class InputBox extends RelativeLayout {
    
    private EditText input;
    private View placeholder;
    
    public InputBox(Context context) {
        super(context);
        init(context);
    }

    public InputBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.input_box, this);
        
        input = (EditText) findViewById(R.id.input);
        placeholder = findViewById(R.id.placeholder);
    }
    
    public EditText input() {
        return input;
    }
    
    public void place(View view) {
        UIUtil.replace(placeholder, placeholder = view, null);
    }
    
    /**
     * 开启清空输入框功能
     */
    public void enableClear() {
        new ClearAction();
    }
    
    private class ClearAction extends MyTextWatcher implements 
    OnFocusChangeListener, OnClickListener {
        
        private final ImageView clear;
        
        public ClearAction() {
            clear = makeClear();
            input.setOnFocusChangeListener(this);
            input.addTextChangedListener(this);
        }
        
        private ImageView makeClear() {
            ImageView clear = new ImageView(getContext());
            clear.setVisibility(View.GONE);
            clear.setImageResource(R.drawable.input_clear);
            clear.setOnClickListener(this);
            place(clear);
            return clear;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
            {
                if (input.getText().length() > 0)
                {
                    clear.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                clear.setVisibility(View.GONE);
            }
        }

        @Override
        protected void changeFromEmpty(String after) {
            clear.setVisibility(View.VISIBLE);
        }

        @Override
        protected void changeToEmpty(String before) {
            clear.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            input.setText(null);
        }
    }
}