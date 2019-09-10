package engine.android.widget.component.input;

import engine.android.widget.R;

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

/**
 * 数字加减输入框
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class NumericInputBox extends LinearLayout implements OnClickListener, TextWatcher {
    
    public ImageView decrement;
    public EditText input;
    public ImageView increment;
    
    private int minNumber = 1;
    private int maxNumber = 99;
    private int number;
    
    private OnChangeListener listener;
    
    public NumericInputBox(Context context) {
        this(context, null);
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
        
        setNumber(number);
    }

    public void setRange(int minNumber, int maxNumber) {
        this.minNumber = minNumber;
        this.maxNumber = maxNumber;
        setNumber(number);
    }

    public void setListener(OnChangeListener listener) {
        this.listener = listener;
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
            setNumber(Integer.parseInt(s.toString()));
        } catch (NumberFormatException e) {
            setNumber(minNumber);
        }
    }
    
    private void setNumber(int num) {
        if (num > maxNumber)
        {
            num = maxNumber;
        }
        else if (num < minNumber)
        {
            num = minNumber;
        }
        
        if (num != number)
        {
            boolean change = true;
            if (listener != null)
            {
                change = listener.onChanged(number, num);
            }

            if (change) number = num;
            input.setTextKeepState(String.valueOf(number));
        }
    }
    
    public int getNumber() {
        return number;
    }

    public interface OnChangeListener {

        /**
         * @return 是否允许改变
         */
        boolean onChanged(int before, int after);
    }
}