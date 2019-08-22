package engine.android.util.ui;

import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;

import java.util.LinkedList;

/**
 * @author Daimon
 * @since 3/26/2012
 */
public class ListViewState {
    
    private static class SavedState {
        
        public int position;
        
        public int top;
    }

    private final ListView listView;
    
    private final LinkedList<SavedState> stack = new LinkedList<SavedState>();
    
    private final SparseArray<SavedState> map = new SparseArray<SavedState>();
    
    public ListViewState(ListView listView) {
        this.listView = listView;
    }
    
    /**
     * 记录并保存当前列表的状态
     */
    public void save() {
        SavedState state = new SavedState();
        state.position = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(0);
        if (view != null) state.top = view.getTop();
        stack.push(state);
    }
    
    /**
     * 恢复列表之前的状态
     */
    public void restore() {
        SavedState state = stack.pop();
        if (state != null) listView.setSelectionFromTop(state.position, state.top);
    }
    
    /**
     * 记录并保存指定列表的状态
     */
    public void save(int index) {
        SavedState state = new SavedState();
        state.position = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(0);
        if (view != null) state.top = view.getTop();
        map.append(index, state);
    }
    
    /**
     * 恢复指定列表之前的状态
     */
    public void restore(int index) {
        SavedState state = map.get(index);
        if (state == null) state = new SavedState();
        listView.setSelectionFromTop(state.position, state.top);
    }
    
    /**
     * 清除保存的状态
     */
    public void clear() {
        stack.clear();
        map.clear();
    }
}