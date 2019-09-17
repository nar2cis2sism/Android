package engine.android.framework.ui.dialog;

import engine.android.framework.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 通用消息对话框
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class MessageDialog extends Dialog implements View.OnClickListener {

    public TextView message;

    public Button positive;
    public Button negative;
    private View divider;

    private OnClickListener positive_listener;
    private OnClickListener negative_listener;

    public MessageDialog(Context context) {
        super(context, R.style.Theme_Dialog);
        setCancelable(false);

        setupView();
    }

    private void setupView() {
        setContentView(R.layout.message_dialog);
        message = (TextView) findViewById(R.id.message);
        positive = (Button) findViewById(R.id.positive);
        negative = (Button) findViewById(R.id.negative);
        divider = findViewById(R.id.divider);

        positive.setOnClickListener(this);
        negative.setOnClickListener(this);
    }

    public MessageDialog setMessage(int messageId) {
        message.setText(messageId);
        return this;
    }

    public MessageDialog setMessage(CharSequence message) {
        this.message.setText(message);
        return this;
    }

    public MessageDialog setPositiveButton(int textId, OnClickListener listener) {
        positive.setText(textId);
        positive_listener = listener;
        return this;
    }

    public MessageDialog setPositiveButton(CharSequence text, OnClickListener listener) {
        positive.setText(text);
        positive_listener = listener;
        return this;
    }

    public MessageDialog setNegativeButton(int textId, OnClickListener listener) {
        negative.setText(textId);
        negative_listener = listener;
        negative.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        return this;
    }

    public MessageDialog setNegativeButton(CharSequence text, OnClickListener listener) {
        negative.setText(text);
        negative_listener = listener;
        negative.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v == positive)
        {
            if (positive_listener != null)
            {
                positive_listener.onClick(this, BUTTON_POSITIVE);
            }
        }
        else if (v == negative)
        {
            if (negative_listener != null)
            {
                negative_listener.onClick(this, BUTTON_NEGATIVE);
            }
        }

        dismiss();
    }
}