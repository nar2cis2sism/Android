package demo.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public class BadgeView extends TextView {

    private static final int DEFAULT_MARGIN_DIP = 5;
    private static final int DEFAULT_LR_PADDING_DIP = 5;
    private static final int DEFAULT_CORNER_RADIUS_DIP = 8;
    private static final int DEFAULT_POSITION = Gravity.RIGHT | Gravity.TOP;
    private static final int DEFAULT_BADGE_COLOR = Color.parseColor("#CCFF0000");
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;

    private static Animation fadeIn;
    private static Animation fadeOut;

    private View target;

    private int badgePosition;
    private MarginLayoutParams badgeParams;
    private int badgeColor;
    private ShapeDrawable badgeDrawable;

    public BadgeView(Context context) {
        super(context);
        init(null);
    }

    public BadgeView(Context context, View target) {
        super(context);
        init(target);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(null);
    }

    private void init(View target) {
        this.target = target;

        // apply defaults
        badgePosition = DEFAULT_POSITION;
        badgeParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        badgeParams.rightMargin = badgeParams.topMargin = dp2px(DEFAULT_MARGIN_DIP);
        badgeColor = DEFAULT_BADGE_COLOR;

        setTypeface(Typeface.DEFAULT_BOLD);
        int paddingPixels = dp2px(DEFAULT_LR_PADDING_DIP);
        setPadding(paddingPixels, 0, paddingPixels, 0);
        setTextColor(DEFAULT_TEXT_COLOR);

        fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);

        if (target != null)
        {
            applyTo(target);
        }
        else
        {
            show();
        }
    }

    private void applyTo(View target) {
        FrameLayout layout = new FrameLayout(getContext());
        ViewParent parent = target.getParent();
        if (parent instanceof TabWidget)
        {
            // 如果是自定义TabWidget可能会有问题
            ((ViewGroup) target).addView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            layout.addView(this);
            setVisibility(View.GONE);
        }
        else if (parent instanceof ViewGroup)
        {
            ViewGroup group = (ViewGroup) parent;
            int index = group.indexOfChild(target);
            group.removeViewAt(index);

            group.addView(layout, index, target.getLayoutParams());

            layout.addView(target);
            layout.addView(this);
            setVisibility(View.GONE);
        }
    }

    /**
     * Make the badge visible in the UI.
     */

    public void show() {
        show(null);
    }

    /**
     * Make the badge visible in the UI.
     * 
     * @param animate flag to apply the default fade-in animation.
     */

    public void show(boolean animate) {
        show(animate ? fadeIn : null);
    }

    /**
     * Make the badge visible in the UI.
     * 
     * @param anim Animation to apply to the view when made visible.
     */

    @SuppressWarnings("deprecation")
    public void show(Animation anim) {
        if (getBackground() == null)
        {
            if (badgeDrawable == null)
            {
                badgeDrawable = getDefaultBackground();
            }

            setBackgroundDrawable(badgeDrawable);
        }

        applyLayoutParams();

        if (anim != null)
        {
            startAnimation(anim);
        }

        setVisibility(View.VISIBLE);
    }

    private void applyLayoutParams() {
        if (target == null)
        {
            return;
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(badgeParams);
        params.gravity = badgePosition;
        setLayoutParams(params);
    }

    /**
     * Make the badge non-visible in the UI.
     */

    public void hide() {
        hide(null);
    }

    /**
     * Make the badge non-visible in the UI.
     * 
     * @param animate flag to apply the default fade-out animation.
     */

    public void hide(boolean animate) {
        hide(animate ? fadeOut : null);
    }

    /**
     * Make the badge non-visible in the UI.
     * 
     * @param anim Animation to apply to the view when made non-visible.
     */

    public void hide(Animation anim) {
        setVisibility(View.GONE);

        if (anim != null)
        {
            startAnimation(anim);
        }
    }

    /**
     * Toggle the badge visibility in the UI.
     */

    public void toggle() {
        toggle(null, null);
    }

    /**
     * Toggle the badge visibility in the UI.
     * 
     * @param animate flag to apply the default fade-in/out animation.
     */

    public void toggle(boolean animate) {
        if (animate)
        {
            toggle(fadeIn, fadeOut);
        }
        else
        {
            toggle(null, null);
        }
    }

    /**
     * Toggle the badge visibility in the UI.
     * 
     * @param animIn Animation to apply to the view when made visible.
     * @param animOut Animation to apply to the view when made non-visible.
     */

    public void toggle(Animation animIn, Animation animOut) {
        if (isShown())
        {
            hide(animOut);
        }
        else
        {
            show(animIn);
        }
    }

    /**
     * Increase the numeric badge label. If the current badge label cannot be
     * converted to an integer value, its label will be set to "0".
     * 
     * @param offset the increment offset.
     * @return the updated value
     */

    public int incrementAndGet(int offset) {
        int value = 0;
        CharSequence text = getText();
        if (!TextUtils.isEmpty(text))
        {
            try {
                value = Integer.parseInt(text.toString());
            } catch (NumberFormatException e) {}
        }

        setText(String.valueOf(value += offset));
        return value;
    }

    /**
     * Decrement the numeric badge label. If the current badge label cannot be
     * converted to an integer value, its label will be set to "0".
     * 
     * @param offset the decrement offset.
     * @return the updated value
     */

    public int decrementAndGet(int offset) {
        return incrementAndGet(-offset);
    }

    /**
     * Returns the target View this badge has been attached to.
     */
    public View getTarget() {
        return target;
    }

    /**
     * Set the position of this badge.
     */

    public void setBadgePosition(int layout_gravity) {
        badgePosition = layout_gravity;
        badgeParams.setMargins(0, 0, 0, 0);
    }

    /**
     * Set the margins in pixels from the target View that is applied to this
     * badge.
     * 
     * @param left the left margin size
     * @param top the top margin size
     * @param right the right margin size
     * @param bottom the bottom margin size
     */

    public void setBadgeMargin(int left, int top, int right, int bottom) {
        badgeParams.setMargins(left, top, right, bottom);
    }

    /**
     * Set the color value of the badge background.
     * 
     * @param badgeColor the badge background color.
     */

    public void setBadgeColor(int badgeColor) {
        this.badgeColor = badgeColor;
        if (badgeDrawable != null)
        {
            badgeDrawable.getPaint().setColor(badgeColor);
        }
        else
        {
            badgeDrawable = getDefaultBackground();
        }
    }

    private ShapeDrawable getDefaultBackground() {
        int r = dp2px(DEFAULT_CORNER_RADIUS_DIP);
        ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(
                new float[] { r, r, r, r, r, r, r, r }, null, null));
        drawable.getPaint().setColor(badgeColor);
        return drawable;
    }

    /**
     * 像素转换
     */

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}