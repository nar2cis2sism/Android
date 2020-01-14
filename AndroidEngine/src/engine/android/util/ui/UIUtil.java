package engine.android.util.ui;

import engine.android.util.manager.MyKeyboardManager;
import engine.android.util.manager.MyKeyboardManager.KeyboardListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * UI工具库
 * 
 * @author Daimon
 * @since 2/2/2015
 */
public final class UIUtil {

    /**
     * 修正列表的高度（与ScrollView不兼容的bug）
     */
    public static void modifyListViewHeight(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null)
        {
            return;
        }

        int height = listView.getPaddingTop() + listView.getPaddingBottom();
        int size = adapter.getCount();
        if (size == 0)
        {
            listView.getLayoutParams().height = height;
            return;
        }

        View listItem;
        for (int i = 0; i < size; i++)
        {
            listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            height += listItem.getMeasuredHeight();
        }

        listView.getLayoutParams().height = height + listView.getDividerHeight() * (size - 1);
    }

    /**
     * 修正文本显示（换行不均匀的bug）
     */
    public static void modifyTextView(TextView tv, String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = text.length(); i < len; i++)
        {
            String s = text.substring(i, i + 1);
            sb.append(s);
            if (s.matches("\\p{Punct}"))
            {
                sb.append(" ");
            }
        }

        tv.setText(sb.toString());
    }

    /**
     * 生成表格行
     * 
     * @param text 第一列值
     * @param value 第三列值
     * @param width 第二列空行宽度
     */
    public static TableRow createTableRow(Context context, String text, String value, int width) {
        TableRow tr = new TableRow(context);

        TextView tv = new TextView(context);
        tv.setText(text);
        tr.addView(tv);

        tv = new TextView(context);
        tv.setWidth(width);
        tr.addView(tv);

        tv = new TextView(context);
        tv.setText(value);
        tr.addView(tv);

        return tr;
    }

    /**
     * 获取该Activity所有view
     */
    public static List<View> getAllViews(Activity a) {
        return getAllViews(a.getWindow().getDecorView());
    }
    
    public static List<View> getAllViews(View root) {
        List<View> list = new LinkedList<View>();
        addAllViews(root, list);
        return list;
    }

    private static void addAllViews(View view, List<View> list) {
        list.add(view);
        if (view instanceof ViewGroup)
        {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++)
            {
                addAllViews(group.getChildAt(i), list);
            }
        }
    }

    /**
     * 替换控件（控件ID将会保持）
     * 
     * @return 替换后的控件
     */
    public static View replace(View src, int resId) {
        final ViewParent viewParent = src.getParent();
        if (viewParent instanceof ViewGroup)
        {
            final ViewGroup parent = (ViewGroup) viewParent;
            final View view = LayoutInflater.from(src.getContext()).inflate(resId, parent, false);
            view.setId(src.getId());

            final int index = parent.indexOfChild(src);
            parent.removeViewInLayout(src);
            parent.addView(view, index);

            return view;
        }

        return null;
    }

    /**
     * 替换控件（控件ID将会保持）
     */
    public static boolean replace(View src, View des, LayoutParams params) {
        final ViewParent viewParent = src.getParent();
        if (viewParent instanceof ViewGroup)
        {
            final ViewGroup parent = (ViewGroup) viewParent;
            final View view = des;
            view.setId(src.getId());

            final int index = parent.indexOfChild(src);
            parent.removeViewInLayout(src);

            if (params != null)
            {
                parent.addView(view, index, params);
            }
            else
            {
                parent.addView(view, index);
            }

            return true;
        }

        return false;
    }
    
    /**
     * 有文本时显示/无文本时隐藏控件
     */
    public static void setTextVisible(TextView view, CharSequence text) {
        if (TextUtils.isEmpty(text))
        {
            view.setVisibility(View.GONE);
        }
        else
        {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        }
    }
    
    /**
     * 显示软键盘
     */
    public static void showSoftInput(final View view, long delay) {
        final InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (delay <= 0)
        {
            imm.showSoftInput(view, 0);
        }
        else
        {
            view.postDelayed(new Runnable() {

                @Override
                public void run() {
                    imm.showSoftInput(view, 0);
                }
            }, delay);
        }
    }
    
    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    /**
     * 软键盘弹出时自适应窗口尺寸
     * 
     * @param anchor 一般取第一个输入框
     */
    public static void adjustResize(ScrollView scrollView, View anchor) {
        new AdjustResize(scrollView, anchor).adjustResize();
    }
    
    private static class AdjustResize implements KeyboardListener, Runnable, OnClickListener {
        
        private final ScrollView scrollView;
        private final View anchor;
        
        private int scrollY;
        
        public AdjustResize(ScrollView scrollView, View anchor) {
            this.scrollView = scrollView;
            this.anchor = anchor;
        }
        
        public void adjustResize() {
            ((Activity) scrollView.getContext()).getWindow()
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            new MyKeyboardManager(scrollView).setKeyboardListener(this);
            scrollView.getChildAt(0).setOnClickListener(this);
        }

        @Override
        public void keyboardChanged(boolean isKeyboardShown, int keyBoardHeight) {
            if (isKeyboardShown)
            {
                scrollView.post(this);
            }
        }

        @Override
        public void run() {
            if (scrollY == 0)
            {
                int[] location = new int[2];
                anchor.getLocationOnScreen(location);
                scrollY = location[1];
            }
            
            scrollView.scrollTo(0, scrollY);
        }

        @Override
        public void onClick(View v) {
            hideSoftInput(v);
        }
    }
    
    public static PopupWindow createPopupWindow(View content) {
        PopupWindow popup = new PopupWindow(content, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // 点击 PopupWindow之外的区域后， PopupWindow会消失
        popup.setBackgroundDrawable(new ColorDrawable());
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);
        
        return popup;
    }
    
    /**
     * 控件不可操作时半透明
     */
    public static void setupAlpha(View view) {
        if (view.isEnabled())
        {
            view.setAlpha(1);
        }
        else
        {
            view.setAlpha(0.5f);
        }
    }

    /**
     * 控件不可操作时置灰
     */
    public static void setupGray(ImageView view) {
        if (view.isEnabled())
        {
            view.clearColorFilter();
        }
        else
        {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            view.setColorFilter(new ColorMatrixColorFilter(cm));
        }
    }

    /**
     * 判断是否在列表底部
     */
    public static boolean atBottom(AbsListView list) {
        return list.getLastVisiblePosition() >= list.getCount() - 1;
    }

    /**
     * 获取圆角图片
     */
    public static Drawable getRoundDrawable(int color, float radius) {
        PaintDrawable drawable = new PaintDrawable(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * 滑动到列表底部（当{@link ListView#TRANSCRIPT_MODE_ALWAYS_SCROLL}无效时）
     */
    public static void scrollToBottom(final AbsListView list) {
        list.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.smoothScrollToPositionFromTop(list.getCount(), 0);
            }
        }, 80);
    }
}