package demo.activity.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

public class TestOnMethod extends TestOnBase {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate");
        super.onCreate(savedInstanceState);
        
        final LinearLayout layout = new MyViewGroup(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        final MyView view = new MyView(this);
        
        Button button1 = new Button(this);
        button1.setText("layout.invalidate");
        button1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                layout.invalidate();
            }
        });
        layout.addView(button1);
        
        Button button2 = new Button(this);
        button2.setText("layout.requestLayout");
        button2.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                layout.requestLayout();
            }
        });
        layout.addView(button2);
        
        button1 = new Button(this);
        button1.setText("view.invalidate");
        button1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                view.invalidate();
            }
        });
        layout.addView(button1);
        
        button2 = new Button(this);
        button2.setText("view.requestLayout");
        button2.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                view.requestLayout();
            }
        });
        layout.addView(button2);
        
        layout.addView(view);

        log("setContentView");
        setContentView(layout);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult-resultCode:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        log("onApplyThemeResource-first:" + first);
        super.onApplyThemeResource(theme, resid, first);
    }
    
    @Override
    public void onAttachedToWindow() {
        log("onAttachedToWindow");
        super.onAttachedToWindow();
    }
    
    @Override
    public void onBackPressed() {
        log("onBackPressed");
        super.onBackPressed();
    }
    
    @Override
    protected void onChildTitleChanged(Activity childActivity,
            CharSequence title) {
        log("onChildTitleChanged-title:" + title);
        super.onChildTitleChanged(childActivity, title);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        log("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onContentChanged() {
        log("onContentChanged");
        super.onContentChanged();
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        log("onContextItemSelected");
        return super.onContextItemSelected(item);
    }
    
    @Override
    public void onContextMenuClosed(Menu menu) {
        log("onContextMenuClosed");
        super.onContextMenuClosed(menu);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        log("onCreateContextMenu");
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    @Override
    public CharSequence onCreateDescription() {
        log("onCreateDescription");
        return super.onCreateDescription();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        log("onCreateDialog");
        return super.onCreateDialog(id);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        log("onCreatePanelMenu");
        return super.onCreatePanelMenu(featureId, menu);
    }
    
    @Override
    public View onCreatePanelView(int featureId) {
        log("onCreatePanelView");
        return super.onCreatePanelView(featureId);
    }
    
    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        log("onCreateThumbnail");
        return super.onCreateThumbnail(outBitmap, canvas);
    }
    
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        log("onCreateView-name:" + name);
        return super.onCreateView(name, context, attrs);
    }
    
    @Override
    protected void onDestroy() {
        log("onDestroy");
        super.onDestroy();
    }
    
    @Override
    public void onDetachedFromWindow() {
        log("onDetachedFromWindow");
        super.onDetachedFromWindow();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        log("onKeyDown-keyCode:" + keyCode);
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        log("onKeyLongPress-keyCode:" + keyCode);
        return super.onKeyLongPress(keyCode, event);
    }
    
    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        log("onKeyMultiple-keyCode:" + keyCode + "-repeatCount:" + repeatCount);
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        log("onKeyUp-keyCode:" + keyCode);
        return super.onKeyUp(keyCode, event);
    }
    
    @Override
    public void onLowMemory() {
        log("onLowMemory");
        super.onLowMemory();
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        log("onMenuItemSelected");
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        log("onMenuOpened");
        return super.onMenuOpened(featureId, menu);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        log("onNewIntent");
        super.onNewIntent(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        log("onOptionsMenuClosed");
        super.onOptionsMenuClosed(menu);
    }
    
    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        log("onPanelClosed");
        super.onPanelClosed(featureId, menu);
    }
    
    @Override
    protected void onPause() {
        log("onPause");
        super.onPause();
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        log("onPostCreate");
        super.onPostCreate(savedInstanceState);
    }
    
    @Override
    protected void onPostResume() {
        log("onPostResume");
        super.onPostResume();
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        log("onPrepareDialog-id:" + id);
        super.onPrepareDialog(id, dialog);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        log("onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        log("onPreparePanel");
        return super.onPreparePanel(featureId, view, menu);
    }
    
    @Override
    protected void onRestart() {
        log("onRestart");
        super.onRestart();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        log("onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onResume() {
        log("onResume");
        super.onResume();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        log("onRetainNonConfigurationInstance");
        return super.onRetainNonConfigurationInstance();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onSearchRequested() {
        log("onSearchRequested");
        return super.onSearchRequested();
    }
    
    @Override
    protected void onStart() {
        log("onStart");
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        log("onStop");
        super.onStop();
    }
    
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        log("onTitleChanged-title:" + title);
        super.onTitleChanged(title, color);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        log("onTouchEvent");
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        log("onTrackballEvent");
        return super.onTrackballEvent(event);
    }
    
    @Override
    public void onUserInteraction() {
        log("onUserInteraction");
        super.onUserInteraction();
    }
    
    @Override
    protected void onUserLeaveHint() {
        log("onUserLeaveHint");
        super.onUserLeaveHint();
    }
    
    @Override
    public void onWindowAttributesChanged(LayoutParams params) {
        log("onWindowAttributesChanged");
        super.onWindowAttributesChanged(params);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        log("onWindowFocusChanged-hasFocus:" + hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
    protected void attachBaseContext(Context newBase) {
        log("attachBaseContext");
        super.attachBaseContext(newBase);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        log("dispatchKeyEvent");
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        log("dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        log("dispatchTrackballEvent");
        return super.dispatchTrackballEvent(ev);
    }
    
    @Override
    protected void log(String content) {
        super.log("(Activity)" + content);
    }
    
    private class MyViewGroup extends LinearLayout {

        public MyViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            log("onLayout-changed:" + changed);
            super.onLayout(changed, l, t, r, b);
        }
        
        @Override
        protected void onAnimationEnd() {
            log("onAnimationEnd");
            super.onAnimationEnd();
        }
        
        @Override
        protected void onAnimationStart() {
            log("onAnimationStart");
            super.onAnimationStart();
        }
        
        @Override
        protected void onAttachedToWindow() {
            log("onAttachedToWindow");
            super.onAttachedToWindow();
        }
        
        @Override
        public boolean onCheckIsTextEditor() {
            log("onCheckIsTextEditor");
            return super.onCheckIsTextEditor();
        }
        
        @Override
        protected void onCreateContextMenu(ContextMenu menu) {
            log("onCreateContextMenu");
            super.onCreateContextMenu(menu);
        }
        
        @Override
        protected int[] onCreateDrawableState(int extraSpace) {
            log("onCreateDrawableState");
            return super.onCreateDrawableState(extraSpace);
        }
        
        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            log("onCreateInputConnection");
            return super.onCreateInputConnection(outAttrs);
        }
        
        @Override
        protected void onDetachedFromWindow() {
            log("onDetachedFromWindow");
            super.onDetachedFromWindow();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            log("onDraw");
            super.onDraw(canvas);
        }
        
        @Override
        protected void onFinishInflate() {
            log("onFinishInflate");
            super.onFinishInflate();
        }
        
        @Override
        public void onFinishTemporaryDetach() {
            log("onFinishTemporaryDetach");
            super.onFinishTemporaryDetach();
        }
        
        @Override
        protected void onFocusChanged(boolean gainFocus, int direction,
                Rect previouslyFocusedRect) {
            log("onFocusChanged-gainFocus:" + gainFocus);
            super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        }
        
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            log("onInterceptTouchEvent");
            return super.onInterceptTouchEvent(ev);
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            log("onKeyDown-keyCode:" + keyCode);
            return super.onKeyDown(keyCode, event);
        }
        
        @Override
        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            log("onKeyLongPress-keyCode:" + keyCode);
            return super.onKeyLongPress(keyCode, event);
        }
        
        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount,
                KeyEvent event) {
            log("onKeyMultiple-keyCode:" + keyCode);
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
        
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            log("onKeyPreIme-keyCode:" + keyCode);
            return super.onKeyPreIme(keyCode, event);
        }
        
        @Override
        public boolean onKeyShortcut(int keyCode, KeyEvent event) {
            log("onKeyShortcut-keyCode:" + keyCode);
            return super.onKeyShortcut(keyCode, event);
        }
        
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            log("onKeyUp-keyCode:" + keyCode);
            return super.onKeyUp(keyCode, event);
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            log("onMeasure");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        
        @Override
        protected boolean onRequestFocusInDescendants(int direction,
                Rect previouslyFocusedRect) {
            log("onRequestFocusInDescendants");
            return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
        }
        
        @Override
        protected void onRestoreInstanceState(Parcelable state) {
            log("onRestoreInstanceState");
            super.onRestoreInstanceState(state);
        }
        
        @Override
        protected Parcelable onSaveInstanceState() {
            log("onSaveInstanceState");
            return super.onSaveInstanceState();
        }
        
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            log("onScrollChanged");
            super.onScrollChanged(l, t, oldl, oldt);
        }
        
        @Override
        protected boolean onSetAlpha(int alpha) {
            log("onSetAlpha");
            return super.onSetAlpha(alpha);
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            log("onSizeChanged");
            super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        public void onStartTemporaryDetach() {
            log("onStartTemporaryDetach");
            super.onStartTemporaryDetach();
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            log("onTouchEvent");
            return super.onTouchEvent(event);
        }
        
        @Override
        public boolean onTrackballEvent(MotionEvent event) {
            log("onTrackballEvent");
            return super.onTrackballEvent(event);
        }
        
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            log("onWindowFocusChanged-hasWindowFocus:" + hasWindowFocus);
            super.onWindowFocusChanged(hasWindowFocus);
        }
        
        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            log("onWindowVisibilityChanged-visibility:" + visibility);
            super.onWindowVisibilityChanged(visibility);
        }
        
        @Override
        protected void dispatchDraw(Canvas canvas) {
            log("dispatchDraw");
            super.dispatchDraw(canvas);
        }
        
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            log("dispatchKeyEvent");
            return super.dispatchKeyEvent(event);
        }
        
        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            log("dispatchKeyEventPreIme");
            return super.dispatchKeyEventPreIme(event);
        }
        
        @Override
        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
            log("dispatchKeyShortcutEvent");
            return super.dispatchKeyShortcutEvent(event);
        }
        
        @Override
        protected void dispatchRestoreInstanceState(
                SparseArray<Parcelable> container) {
            log("dispatchRestoreInstanceState");
            super.dispatchRestoreInstanceState(container);
        }
        
        @Override
        protected void dispatchSaveInstanceState(
                SparseArray<Parcelable> container) {
            log("dispatchSaveInstanceState");
            super.dispatchSaveInstanceState(container);
        }
        
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            log("dispatchTouchEvent");
            return super.dispatchTouchEvent(ev);
        }
        
        @Override
        public boolean dispatchTrackballEvent(MotionEvent event) {
            log("dispatchTrackballEvent");
            return super.dispatchTrackballEvent(event);
        }
        
        @Override
        public void dispatchWindowFocusChanged(boolean hasFocus) {
            log("dispatchWindowFocusChanged-hasFocus:" + hasFocus);
            super.dispatchWindowFocusChanged(hasFocus);
        }
        
        @Override
        public void dispatchWindowVisibilityChanged(int visibility) {
            log("dispatchWindowVisibilityChanged-visibility:" + visibility);
            super.dispatchWindowVisibilityChanged(visibility);
        }
        
        @Override
        public void draw(Canvas canvas) {
            log("draw");
            super.draw(canvas);
        }
        
        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            log("drawChild");
            return super.drawChild(canvas, child, drawingTime);
        }
        
        private void log(String content)
        {
            TestOnMethod.super.log("(MyViewGroup)" + content);
        }
    }
    
    private class MyView extends View {

        public MyView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            log("onLayout-changed:" + changed);
            super.onLayout(changed, l, t, r, b);
        }
        
        @Override
        protected void onAnimationEnd() {
            log("onAnimationEnd");
            super.onAnimationEnd();
        }
        
        @Override
        protected void onAnimationStart() {
            log("onAnimationStart");
            super.onAnimationStart();
        }
        
        @Override
        protected void onAttachedToWindow() {
            log("onAttachedToWindow");
            super.onAttachedToWindow();
        }
        
        @Override
        public boolean onCheckIsTextEditor() {
            log("onCheckIsTextEditor");
            return super.onCheckIsTextEditor();
        }
        
        @Override
        protected void onCreateContextMenu(ContextMenu menu) {
            log("onCreateContextMenu");
            super.onCreateContextMenu(menu);
        }
        
        @Override
        protected int[] onCreateDrawableState(int extraSpace) {
            log("onCreateDrawableState");
            return super.onCreateDrawableState(extraSpace);
        }
        
        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            log("onCreateInputConnection");
            return super.onCreateInputConnection(outAttrs);
        }
        
        @Override
        protected void onDetachedFromWindow() {
            log("onDetachedFromWindow");
            super.onDetachedFromWindow();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            log("onDraw");
            super.onDraw(canvas);
        }
        
        @Override
        protected void onFinishInflate() {
            log("onFinishInflate");
            super.onFinishInflate();
        }
        
        @Override
        public void onFinishTemporaryDetach() {
            log("onFinishTemporaryDetach");
            super.onFinishTemporaryDetach();
        }
        
        @Override
        protected void onFocusChanged(boolean gainFocus, int direction,
                Rect previouslyFocusedRect) {
            log("onFocusChanged-gainFocus:" + gainFocus);
            super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            log("onKeyDown-keyCode:" + keyCode);
            return super.onKeyDown(keyCode, event);
        }
        
        @Override
        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            log("onKeyLongPress-keyCode:" + keyCode);
            return super.onKeyLongPress(keyCode, event);
        }
        
        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount,
                KeyEvent event) {
            log("onKeyMultiple-keyCode:" + keyCode);
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
        
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            log("onKeyPreIme-keyCode:" + keyCode);
            return super.onKeyPreIme(keyCode, event);
        }
        
        @Override
        public boolean onKeyShortcut(int keyCode, KeyEvent event) {
            log("onKeyShortcut-keyCode:" + keyCode);
            return super.onKeyShortcut(keyCode, event);
        }
        
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            log("onKeyUp-keyCode:" + keyCode);
            return super.onKeyUp(keyCode, event);
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            log("onMeasure");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        
        @Override
        protected void onRestoreInstanceState(Parcelable state) {
            log("onRestoreInstanceState");
            super.onRestoreInstanceState(state);
        }
        
        @Override
        protected Parcelable onSaveInstanceState() {
            log("onSaveInstanceState");
            return super.onSaveInstanceState();
        }
        
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            log("onScrollChanged");
            super.onScrollChanged(l, t, oldl, oldt);
        }
        
        @Override
        protected boolean onSetAlpha(int alpha) {
            log("onSetAlpha");
            return super.onSetAlpha(alpha);
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            log("onSizeChanged");
            super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        public void onStartTemporaryDetach() {
            log("onStartTemporaryDetach");
            super.onStartTemporaryDetach();
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            log("onTouchEvent");
            return super.onTouchEvent(event);
        }
        
        @Override
        public boolean onTrackballEvent(MotionEvent event) {
            log("onTrackballEvent");
            return super.onTrackballEvent(event);
        }
        
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            log("onWindowFocusChanged-hasWindowFocus:" + hasWindowFocus);
            super.onWindowFocusChanged(hasWindowFocus);
        }
        
        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            log("onWindowVisibilityChanged-visibility:" + visibility);
            super.onWindowVisibilityChanged(visibility);
        }
        
        @Override
        protected void dispatchDraw(Canvas canvas) {
            log("dispatchDraw");
            super.dispatchDraw(canvas);
        }
        
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            log("dispatchKeyEvent");
            return super.dispatchKeyEvent(event);
        }
        
        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            log("dispatchKeyEventPreIme");
            return super.dispatchKeyEventPreIme(event);
        }
        
        @Override
        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
            log("dispatchKeyShortcutEvent");
            return super.dispatchKeyShortcutEvent(event);
        }
        
        @Override
        protected void dispatchRestoreInstanceState(
                SparseArray<Parcelable> container) {
            log("dispatchRestoreInstanceState");
            super.dispatchRestoreInstanceState(container);
        }
        
        @Override
        protected void dispatchSaveInstanceState(
                SparseArray<Parcelable> container) {
            log("dispatchSaveInstanceState");
            super.dispatchSaveInstanceState(container);
        }
        
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            log("dispatchTouchEvent");
            return super.dispatchTouchEvent(ev);
        }
        
        @Override
        public boolean dispatchTrackballEvent(MotionEvent event) {
            log("dispatchTrackballEvent");
            return super.dispatchTrackballEvent(event);
        }
        
        @Override
        public void dispatchWindowFocusChanged(boolean hasFocus) {
            log("dispatchWindowFocusChanged-hasFocus:" + hasFocus);
            super.dispatchWindowFocusChanged(hasFocus);
        }
        
        @Override
        public void dispatchWindowVisibilityChanged(int visibility) {
            log("dispatchWindowVisibilityChanged-visibility:" + visibility);
            super.dispatchWindowVisibilityChanged(visibility);
        }
        
        @Override
        public void draw(Canvas canvas) {
            log("draw");
            super.draw(canvas);
        }
        
        private void log(String content)
        {
            TestOnMethod.super.log("(MyView)" + content);
        }
    }
}