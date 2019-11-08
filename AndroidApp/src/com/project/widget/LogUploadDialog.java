package com.project.widget;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.ui.BaseDialog;
import engine.android.util.file.FileManager;
import engine.android.util.image.ImageUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MyContext;
import com.project.network.action.file.UploadLog;
import com.project.util.LogUploader;

import org.apache.http.HttpEntity;

import java.io.File;

/**
 * 日志上传对话框
 * 
 * @author Daimon
 */
public class LogUploadDialog extends BaseDialog {

    @InjectView(R.id.text)
    EditText text;

    public LogUploadDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.log_upload_dialog);
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        super.cancel();
    }

    @OnClick(R.id.ok)
    void ok() {
        LOG.log("", "");
        LOG.log("日志说明", text.getText());

        new LogUploadTask().execute();
    }

    public static class LogUploadTask extends AsyncTask<Void, Void, Void> {

        private Bitmap screenshot;

        @Override
        protected void onPreExecute() {
            Activity activity = MyApp.getApp().currentActivity();
            if (activity != null)
            {
                View decor = activity.getWindow().getDecorView();
                if (decor != null)
                {
                    screenshot = ImageUtil.view2Bitmap(decor);
                }
            }

            MyApp.getApp().getActivityStack().popupAllActivities();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File logDir = LogUploader.upload(MyContext.getContext(), screenshot);
            if (logDir != null)
            {
                boolean isDebuggable = MyApp.getApp().isDebuggable();
                if (!isDebuggable || !MyApp.global().getConfig().isOffline())
                {
                    UploadLog action = new UploadLog(logDir);
                    try {
                        HttpEntity entity = action.execute();
                        if (entity != null)
                        {
                            FileManager.delete(logDir);
                        }
                    } catch (Exception e) {
                        Log.w("uploadLog", e);
                    } finally {
                        if (!isDebuggable) FileManager.delete(logDir.getParentFile());
                    }
                }
            }

            Process.killProcess(Process.myPid());
            System.exit(0);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {}
    }
}