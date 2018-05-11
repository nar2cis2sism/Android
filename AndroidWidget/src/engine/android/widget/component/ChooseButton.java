package engine.android.widget.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import engine.android.widget.R;

/**
 * 按钮切换控件
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class ChooseButton extends RadioGroup {
    
    private static final int INDEX_FIRST = 1;
    
    private RadioButton button_positive;
    private RadioButton button_negative;
    
    private int neutralIndex = INDEX_FIRST;
    
    public ChooseButton(Context context) {
        super(context);
        init(context);
    }
    
    public ChooseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.choose_button, this);

        button_positive = (RadioButton) findViewById(R.id.button_positive);
        button_negative = (RadioButton) findViewById(R.id.button_negative);
    }

    public RadioButton setPositiveButton(int textId, 
            CompoundButton.OnCheckedChangeListener listener) {
        button_positive.setText(textId);
        button_positive.setOnCheckedChangeListener(listener);
        return button_positive;
    }
    
    public RadioButton setPositiveButton(CharSequence text, 
            CompoundButton.OnCheckedChangeListener listener) {
        button_positive.setText(text);
        button_positive.setOnCheckedChangeListener(listener);
        return button_positive;
    }

    public RadioButton setNegativeButton(int textId, 
            CompoundButton.OnCheckedChangeListener listener) {
        button_negative.setText(textId);
        button_negative.setOnCheckedChangeListener(listener);
        return button_negative;
    }
    
    public RadioButton setNegativeButton(CharSequence text, 
            CompoundButton.OnCheckedChangeListener listener) {
        button_negative.setText(text);
        button_negative.setOnCheckedChangeListener(listener);
        return button_negative;
    }

    public RadioButton addNeutralButton(int textId, 
            CompoundButton.OnCheckedChangeListener listener) {
        if (neutralIndex == INDEX_FIRST)
        {
            addDivider();
        }
        
        RadioButton button = (RadioButton) LayoutInflater.from(getContext())
                .inflate(R.layout.choose_button_neutral, this, false);
        button.setText(textId);
        button.setOnCheckedChangeListener(listener);
        addView(button, neutralIndex++);
        
        addDivider();
        
        return button;
    }

    public RadioButton addNeutralButton(CharSequence text, 
            CompoundButton.OnCheckedChangeListener listener) {
        if (neutralIndex == INDEX_FIRST)
        {
            addDivider();
        }
        
        RadioButton button = (RadioButton) LayoutInflater.from(getContext())
                .inflate(R.layout.choose_button_neutral, this, false);
        button.setText(text);
        button.setOnCheckedChangeListener(listener);
        addView(button, neutralIndex++);
        
        addDivider();
        
        return button;
    }
    
    private void addDivider() {
        View divider = new View(getContext());
        divider.setBackgroundResource(R.drawable.choose_button_neutral_checked_on);
        addView(divider, neutralIndex++, new LayoutParams(2, LayoutParams.MATCH_PARENT));
    }
    
    public void choosePositiveButton() {
        button_positive.setChecked(true);
    }
    
    public void chooseNegativeButton() {
        button_negative.setChecked(true);
    }
}