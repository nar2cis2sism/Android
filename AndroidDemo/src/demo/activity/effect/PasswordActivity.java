package demo.activity.effect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import demo.android.R;
import demo.wheel.NumericWheelAdapter;
import demo.wheel.OnWheelScrollListener;
import demo.wheel.WheelView;

public class PasswordActivity extends Activity implements OnClickListener, OnWheelScrollListener {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        
        //Warning: It's abnormal under tag(supports-screens) or tag(uses-sdk)
        
        initWheel(R.id.pwd1);
        initWheel(R.id.pwd2);
        initWheel(R.id.pwd3);
        initWheel(R.id.pwd4);
        
        Button btn_mix = (Button) findViewById(R.id.btn_mix);
        btn_mix.setOnClickListener(this);
        
        updateStatus();
    }
    
    private void initWheel(int resId)
    {
        WheelView wheel = getWheel(resId);
        wheel.setAdapter(new NumericWheelAdapter());
        wheel.setCyclic(true);
        wheel.setCurrentItem((int) (Math.random() * 10));
        
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
        wheel.addScrollingListener(this);
    }
    
    private void mixWheel(int resId)
    {
        WheelView wheel = getWheel(resId);
        wheel.scroll(-25 + (int) (Math.random() * 50), 2000);
    }
    
    private WheelView getWheel(int resId)
    {
        return (WheelView) findViewById(resId);
    }

    @Override
    public void onClick(View v) {
        mixWheel(R.id.pwd1);
        mixWheel(R.id.pwd2);
        mixWheel(R.id.pwd3);
        mixWheel(R.id.pwd4);
    }
    
    private void updateStatus()
    {
        TextView tv_status = (TextView) findViewById(R.id.tv_status);
        if (checkPin(2, 4, 6, 1))
        {
            tv_status.setText("Congratulation!");
        }
        else
        {
            tv_status.setText("Invalid PIN");
        }
    }
    
    private boolean checkPin(int pwd1, int pwd2, int pwd3, int pwd4)
    {
        return checkPwd(R.id.pwd1, pwd1) && checkPwd(R.id.pwd2, pwd2)
            && checkPwd(R.id.pwd3, pwd3) && checkPwd(R.id.pwd4, pwd4);
    }
    
    private boolean checkPwd(int resId, int pwd)
    {
        return getWheel(resId).getCurrentItem() == pwd;
    }

    @Override
    public void onScrollingStarted(WheelView wheel) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onScrollingFinished(WheelView wheel) {
        updateStatus();
    }
}