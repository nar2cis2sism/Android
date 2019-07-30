package engine.android.widget.component;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import engine.android.widget.R;

/**
 * 数字输入框
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class NumericInputBox extends LinearLayout implements OnClickListener, TextWatcher {
    
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 99;
    
    private ImageView decrement;
    private EditText input;
    private ImageView increment;
    
    private int number;
    
    public NumericInputBox(Context context) {
        super(context);
        init(context);
    }

    public NumericInputBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.numeric_input_box, this);
        
        decrement = (ImageView) findViewById(R.id.decrement);
        input = (EditText) findViewById(R.id.input);
        increment = (ImageView) findViewById(R.id.increment);
        
        decrement.setOnClickListener(this);
        input.addTextChangedListener(this);
        increment.setOnClickListener(this);
        
        setNumber(MIN_NUMBER);
    }
    
    public EditText input() {
        return input;
    }

    @Override
    public void onClick(View v) {
        if (v == decrement)
        {
            setNumber(number - 1);
        }
        else if (v == increment)
        {
            setNumber(number + 1);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        try {
            setNumber(number = Integer.parseInt(s.toString()));
        } catch (NumberFormatException e) {
            setNumber(MIN_NUMBER);
        }
    }
    
    private void setNumber(int num) {
        if (num > MAX_NUMBER)
        {
            num = MAX_NUMBER;
        }
        else if (num < MIN_NUMBER)
        {
            num = MIN_NUMBER;
        }
        
        if (num != number)
        {
            input.setText(String.valueOf(number = num));
        }
    }
    
    public int getNumber() {
        return number;
    }
}