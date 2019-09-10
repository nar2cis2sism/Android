package engine.android.widget.component.input;

import engine.android.util.AndroidUtil;
import engine.android.util.ui.MyPasswordTransformationMethod;
import engine.android.util.ui.UIUtil;
import engine.android.widget.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 密码支付输入框
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class PasswordInputBox extends FrameLayout implements TextWatcher {

    private int count = 6;
    private int dividerWidth;

    private EditText input;
    private LinearLayout content;

    private View[] itemViews;

    private OnInputListener listener;

    public PasswordInputBox(Context context) {
        this(context, null);
    }

    public PasswordInputBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        dividerWidth = AndroidUtil.dp2px(context, 1);
        setBackgroundResource(R.drawable.password_input_bg);
        LayoutInflater.from(context).inflate(R.layout.password_input_box, this);
    }

    public String getPassword() {
        return input.getText().toString();
    }

    public void clearPassword() {
        input.setText(null);
        for (View view : itemViews)
        {
            view.setVisibility(INVISIBLE);
        }
    }

    public void setCount(int count) {
        this.count = count;
        input.setFilters(new InputFilter[] {new LengthFilter(count)});
        setupContent(content);
    }

    public void setListener(OnInputListener listener) {
        this.listener = listener;
    }

    public void showSoftInput() {
        requestFocus();
        UIUtil.showSoftInput(input, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        input = (EditText) findViewById(R.id.input);
        content = (LinearLayout) findViewById(R.id.content);
        setupInput(input);
        setCount(count);
    }

    private void setupInput(EditText input) {
        input.setTransformationMethod(new MyPasswordTransformationMethod((char) 0));
        input.addTextChangedListener(this);
    }

    private void setupContent(LinearLayout content) {
        content.removeAllViews();
        itemViews = new View[count];

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        for (int i = 0; i < count; i++)
        {
            if (i > 0)
            {
                View divider = new View(getContext());
                divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
                content.addView(divider, dividerWidth, LayoutParams.MATCH_PARENT);
            }

            TextView text = new TextView(getContext());
            text.setVisibility(INVISIBLE);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(Color.parseColor("#333333"));
            text.setText("●");

            content.addView(itemViews[i] = text, params);
        }
    }

    private void updatePassword(int length) {
        if (length > 0)
        {
            itemViews[length - 1].setVisibility(VISIBLE);
        }

        if (length < count)
        {
            itemViews[length].setVisibility(INVISIBLE);
        }
        else if (length == count)
        {
            input.clearFocus();
            UIUtil.hideSoftInput(input);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }

        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        }

        if (width != 0 || height != 0)
        {
            if (width == 0)
            {
                width = height * count + dividerWidth * (count - 1) + getPaddingLeft() + getPaddingRight();
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            }
            else if (height == 0)
            {
                height = ((width - getPaddingLeft() - getPaddingRight()) - dividerWidth * (count - 1)) / count;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        updatePassword(s.length());
        if (listener != null)
        {
            listener.input(s.toString());
        }
    }

    public interface OnInputListener {

        /**
         * @param password 输入密码
         */
        void input(String password);
    }
}