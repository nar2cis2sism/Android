package engine.android.util.ui;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;

/**
 * 上下文模式（列表长按事件）
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class ContextualMode implements MultiChoiceModeListener {

    protected final AbsListView listView;

    private final int menuResource;

    protected ActionMode actionMode;

    public ContextualMode(AbsListView listView, int menuResource) {
        this.listView = listView;
        this.menuResource = menuResource;

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (actionMode != null) return false;
        (actionMode = mode).getMenuInflater().inflate(menuResource, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return update(mode);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
            long id, boolean checked) {
        update(mode);
    }

    public final void finish() {
        if (actionMode != null)
        {
            actionMode.finish();
            actionMode = null;
        }
    }

    protected boolean update(ActionMode mode) {
        if (listView != null)
        {
            mode.setTitle(String.valueOf(listView.getCheckedItemCount()));
            return true;
        }

        return false;
    }
}