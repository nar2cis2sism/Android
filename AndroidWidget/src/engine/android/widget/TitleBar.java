package engine.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 标题栏
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class TitleBar extends RelativeLayout {

    private ImageView navigation_up;

    private TextView title;

    private FrameLayout content;

    private LinearLayout actions;
    
    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        navigation_up = (ImageView) findViewById(R.id.navigation_up);
        title = (TextView) findViewById(R.id.title);
        content = (FrameLayout) findViewById(R.id.content);
        actions = (LinearLayout) findViewById(R.id.actions);
    }
    
    public TitleBar setUpIndicator(Drawable drawable) {
        navigation_up.setImageDrawable(drawable);
        return this;
    }
    
    public TitleBar setUpIndicator(int resId) {
        navigation_up.setImageResource(resId);
        return this;
    }
    
    public TitleBar setDisplayUpEnabled(boolean showUp) {
        navigation_up.setVisibility(showUp ? VISIBLE : GONE);
        return this;
    }

    public TitleBar setTitle(CharSequence text) {
        title.setText(text);
        return this;
    }

    public TitleBar setTitle(int resId) {
        title.setText(resId);
        return this;
    }

    public TitleBar setDisplayShowTitleEnabled(boolean showTitle) {
        title.setVisibility(showTitle ? VISIBLE : GONE);
        return this;
    }
    
    public TitleBar setCustomView(View view) {
        content.removeAllViews();
        content.addView(view);
        return this;
    }
    
    public TitleBar setCustomView(View view, FrameLayout.LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        return setCustomView(view);
    }

    public TitleBar setCustomView(int resId) {
        return setCustomView(LayoutInflater.from(getContext()).inflate(resId, content, false));
    }
    
    public TitleBar setDisplayShowCustomEnabled(boolean showCustom) {
        content.setVisibility(showCustom ? VISIBLE : GONE);
        return this;
    }
    
    public TitleBar addAction(int iconId) {
        return addAction(iconId, null);
    }
    
    public TitleBar addAction(int iconId, OnClickListener listener) {
        ImageView iv = new ImageView(getContext());
        iv.setImageResource(iconId);
        if (listener != null) iv.setOnClickListener(listener);
        
        return addAction(iv);
    }
    
    public TitleBar addAction(CharSequence text) {
        return addAction(text, null);
    }
    
    public TitleBar addAction(CharSequence text, OnClickListener listener) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        if (listener != null) tv.setOnClickListener(listener);
        
        return addAction(tv);
    }
    
    public TitleBar addAction(View action) {
        actions.addView(action);
        return this;
    }
    
    public void show() {
        setVisibility(VISIBLE);
    }
    
    public void hide() {
        setVisibility(GONE);
    }
    
    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }
    
    /**
     * 恢复默认状态
     */
    public TitleBar reset() {
        if (isShowing())
        {
            navigation_up.setImageResource(R.drawable.navigation_up);
            setDisplayUpEnabled(false);
            setTitle(null);
            setDisplayShowTitleEnabled(true);
            setDisplayShowCustomEnabled(false);
            actions.removeAllViews();
            hide();
        }
        
        return this;
    }
    
    public CharSequence getTitle() {
        return title.getText();
    }
}