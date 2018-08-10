package engine.android.core;

import static engine.android.core.Injector.inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import engine.android.core.Forelet.FragmentTransaction;
import engine.android.core.Forelet.Task;
import engine.android.core.Forelet.Task.TaskExecutor;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 前台展现界面<p>
 * 功能：View注解，对话框管理，异步任务及进度条操作，
 * 数据验证，Fragment事务，Activity导航，横竖屏切换等
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class Forelet extends Activity implements TaskCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            restoreDialogs(savedInstanceState);
            
            SavedInstance savedInstance = SavedInstance.restore(savedInstanceState);
            if (savedInstance == null)
            {
                return;
            }
            
            if ((mTask = savedInstance.task) != null) mTask.setup(this);
            if ((mProgress = ProgressWrapper.class.cast(savedInstance.progress)) != null)
                 mProgress.setup(onCreateProgressDialog());
            mTransaction = savedInstance.transaction;
            if (!savedInstance.savedMap.isEmpty())
                Injector.restoreState(this, savedInstance.savedMap);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        commitAllowed = true;
        commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        commitAllowed = false;
        super.onSaveInstanceState(outState);

        saveDialogs(outState);
        
        if (allowSaveInstanceState())
        {
            SavedInstance savedInstance = new SavedInstance();

            savedInstance.task = mTask;
            savedInstance.progress = mProgress;
            savedInstance.transaction = mTransaction;
            Injector.saveState(this, savedInstance.savedMap);
            
            SavedInstance.save(outState, savedInstance);
        }
    }
    
    /**
     * 为了提高性能，默认不保存状态
     */
    protected boolean allowSaveInstanceState() {
        return false;
    }

    @Override
    protected final void onDestroy() {
        if (isChangingConfigurations() || !isFinishing())
        {
            if (mTask != null) mTask.setup(null);
            if (mProgress != null) mProgress.setup(null);
            
            onDestroy(false);
        }
        else
        {
            cancelTask();
            hideProgress();
            
            onDestroy(true);
        }
        
        mTask = null;
        mProgress = null;
        mTransaction = null;

        closeDialogs();
        clearValidation();
        getHandler().removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    /**
     * @param finish True表示手动关闭Activity,False表示被系统杀掉
     */
    protected void onDestroy(boolean finish) {}

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        inject(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        inject(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        inject(this);
    }
    
    /******************************* 回退事件处理 *******************************/
    
    public interface OnBackListener {
        
        boolean onBackPressed();
    }
    
    private LinkedList<OnBackListener> onBackListener;
    
    public void addOnBackListener(OnBackListener listener) {
        if (onBackListener == null)
        {
            onBackListener = new LinkedList<OnBackListener>();
        }
        
        onBackListener.addFirst(listener);
    }
    
    public void removeOnBackListener(OnBackListener listener) {
        if (onBackListener != null)
        {
            onBackListener.remove(listener);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (onBackListener != null)
        {
            for (OnBackListener listener : onBackListener)
            {
                if (listener.onBackPressed())
                {
                    return;
                }
                else
                {
                    break;
                }
            }
        }
        
        super.onBackPressed();
    }

    /******************************* 对话框管理 *******************************/

    private static final String SAVED_DIALOGS_TAG       = "Forelet:savedDialogs";
    private static final String SAVED_DIALOG_KEY_PREFIX = "Forelet:dialog_";
    private static final String SAVED_DIALOG_NAMES_KEY  = "Forelet:savedDialogNames";

    private final LinkedHashMap<String, Dialog> mDialogs    // 界面绑定对话框查询表
    = new LinkedHashMap<String, Dialog>();

    /**
     * 显示对话框（保持唯一性）
     * 
     * @param name 对话框名称
     * @param dialog 对话框实例
     */
    public final void showDialog(String name, Dialog dialog) {
        if (TextUtils.isEmpty(name))
        {
            throw new NullPointerException("请给对话框取个名，亲！");
        }

        if (dialog == null)
        {
            Dialog d = mDialogs.remove(name);
            if (d != null && d.isShowing())
            {
                d.dismiss();
            }

            return;
        }

        Dialog d = mDialogs.get(name);
        if (d != null && d.isShowing())
        {
            return;
        }

        mDialogs.put(name, dialog);

        dialog.setOwnerActivity(this);
        dialog.show();
    }
    
    private void closeDialogs() {
        if (!mDialogs.isEmpty())
        {
            for (Dialog dialog : mDialogs.values())
            {
                if (dialog.isShowing()) dialog.dismiss();
            }
            
            mDialogs.clear();
        }
    }

    /**
     * 显示消息对话框
     * 
     * @param title 标题
     * @param message 内容
     * @param ok 确定按钮
     */
    public void showMessageDialog(CharSequence title, CharSequence message,
            CharSequence ok) {
        Dialog dialog = new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dialog.dismiss();
            }
        })
        .create();

        showDialog(getClass().getName() + title, dialog);
    }

    private void saveDialogs(Bundle outState) {
        if (mDialogs.isEmpty())
        {
            return;
        }

        Bundle dialogState = new Bundle();

        Set<String> names = mDialogs.keySet();

        // save each dialog's state, gather the names
        for (String name : names)
        {
            final Dialog dialog = mDialogs.get(name);
            dialogState.putBoolean(SAVED_DIALOG_KEY_PREFIX + name, dialog.isShowing());
        }

        dialogState.putStringArray(SAVED_DIALOG_NAMES_KEY,
                names.toArray(new String[names.size()]));
        outState.putBundle(SAVED_DIALOGS_TAG, dialogState);
    }

    private void restoreDialogs(Bundle savedInstanceState) {
        final Bundle dialogState = savedInstanceState.getBundle(SAVED_DIALOGS_TAG);
        if (dialogState == null)
        {
            return;
        }

        final String[] names = dialogState.getStringArray(SAVED_DIALOG_NAMES_KEY);

        for (String name : names)
        {
            if (dialogState.getBoolean(SAVED_DIALOG_KEY_PREFIX + name))
            {
                Injector.onRestoreDialogShowing(this, name);
            }
        }
    }

    /******************************* 任务管理机制 *******************************/

    /**
     * 封装一个简化版的异步任务
     */
    public static class Task extends android.os.AsyncTask<Void, Integer, Object> {

        private final TaskExecutor mTaskExecutor;
        
        private WeakReference<TaskCallback> mTaskCallback;
        private boolean mHasCallback;

        /** 结果是否有效(Activity被销毁后结果无法处理) **/
        private boolean isResultAvailable = true;
        private Object result;
        private boolean hasResult;
        
        public Task(TaskExecutor taskExecutor) {
            mTaskExecutor = taskExecutor;
        }

        void setTaskCallback(TaskCallback taskCallback) {
            if (taskCallback == null)
            {
                mTaskCallback = null;
            }
            else
            {
                mTaskCallback = new WeakReference<TaskCallback>(taskCallback);
            }
        }

        void setup(TaskCallback taskCallback) {
            if (taskCallback == null)
            {
                setResultAvailable(false);
            }
            else
            {
                if (mHasCallback) setTaskCallback(taskCallback);
                setResultAvailable(true);
            }
        }

        int executeTask() {
            doExecuteTask();
            return getId();
        }

        /**
         * 真正执行任务的入口
         */
        protected void doExecuteTask() {
            executeOnExecutor(THREAD_POOL_EXECUTOR);
        }

        void cancelTask() {
            setTaskCallback(null);
            mTaskExecutor.cancel();
            cancel(true);
        }
        
        private void setResultAvailable(boolean isResultAvailable) {
            if (this.isResultAvailable = isResultAvailable)
            {
                if (hasResult)
                {
                    postResult(result);
                    result = null;
                    hasResult = false;
                }
            }
            else
            {
                mHasCallback = mTaskCallback != null;
                setTaskCallback(null);
            }
        }

        @Override
        protected final Object doInBackground(Void... params) {
            return mTaskExecutor.doExecute();
        }

        @Override
        protected final void onPostExecute(Object result) {
            if (isResultAvailable)
            {
                postResult(result);
            }
            else
            {
                hasResult = true;
                this.result = result;
            }
        }

        private void postResult(Object result) {
            if (mTaskCallback != null)
            {
                TaskCallback callback = mTaskCallback.get();
                if (callback != null)
                {
                    callback.onTaskCallback(getId(), result);
                }

                mTaskCallback = null;
            }
        }
        
        private int getId() {
            return hashCode();
        }

        public interface TaskExecutor {

            /**
             * 任务执行方法
             * 
             * @return 执行结果
             */
            Object doExecute();

            /**
             * 任务取消
             */
            void cancel();
        }
    }

    private Task mTask;

    /**
     * 执行任务
     * 
     * @param executor 任务执行器
     * @param hasCallback 是否需要处理回调，通过{@link #onTaskCallback}接收回调
     * 
     * @return taskId 用于任务回调
     */
    public final int executeTask(TaskExecutor executor, boolean hasCallback) {
        return executeTask(new Task(executor), hasCallback);
    }

    /**
     * 执行任务
     * 
     * @param task 并发执行多个任务会覆盖当前任务
     * @param hasCallback 是否需要处理回调，通过{@link #onTaskCallback}接收回调
     * 
     * @return taskId 用于任务回调
     */
    public final int executeTask(Task task, boolean hasCallback) {
        if (hasCallback) task.setTaskCallback(this);
        return (mTask = task).executeTask();
    }
    
    /**
     * 取消指定任务
     */
    public final void cancelTask(Task task) {
        if (task != null) task.cancelTask();
    }

    /**
     * 取消当前任务
     */
    public final void cancelTask() {
        if (mTask != null)
        {
            mTask.cancelTask();
            mTask = null;
        }
    }

    /**
     * 任务完成回调方法
     * 
     * @param result 执行结果
     */
    @Override
    public void onTaskCallback(int taskId, Object result) {}

    /******************************* 进度条操作 *******************************/

    /**
     * 通用进度条设置
     */
    public static class ProgressSetting {
        
        private static ProgressSetting defaultSetting;
    
        private int mTitleResourceId;
        private CharSequence mTitle;
        private Boolean useTitleResource;
    
        private CharSequence mMessage;
    
        private boolean mCancelable = true;
    
        public ProgressSetting setTitle(int titleResourceId) {
            mTitleResourceId = titleResourceId;
            useTitleResource = Boolean.TRUE;
            return this;
        }
    
        public ProgressSetting setTitle(CharSequence title) {
            mTitle = title;
            useTitleResource = Boolean.FALSE;
            return this;
        }
    
        public ProgressSetting setMessage(CharSequence message) {
            mMessage = message;
            return this;
        }
    
        public ProgressSetting setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }
        
        /**
         * Call it to reset the status.
         */
        public ProgressSetting reset() {
            mTitleResourceId = 0;
            mTitle = null;
            useTitleResource = null;
            mMessage = null;
            mCancelable = true;
            return this;
        }
        
        /**
         * 提供一个默认的设置供重复使用
         */
        public static ProgressSetting getDefault() {
            if (defaultSetting == null)
            {
                defaultSetting = new ProgressSetting();
            }
            else
            {
                defaultSetting.reset();
            }
            
            return defaultSetting;
        }
    
        protected void setup(ProgressDialog progress) {
            if (useTitleResource != null)
            {
                if (useTitleResource)
                {
                    progress.setTitle(mTitleResourceId);
                }
                else
                {
                    progress.setTitle(mTitle);
                }
            }
    
            progress.setMessage(mMessage);
            progress.setCancelable(mCancelable);
        }
    }
    
    /**
     * 为了和任务关联自定义一个进度条
     */
    private class TaskProgressDialog extends ProgressDialog {

        public TaskProgressDialog(Context context) {
            super(context);
        }
        
        @Override
        public boolean onSearchRequested() {
            // 屏蔽搜索按键
            return false;
        }
        
        @Override
        public void cancel() {
            // 随即取消任务
            cancelTask();
            super.cancel();
        }
    }
    
    /**
     * 子类可自定义进度条样式
     */
    protected ProgressDialog onCreateProgressDialog() {
        ProgressDialog progress = new TaskProgressDialog(this);
        progress.setCanceledOnTouchOutside(false);
        progress.setOwnerActivity(this);
        return progress;
    }
    
    private static class ProgressWrapper extends Handler {

        private ProgressSetting mSetting;
        private WeakReference<ProgressDialog> mProgress;

        /** 能否显示进度条(Activity被销毁后无法继续显示) **/
        private boolean isShownAvailable = true;
        /** 是否需要显示进度条 **/
        private boolean isGoingToShow;

        public ProgressWrapper(ProgressDialog progress) {
            setProgressDialog(progress);
        }

        public void setup(ProgressDialog progress) {
            if (progress == null)
            {
                setShownAvailable(false);
            }
            else
            {
                setProgressDialog(progress);
                setShownAvailable(true);
            }
        }

        private void setProgressDialog(ProgressDialog progress) {
            if (progress == null)
            {
                mProgress = null;
            }
            else
            {
                mProgress = new WeakReference<ProgressDialog>(progress);
            }
        }

        public void showProgress(ProgressSetting setting, long delay) {
            if (delay > 0)
            {
                sendMessageDelayed(obtainMessage(0, setting), delay);
            }
            else
            {
                show(setting);
            }
        }

        private void show(ProgressSetting setting) {
            mSetting = setting;
            if (isShownAvailable)
            {
                showOrHideProgress(true);
            }
            else
            {
                isGoingToShow = true;
            }
        }

        public void hideProgress() {
            removeCallbacksAndMessages(null);
            showOrHideProgress(false);
        }

        private void setShownAvailable(boolean isShownAvailable) {
            if (this.isShownAvailable = isShownAvailable)
            {
                if (isGoingToShow)
                {
                    showOrHideProgress(true);
                }
            }
            else
            {
                isGoingToShow = isShowing();
                showOrHideProgress(false);
                setProgressDialog(null);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            show((ProgressSetting) msg.obj);
        }

        private void showOrHideProgress(boolean show) {
            if (mProgress != null)
            {
                ProgressDialog progress = mProgress.get();
                if (progress != null)
                {
                    if (show)
                    {
                        if (mSetting != null) mSetting.setup(progress);
                        progress.show();
                    }
                    else
                    {
                        progress.dismiss();
                    }
                }
            }
        }

        private boolean isShowing() {
            if (mProgress != null)
            {
                ProgressDialog progress = mProgress.get();
                if (progress != null)
                {
                    return progress.isShowing();
                }
            }

            return false;
        }
    }

    private ProgressWrapper mProgress;

    /**
     * 显示进度条
     * 
     * @see #showProgress(ProgressSetting, long)
     */
    public final void showProgress(ProgressSetting setting) {
        showProgress(setting, 0);
    }

    /**
     * 显示进度条
     * 
     * @param setting 进度条配置
     * @param delay 延迟时间
     */
    public final void showProgress(ProgressSetting setting, long delay) {
        if (mProgress == null)
        {
            mProgress = new ProgressWrapper(onCreateProgressDialog());
        }

        mProgress.showProgress(setting, delay);
    }

    /**
     * 隐藏进度条
     */
    public final void hideProgress() {
        if (mProgress != null)
        {
            mProgress.hideProgress();
        }
    }

    /******************************* 验证模块 *******************************/

    private static class ValidationView<T extends View> {

        private final T view;
        private final Validation<T> validation;

        public ValidationView(T view, Validation<T> validation) {
            this.view = view;
            this.validation = validation;
        }

        public boolean isValid() {
            return validation.isValid(view);
        }
    }

    private LinkedList<ValidationView<? extends View>> validations;

    /**
     * 绑定需要验证的视图
     */
    public <T extends View> void bindValidation(T view, Validation<T> validation) {
        if (validations == null)
        {
            validations = new LinkedList<ValidationView<? extends View>>();
        }

        validations.add(new ValidationView<T>(view, validation));
    }

    public void clearValidation() {
        if (validations != null)
        {
            validations.clear();
            validations = null;
        }
    }

    /**
     * 验证请求
     * 
     * @return 验证结果
     */
    public boolean requestValidation() {
        if (validations != null)
        {
            for (ValidationView<? extends View> view : validations)
            {
                if (!view.isValid())
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 验证接口
     */
    public interface Validation<T extends View> {

        boolean isValid(T view);
    }

    /**
     * 验证适配器
     */
    public static abstract class ValidationAdapter<S, T extends View>
            implements Validation<T> {

        @Override
        public final boolean isValid(T view) {
            return isValid(getValue(view));
        }

        public abstract S getValue(T view);

        public abstract boolean isValid(S value);
    }

    /**
     * 文本验证
     */
    public static abstract class TextValidation<T extends TextView>
            extends ValidationAdapter<String, T> {

        @Override
        public String getValue(T view) {
            return view.getText().toString();
        }
    }

    /******************************* Fragment模块 *******************************/

    private FragmentTransaction mTransaction;

    private boolean commitAllowed = true;

    public interface FragmentTransaction {

        void commit(FragmentManager fragmentManager);
    }

    public final void commitFragmentTransaction(FragmentTransaction transaction) {
        if (isFinishing())
        {
            return;
        }

        if (commitAllowed)
        {
            transaction.commit(getFragmentManager());
        }
        else
        {
            mTransaction = transaction;
        }
    }

    private void commit() {
        if (mTransaction != null)
        {
            mTransaction.commit(getFragmentManager());
            mTransaction = null;
        }
    }

    /******************************* ActionBar模块 *******************************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                onHomeUpPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    protected void onHomeUpPressed() {
        Class<? extends Activity> cls = parentActivity();
        if (cls != null)
        {
            navigateUpTo(cls);
        }
        else
        {
            finish();
        }
    }

    protected void navigateUpTo(Class<? extends Activity> cls) {
        NavUtils.navigateUpTo(this, new Intent(this, cls)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    protected Class<? extends Activity> parentActivity() {
        return null;
    }

    /******************************* Handler模块 *******************************/
    
    private Handler handler;
    
    public final Handler getHandler() {
        if (handler == null
        && (handler = getWindow().getDecorView().getHandler()) == null)
        {
            handler = new Handler();
        }
        
        return handler;
    }
}

interface TaskCallback {
    
    void onTaskCallback(int taskId, Object result);
}

class SavedInstance {
    
    private static final WeakHashMap<Bundle, SavedInstance> savedInstanceMap
    = new WeakHashMap<Bundle, SavedInstance>();
    
    public static void save(Bundle bundle, SavedInstance savedInstance) {
        savedInstanceMap.put(bundle, savedInstance);
    }
    
    public static SavedInstance restore(Bundle bundle) {
        return savedInstanceMap.get(bundle);
    }
    
    public Task task;
    
    public Object progress;
    
    public FragmentTransaction transaction;
    
    public final HashMap<String, Object> savedMap = new HashMap<String, Object>();
}