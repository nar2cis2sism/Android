package engine.android.util.ui;

import android.widget.CompoundButton;

import java.util.LinkedList;

/**
 * 只允许选中一个按钮的管理组件
 * 
 * @author Daimon
 * @version N
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
        r.setOnCheckedChangeListener(new CheckedStateTracker(group.size()));
        group.add(r);
        if (r.isChecked())
        {
            r.setChecked(true);
        }
    }
    
    public void check(int index) {
        if (index == -1)
        {
            checked.setChecked(false);
            checked = null;
        }
        else if (index >= 0 && index < group.size())
        {
            group.get(index).setChecked(true);
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
        
        public void onCheckedChanged(ButtonGroup group, int checkedIndex);
    }
    
    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {

        private final int checkedIndex;
        
        public CheckedStateTracker(int checkedIndex) {
            this.checkedIndex = checkedIndex;
        }
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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