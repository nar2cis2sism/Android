package engine.android.util.ui;

import android.view.View;
import android.widget.ListView;

import java.util.LinkedList;

/**
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class ListViewState {
    
    private static class SavedState {
        
        public int position;
        
        public int top;
    }

    private final ListView listView;
    
    private final LinkedList<SavedState> stack = new LinkedList<SavedState>();
    
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
        listView.setSelectionFromTop(state.position, state.top);
    }
}