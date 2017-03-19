package engine.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 提供一个动态添加操作按钮的布局
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class ActionContainer extends LinearLayout {
    
    private LinearLayout.LayoutParams params;

    public ActionContainer(Context context) {
        super(context);
        init(context);
    }

    public ActionContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        setGravity(Gravity.CENTER_VERTICAL);
        params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
    }
    
    public void addAction(View action) {
        addView(action, params);
    }
    
    public TextView addAction(int iconRes, int textRes) {
        TextView action = new TextView(getContext());
        if (iconRes != 0) action.setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0);
        if (textRes != 0) action.setText(textRes);
        action.setGravity(Gravity.CENTER_HORIZONTAL);
        
        addView(action, params);
        return action;
    }
    
    public TextView addAction(Drawable icon, CharSequence text) {
        TextView action = new TextView(getContext());
        if (icon != null) action.setCompoundDrawables(null, icon, null, null);
        if (!TextUtils.isEmpty(text)) action.setText(text);
        action.setGravity(Gravity.CENTER_HORIZONTAL);
        
        addView(action, params);
        return action;
    }
}