package engine.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.UIUtil;

/**
 * 输入框
 * 
 * @author Daimon
 */
public class InputBox extends LinearLayout {
    
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
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.input_box, this, true);
        
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
        // 清空输入框
        final ImageView clear = new ImageView(getContext());
        clear.setImageResource(R.drawable.input_clear);
        clear.setVisibility(View.GONE);
        place(clear);
        
        input.setOnFocusChangeListener(new OnFocusChangeListener() {
            
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
        });
        input.addTextChangedListener(new MyTextWatcher() {
            
            @Override
            protected void changeToEmpty(String before) {
                clear.setVisibility(View.GONE);
            }
            
            @Override
            protected void changeFromEmpty(String after) {
                clear.setVisibility(View.VISIBLE);
            }
        });
        
        clear.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                input.setText(null);
            }
        });
    }
}