package engine.android.widget.extra;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * 禁止复制粘贴
 * 
 * @author Daimon
 * @since 5/7/2014
 */
public class NoEditText extends EditText implements Callback {

    public NoEditText(Context context) {
        this(context, null);
    }

    public NoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLongClickable(false);
        setTextIsSelectable(false);
        setCustomSelectionActionModeCallback(this);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}
}