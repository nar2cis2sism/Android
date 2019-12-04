package demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ota.PatchUtil;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory.LogUtil;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;
import engine.android.util.manager.SDCardManager;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

import java.io.File;
import java.io.IOException;

public class AppUpgradeActivity extends Activity {
    
    TextView tv;
    
    Button btn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);
        
        btn = new Button(this);
        btn.setText("升级");
        layout.addView(btn);
        
        tv = new TextView(this);
        layout.addView(tv);
        
        final File otaDir = new File(SDCardManager.openSDCardAppDir(this), "ota");
        final File oldApk = new File(otaDir, "iReader1.6.2.0(v35).apk");
        final File compareApk = new File(otaDir, "iReader1.8.0.1(v40).apk");
        final File patch = new File(otaDir, "iReader.patch");
        try {
            FileManager.writeFile(oldApk, IOUtil.readStream(getAssets().open("ota/" + oldApk.getName())), false);
            FileManager.writeFile(compareApk, IOUtil.readStream(getAssets().open("ota/" + compareApk.getName())), false);
            FileManager.writeFile(patch, IOUtil.readStream(getAssets().open("ota/" + patch.getName())), false);
        } catch (IOException e) {
            e.printStackTrace();
            tv.setText(LogUtil.getExceptionInfo(e));
            return;
        }
        
        StringBuilder sb = new StringBuilder()
        .append("旧版本:").append(Formatter.formatFileSize(this, oldApk.length())).append("\n")
        .append("新版本:").append(Formatter.formatFileSize(this, compareApk.length())).append("\n")
        .append("补丁包:").append(Formatter.formatFileSize(this, patch.length()));
        tv.setText(sb.toString());
        
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                File newApk = new File(otaDir, "new.apk");
                FileManager.delete(newApk);
                
                try {
                    PatchUtil.applyPatch(oldApk.getAbsolutePath(), newApk.getAbsolutePath(), patch.getAbsolutePath());
                    tv.append("\n");
                    tv.append("合成后:");
                    tv.append(Formatter.formatFileSize(AppUpgradeActivity.this, newApk.length()));
                } catch (IOException e) {
                    e.printStackTrace();
                    tv.append("\n");
                    tv.append(LogUtil.getExceptionInfo(e));
                    return;
                }
                
                String newSHA1 = HexUtil.encode(CryptoUtil.SHA1(FileManager.readFile(newApk)));
                String compareSHA1 = HexUtil.encode(CryptoUtil.SHA1(FileManager.readFile(compareApk)));
                tv.append("\n");
                tv.append("比对SHA1值:");
                tv.append(TextUtils.equals(newSHA1, compareSHA1) ? "成功" : "失败");
            }
        });
    }
}