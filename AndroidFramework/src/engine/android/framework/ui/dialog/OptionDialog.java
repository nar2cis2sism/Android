package engine.android.framework.ui.dialog;

import engine.android.framework.R;
import engine.android.framework.ui.BaseDialog;
import engine.android.util.AndroidUtil;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 通用选项对话框
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class OptionDialog extends BaseDialog implements OnClickListener {

    private LinearLayout root;

    private CharSequence[] options;
    private OnClickListener listener;

    public OptionDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
        
        setupView();
    }

    private void setupView() {
        root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(android.R.color.white);
        setContentView(root);
    }

    @Override
    protected void setupParams(WindowManager.LayoutParams params) {
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        params.windowAnimations = R.style.Animation_Dialog_Bottom;
    }

    public void setItems(int itemsId, OnClickListener listener) {
        setItems(getContext().getResources().getTextArray(itemsId), listener);
    }

    public void setItems(CharSequence[] items, OnClickListener listener) {
        options = items;
        this.listener = listener;
        build();
    }

    private void build() {
        root.removeAllViews();

        if (options != null)
        {
            for (int i = 0; i < options.length; i++)
            {
                addItem(i);
                addDivider();
            }
        }

        addCategory();
        addDivider();
        addCancel();
    }

    private void addItem(int index) {
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.option_dialog_item, root, false);
        tv.setText(options[index]);
        tv.setTag(index);
        tv.setOnClickListener(this);
        root.addView(tv);
    }

    private void addDivider() {
        View divider = new View(getContext());
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        root.addView(divider, LayoutParams.MATCH_PARENT, 1);
    }

    private void addCategory() {
        View divider = new View(getContext());
        divider.setBackgroundColor(Color.parseColor("#F5F5F5"));
        root.addView(divider, LayoutParams.MATCH_PARENT, AndroidUtil.dp2px(getContext(), 10));
    }

    private void addCancel() {
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.option_dialog_item, root, false);
        tv.setText("取消");
        tv.setTextColor(Color.parseColor("#666666"));
        tv.setOnClickListener(this);
        root.addView(tv);
    }

    @Override
    public void onClick(View v) {
        Integer index = (Integer) v.getTag();
        if (index == null)
        {
            // 取消
            cancel();
            return;
        }

        if (listener != null)
        {
            listener.onClick(this, index);
            dismiss();
        }
    }
}
