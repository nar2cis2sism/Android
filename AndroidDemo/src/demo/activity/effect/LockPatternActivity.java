package demo.activity.effect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import demo.android.R;
import demo.widget.LockPatternView;
import demo.widget.LockPatternView.Cell;
import demo.widget.LockPatternView.DisplayMode;
import demo.widget.LockPatternView.OnPatternListener;

import java.util.ArrayList;
import java.util.List;

public class LockPatternActivity extends Activity implements OnClickListener, OnPatternListener {
    
    LockPatternUtil util;
    
    LockPatternView lock;
    
    boolean setPwd = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_pattern);
        
        util = new LockPatternUtil(getPreferences(MODE_PRIVATE));
        
        lock = (LockPatternView) findViewById(R.id.lock);
        Button anim_demo = (Button) findViewById(R.id.anim_demo);
        Button stealth_mode = (Button) findViewById(R.id.stealth_mode);
        Button set_pwd = (Button) findViewById(R.id.set_pwd);
        Button reset_pwd = (Button) findViewById(R.id.reset_pwd);
        
        lock.setOnPatternListener(this);
        
        anim_demo.setOnClickListener(this);
        stealth_mode.setOnClickListener(this);
        set_pwd.setOnClickListener(this);
        reset_pwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.anim_demo:
            ArrayList<Cell> pattern = new ArrayList<Cell>();
            pattern.add(Cell.getCell(0, 0));
            pattern.add(Cell.getCell(0, 1));
            pattern.add(Cell.getCell(0, 2));
            pattern.add(Cell.getCell(1, 2));
            pattern.add(Cell.getCell(2, 2));
            lock.setPattern(DisplayMode.Animation, pattern);
            break;
        case R.id.stealth_mode:
            lock.setStealthMode(!lock.isStealthMode());
            break;
        case R.id.set_pwd:
            lock.clearPattern();
            setPwd = true;
            break;
        case R.id.reset_pwd:
            lock.clearPattern();
            util.removePattern();
            setPwd = false;
            break;
        }
    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPatternStart() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        if (setPwd)
        {
            if (pattern.size() < 4)
            {
                lock.setDisplayMode(DisplayMode.Wrong);
                Toast.makeText(this, "应至少连接4个点", Toast.LENGTH_SHORT).show();
                return;
            }
            
            lock.clearPattern();
            Toast.makeText(this, "密码已经设置", Toast.LENGTH_SHORT).show();
            util.savePattern(pattern);
            setPwd = false;
        }
        else
        {
            if (util.getSerializedPattern() == null)
            {
                Toast.makeText(this, "请先设置密码", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (util.checkPattern(pattern))
                {
                    Toast.makeText(this, "密码正确", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    lock.setDisplayMode(DisplayMode.Wrong);
                    Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onPatternCleared() {
        // TODO Auto-generated method stub
        
    }
    
    private static class LockPatternUtil {
        
        private static final String KEY = "pattern";
        
        private final SharedPreferences sp;
        
        public LockPatternUtil(SharedPreferences sp) {
            this.sp = sp;
        }
        
        public void savePattern(List<Cell> pattern)
        {
            sp.edit().putString(KEY, LockPatternView.serialize(pattern)).commit();
        }
        
        public String getSerializedPattern()
        {
            return sp.getString(KEY, null);
        }
        
        public boolean checkPattern(List<Cell> pattern)
        {
            String serializedPattern = getSerializedPattern();
            if (TextUtils.isEmpty(serializedPattern))
            {
                return false;
            }
            
            return serializedPattern.equals(LockPatternView.serialize(pattern));
        }
        
        public void removePattern()
        {
            sp.edit().remove(KEY).commit();
        }
    }
}