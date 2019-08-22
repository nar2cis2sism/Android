package engine.android.util.ui;

import android.widget.CompoundButton;

import java.util.LinkedList;

/**
 * 只允许选中一个按钮的管理组件
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public class ButtonGroup {

    private final LinkedList<CompoundButton> group
    = new LinkedList<CompoundButton>();                     // 存储所有的单选按钮

    private CompoundButton checked;                         // 选中的按钮
    
    private OnCheckedChangeListener mOnCheckedChangeListener;

    /**
     * 添加需要管理的按钮（支持单选和复选）
     */
    public void add(CompoundButton r) {
        add(r, null);
    }

    /**
     * 添加需要管理的按钮（支持单选和复选）
     */
    public void add(CompoundButton r, CompoundButton.OnCheckedChangeListener listener) {
        listener = new CheckedStateTracker(group.size(), listener);
        r.setOnCheckedChangeListener(listener);
        group.add(r);
        if (r.isChecked())
        {
            listener.onCheckedChanged(r, true);
        }
    }
    
    public void check(int index) {
        if (index >= 0 && index < group.size())
        {
            group.get(index).setChecked(true);
        }
        else if (checked != null)
        {
            checked.setChecked(false);
            checked = null;
        }
    }

    /**
     * 获取选中的按钮
     */
    public CompoundButton getCheckedButton() {
        return checked;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        
        void onCheckedChanged(ButtonGroup group, int checkedIndex);
    }
    
    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {

        private final int checkedIndex;
        
        private final CompoundButton.OnCheckedChangeListener listener;
        
        public CheckedStateTracker(int checkedIndex, CompoundButton.OnCheckedChangeListener listener) {
            this.checkedIndex = checkedIndex;
            this.listener = listener;
        }
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (listener != null)
            {
                listener.onCheckedChanged(buttonView, isChecked);
            }
            
            if (isChecked)
            {
                if (checked != null)
                {
                    checked.setChecked(false);
                }

                checked = buttonView;
                
                if (mOnCheckedChangeListener != null)
                {
                    mOnCheckedChangeListener.onCheckedChanged(ButtonGroup.this, checkedIndex);
                }
            }
        }
    }
}