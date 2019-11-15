package engine.android.widget.component.input;

import engine.android.util.listener.MyTextWatcher;
import engine.android.widget.R;
import engine.android.widget.common.button.CountDownButton;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.method.DigitsKeyListener;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 输入框
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class InputBox extends FrameLayout {
    
    public static final int STYLE_MOBILE    = 1; // 手机号
    public static final int STYLE_PASSWORD  = 2; // 密码
    public static final int STYLE_PASSCODE  = 3; // 验证码

    /**
     * 密码输入限制
     */
    public static final DigitsKeyListener passwordKeyListener
    = DigitsKeyListener.getInstance("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ");

    public EditText input;
    public ImageView clear;
    public ImageView eye;
    public CountDownButton sms_code;
    private View divider;

    private InputAction action;

    public InputBox(Context context) {
        this(context, null);
    }

    public InputBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.input_box, this);

        input = (EditText) findViewById(R.id.input);
        clear = (ImageView) findViewById(R.id.clear);
        eye = (ImageView) findViewById(R.id.eye);
        sms_code = (CountDownButton) findViewById(R.id.sms_code);
        divider = findViewById(R.id.divider);
        action = new InputAction(input);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(d);
        divider.setVisibility(GONE);
    }

    public void setStyle(int style) {
        switch (style) {
            case STYLE_MOBILE:
                input.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                input.setFilters(new InputFilter[] { new LengthFilter(11) });
                break;
            case STYLE_PASSWORD:
                input.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                input.setFilters(new InputFilter[] { passwordKeyListener, new LengthFilter(20) });
                eye.setVisibility(VISIBLE);
                action.bindEye(eye);
                break;
            case STYLE_PASSCODE:
                input.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                input.setFilters(new InputFilter[] { new LengthFilter(4) });
                sms_code.setVisibility(VISIBLE);
                break;
        }
    }

    /**
     * 开启清空输入框功能
     */
    public void enableClear() {
        action.bindClear(clear);
    }

    public static class InputAction extends MyTextWatcher
    implements OnFocusChangeListener, OnClickListener {

        private final EditText input;

        private View clear;

        private View eye;
        private TransformationMethod method;

        public InputAction(EditText input) {
            this.input = input;
        }

        public InputAction bindClear(View clear) {
            input.setOnFocusChangeListener(this);
            input.addTextChangedListener(this);
            (this.clear = clear).setOnClickListener(this);
            return this;
        }

        public InputAction bindEye(View eye) {
            (this.eye = eye).setOnClickListener(this);
            return this;
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
            if (input.hasFocus()) clear.setVisibility(View.VISIBLE);
        }

        @Override
        protected void changeToEmpty(String before) {
            clear.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (v == clear)
            {
                input.setText(null);
            }
            else if (v == eye)
            {
                int start = input.getSelectionStart();
                int end = input.getSelectionEnd();

                boolean isSelected = v.isSelected();
                if (isSelected)
                {
                    input.setTransformationMethod(method);
                }
                else
                {
                    if ((method = input.getTransformationMethod()) == null)
                    {
                        method = PasswordTransformationMethod.getInstance();
                    }
                    
                    input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                v.setSelected(!isSelected);
                input.setSelection(start, end);
            }
        }
    }
}