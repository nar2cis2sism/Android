package engine.android.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import engine.android.core.Forelet.Task.TaskCallback;
import engine.android.core.annotation.Injector;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 前台展现界面<p>
 * 功能：View注解，对话框管理，异步任务及进度条操作，数据验证，
 * JavaBean与视图绑定，Fragment事务，Activity导航，横竖屏切换
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class Forelet extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            mTransaction = restoreFragmentTransactionToken();
            commit();

            if ((mTask = TaskWrapper.class.cast(restoreTaskToken())) != null)
            {
                onRestoreTaskExecuting(mTask.getTask());
            }

            if ((mProgress = ProgressWrapper.class.cast(restoreProgressToken())) != null)
            {
                mProgress.setup(onCreateProgressDialog());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        commitAllowed = true;
        commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        commitAllowed = false;
        saveDialogs(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restoreDialogs(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        if (isFinishing() && !isChangingConfigurations())
        {
            mTransaction = null;
            cancelTask();

            hideProgress();
            mProgress = null;

            onFinish();
        }
        else
        {
            if (mTransaction != null)
            {
                saveFragmentTransactionToken(mTransaction);
                mTransaction = null;
            }

            if (mTask != null)
            {
                saveTaskToken(mTask);

                mTask.onConfigurationChanged();
                mTask = null;
            }

            if (mProgress != null)
            {
                if (mProgress.isGoingToShow())
                {
                    saveProgressToken(mProgress);
                }

                mProgress.onConfigurationChanged();
                mProgress = null;
            }
        }

        closeDialogs();
        clearValidation();
        clearJavaBean();

        super.onDestroy();
    }

    /**
     * 手动关闭Activity时调用
     */
    protected void onFinish() {}

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        injectView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        injectView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        injectView();
    }

    private void injectView() {
        Injector.injectView(this);
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
                dialog.dismiss();
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
                onRestoreDialogShowing(name);
            }
        }
    }

    /**
     * Restore a showing dialog
     * 
     * @param name 对话框名称
     * @see #showDialog(String, Dialog)
     */
    protected void onRestoreDialogShowing(String name) {}

    /******************************* 任务管理机制 *******************************/

    /**
     * 内置任务（深度定制）
     */
    public static class Task extends android.os.AsyncTask<Void, Integer, Object> {

        private final TaskExecutor mTaskExecutor;
        private WeakReference<TaskCallback> mTaskCallback;

        /** 结果是否有效(Activity被销毁后结果无法处理) **/
        private boolean isResultAvailable = true;
        private Object result;
        private boolean hasResult;

        public Task(TaskExecutor taskExecutor, TaskCallback taskCallback) {
            mTaskExecutor = taskExecutor;
            setTaskCallback(taskCallback);
        }

        private void setTaskCallback(TaskCallback taskCallback) {
            if (taskCallback == null)
            {
                mTaskCallback = null;
            }
            else
            {
                mTaskCallback = new WeakReference<TaskCallback>(taskCallback);
            }
        }

        /**
         * 返回任务名称
         */
        protected String getTaskName() {
            return toString();
        }

        void executeTask() {
            doExecuteTask();
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

        /**
         * Need to call it manually when restored
         */
        public final void restart(TaskCallback taskCallback) {
            setTaskCallback(taskCallback);
            setResultAvailable(true);
        }

        void setResultAvailable(boolean isResultAvailable) {
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
                    callback.onFinished(result);
                }

                mTaskCallback = null;
            }
        }

        public static interface TaskExecutor {

            /**
             * 任务执行方法
             * 
             * @return 执行结果
             */
            public Object doExecute();

            /**
             * 任务取消
             */
            public void cancel();
        }

        public static interface TaskCallback {

            /**
             * 任务完成回调方法
             * 
             * @param 执行结果
             */
            public void onFinished(Object result);
        }
    }

    private static class TaskWrapper extends TimerTask {

        private final Task task;                                // 当前执行任务

        private static final String TIMER_NAME_PREFIX = "Forelet:TimerTask-";
        private Timer timer;                                    // 任务执行定时器

        public TaskWrapper(Task task) {
            this.task = task;
        }

        public void executeTask(long delay) {
            if (delay > 0)
            {
                timer = new Timer(TIMER_NAME_PREFIX + task.getTaskName());
                timer.schedule(this, delay);
            }
            else
            {
                task.executeTask();
            }
        }

        public void cancelTask() {
            cancelTimer();
            task.cancelTask();
        }

        private void cancelTimer() {
            if (timer != null)
            {
                timer.cancel();
                timer = null;
            }
        }

        public Task getTask() {
            return task;
        }

        @Override
        public void run() {
            task.executeTask();
            cancelTimer();
        }

        public void onConfigurationChanged() {
            task.setResultAvailable(false);
        }
    }

    private TaskWrapper mTask;

    /**
     * 执行任务
     * 
     * @see #executeTask(Task, long)
     */
    public final void executeTask(Task task) {
        executeTask(task, 0);
    }

    /**
     * 执行任务
     * 
     * @param task 内置任务
     * @param delay 延迟时间
     */
    public final void executeTask(Task task, long delay) {
        (mTask = new TaskWrapper(task)).executeTask(delay);
    }

    /**
     * 取消任务
     */
    public final void cancelTask() {
        if (mTask != null)
        {
            mTask.cancelTask();
            mTask = null;
        }
    }

    /**
     * Provide a mechanism to save unfinished task when configuration changed
     * 
     * @param token 需要保存的任务token
     */
    protected void saveTaskToken(Object token) {}

    /**
     * Provide a mechanism to restore unfinished task when configuration changed
     * 
     * @return 用以恢复的任务token
     */
    protected Object restoreTaskToken() { return null; }

    /**
     * Restore a executing task
     * 
     * @param task 任务实例
     * @see Task#restart(TaskCallback)
     */
    protected void onRestoreTaskExecuting(Task task) {}

    /******************************* 进度条操作 *******************************/

    /**
     * 进度条取消操作
     */
    protected final class TaskCancelListener implements OnCancelListener {

        private final OnCancelListener onCancelListener;

        public TaskCancelListener(OnCancelListener onCancelListener) {
            this.onCancelListener = onCancelListener;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (onCancelListener != null)
            {
                onCancelListener.onCancel(dialog);
            }

            cancelTask();
        }
    }

    /**
     * 屏蔽搜索按键
     */
    protected final class TaskKeyListener implements OnKeyListener {

        private final OnKeyListener onKeyListener;

        public TaskKeyListener(OnKeyListener onKeyListener) {
            this.onKeyListener = onKeyListener;
        }

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (onKeyListener != null && onKeyListener.onKey(dialog, keyCode, event))
            {
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_SEARCH)
            {
                return true;
            }

            return false;
        }
    }
    
    /**
     * 子类可自定义进度条样式
     */
    protected ProgressDialog onCreateProgressDialog() {
        ProgressDialog progress = new ProgressDialog(this);

        progress.setCanceledOnTouchOutside(false);

        progress.setOnCancelListener(new TaskCancelListener(null));
        progress.setOnKeyListener(new TaskKeyListener(null));

        progress.setOwnerActivity(this);

        return progress;
    }
    
    /**
     * 通用进度条设置
     */
    public static class ProgressSetting {
        
        private static ProgressSetting defaultSetting;

        private int mTitleResourceId;
        private Boolean useTitleResource;

        private CharSequence mTitle;
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
            useTitleResource = null;
            mTitle = null;
            mMessage = null;
            mCancelable = true;
            return this;
        }
        
        /**
         * 提供一个默认的设置供重复利用
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

    private static class ProgressWrapper extends Handler {

        private ProgressSetting mSetting;                       // 当前对话框设置
        private WeakReference<ProgressDialog> mProgress;

        /** 能否显示进度条(Activity被销毁后无法继续显示) **/
        private boolean isShownAvailable = true;
        /** 是否需要显示进度条 **/
        private boolean isGoingToShow;

        public ProgressWrapper(ProgressDialog progress) {
            setProgressDialog(progress);
        }

        public void setup(ProgressDialog progress) {
            setProgressDialog(progress);
            setShownAvailable(true);
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
            isGoingToShow = true;
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
        }

        public void hideProgress() {
            removeCallbacksAndMessages(null);
            isGoingToShow = false;
            showOrHideProgress(false);
        }

        public boolean isGoingToShow() {
            return isGoingToShow || isShowing();
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
                showOrHideProgress(false);
                setProgressDialog(null);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            show((ProgressSetting) msg.obj);
        }

        public void onConfigurationChanged() {
            setShownAvailable(false);
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
                        isGoingToShow = false;
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

    /**
     * Provide a mechanism to save showing progress when configuration changed
     * 
     * @see #saveTaskToken(Object)
     */
    protected void saveProgressToken(Object token) {}

    /**
     * Provide a mechanism to restore showing progress when configuration
     * changed
     * 
     * @see #restoreTaskToken()
     */
    protected Object restoreProgressToken() { return null; }

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
    public static interface Validation<T extends View> {

        public boolean isValid(T view);
    }

    /**
     * 验证适配器
     */
    public static abstract class ValidationAdapter<S, T extends View>
            implements Validation<T> {

        @Override
        public boolean isValid(T view) {
            return isValid(getValue(view));
        }

        public abstract S getValue(T view);

        public abstract boolean isValid(S s);
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

    /******************************* JavaBean模块 *******************************/

    private static class JavaBeanView<T extends View> {

        private final T view;
        private final JavaBean<T> bean;

        public JavaBeanView(T view, JavaBean<T> bean) {
            this.view = view;
            this.bean = bean;
        }

        public void fillView() {
            bean.setValueTo(view);
        }

        public void fillBean() {
            bean.getValueFrom(view);
        }
    }

    private LinkedList<JavaBeanView<? extends View>> beans;

    /**
     * 绑定视图对象
     */
    public <T extends View> void bindJavaBean(T view, JavaBean<T> bean) {
        if (beans == null)
        {
            beans = new LinkedList<JavaBeanView<? extends View>>();
        }

        beans.add(new JavaBeanView<T>(view, bean));
    }

    public void clearJavaBean() {
        if (beans != null)
        {
            beans.clear();
            beans = null;
        }
    }

    /**
     * 从视图取值填充JavaBean
     */
    public void fillBeanFromView() {
        if (beans != null)
        {
            for (JavaBeanView<? extends View> view : beans)
            {
                view.fillBean();
            }
        }
    }

    /**
     * 从JavaBean取值填充视图
     */
    public void fillViewFromBean() {
        if (beans != null)
        {
            for (JavaBeanView<? extends View> view : beans)
            {
                view.fillView();
            }
        }
    }

    /**
     * JavaBean接口
     */
    public static interface JavaBean<T extends View> {

        public void setValueTo(T view);

        public void getValueFrom(T view);
    }

    /**
     * 文本对象视图
     */
    public static abstract class TextJavaBean<T extends TextView> implements JavaBean<T> {

        @Override
        public void setValueTo(T view) {
            view.setText(get());
        }

        @Override
        public void getValueFrom(T view) {
            set(view.getText().toString());
        }

        public abstract String get();

        public abstract void set(String s);
    }

    /******************************* Fragment模块 *******************************/

    private FragmentTransaction mTransaction;

    private boolean commitAllowed = true;

    public static interface FragmentTransaction {

        public void commit(FragmentManager fragmentManager);
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

    /**
     * Provide a mechanism to save uncommitted fragment transaction when
     * configuration changed
     * 
     * @see #saveTaskToken(Object)
     */
    protected void saveFragmentTransactionToken(FragmentTransaction transaction) {}

    /**
     * Provide a mechanism to restore uncommitted fragment transaction when
     * configuration changed
     * 
     * @see #restoreTaskToken()
     */
    protected FragmentTransaction restoreFragmentTransactionToken() { return null; }

    /******************************* ActionBar模块 *******************************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Class<? extends Activity> cls = parentActivity();
                if (cls != null)
                {
                    navigateUpTo(cls);
                }
                else
                {
                    finish();
                }

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    protected void navigateUpTo(Class<? extends Activity> cls) {
        NavUtils.navigateUpTo(this, new Intent(this, cls).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    protected Class<? extends Activity> parentActivity() {
        return null;
    }
}