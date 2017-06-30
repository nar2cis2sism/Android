package engine.android.framework.ui.extra;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.R;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.AndroidUtil;
import engine.android.util.ui.UIUtil;

/**
 * 提供详情界面基本组件
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public abstract class BaseInfoFragment extends BaseFragment {
    
    protected static final int NO_TITLE = 0;
    
    protected static final View NO_TEXT = null;
    
    /**
     * @param titleId 标题
     * @param textId 文本
     * @param showArrow 是否显示右箭头
     */
    protected ViewHolder addComponent(ViewGroup root, LayoutInflater inflater, 
            int titleId, CharSequence text, boolean showArrow) {
        ViewHolder holder = addComponent(root, inflater, titleId, NO_TEXT, showArrow);
        
        if (!TextUtils.isEmpty(text))
        {
            holder.setTextView(R.id.text, text);
        }

        return holder;
    }
    
    /**
     * @param titleId 标题
     * @param replaceText 替换文本区域
     * @param showArrow 是否显示右箭头
     */
    protected ViewHolder addComponent(ViewGroup root, LayoutInflater inflater, 
            int titleId, View replaceText, boolean showArrow) {
        View component = inflater.inflate(R.layout.base_info_item, root, false);
        root.addView(component);
        
        ViewHolder holder = new ViewHolder(component);
        
        if (titleId != NO_TITLE)
        {
            holder.setTextView(R.id.title, titleId);
        }
        
        if (replaceText != null)
        {
            View text = holder.getView(R.id.text);
            UIUtil.replace(text, replaceText, text.getLayoutParams());
            holder.removeView(R.id.text);
        }
        
        if (!showArrow)
        {
            holder.setVisibility(R.id.arrow, View.INVISIBLE);
        }
        
        // Divider
        addDivider(root, getResources().getColor(R.color.divider_horizontal), 1);
        
        return holder;
    }
    
    protected void addDivider(ViewGroup root, int color, int height) {
        View divider = new View(getContext());
        divider.setBackgroundColor(color);
        root.addView(divider, new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT, height));
    }
    
    protected void addCategory(ViewGroup root) {
        addDivider(root, getResources().getColor(R.color.divider_category), 
                AndroidUtil.dp2px(getContext(), 10));
    }
}