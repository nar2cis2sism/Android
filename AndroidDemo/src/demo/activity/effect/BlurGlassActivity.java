package demo.activity.effect;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import demo.android.R;
import demo.blurglass.BlurGlassHelper;
import demo.blurglass.BlurGlassUtil;

public class BlurGlassActivity extends Activity implements OnSeekBarChangeListener {
    
    BlurGlassHelper helper;
    
    TextView blurRadius_tv;
    SeekBar blurRadius_sb;
    TextView downSampling_tv;
    SeekBar downSampling_sb;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.blur_glass_activity);

        helper = new BlurGlassHelper((ImageView) findViewById(R.id.blur_glass));
        helper.setContentView(decorateContentView(findViewById(R.id.content)));
        
        blurRadius_tv = (TextView) findViewById(R.id.blurRadius_tv);
        blurRadius_sb = (SeekBar) findViewById(R.id.blurRadius_sb);
        blurRadius_sb.setOnSeekBarChangeListener(this);

        downSampling_tv = (TextView) findViewById(R.id.downSampling_tv);
        downSampling_sb = (SeekBar) findViewById(R.id.downSampling_sb);
        downSampling_sb.setOnSeekBarChangeListener(this);
        
        setSeekBarValue(blurRadius_sb, helper.getBlurRadius(), BlurGlassUtil.MIN_BLUR_RADIUS, BlurGlassUtil.MAX_BLUR_RADIUS);
        updateBlurRadiusTextView();
        
        setSeekBarValue(downSampling_sb, helper.getDownSampling(), BlurGlassUtil.MIN_DOWNSAMPLING, BlurGlassUtil.MAX_DOWNSAMPLING);
        updateDownSamplingTextView();
    }
    
    private View decorateContentView(View contentView) {
        if (contentView instanceof ScrollView)
        {
            ScrollView sv = (ScrollView) contentView;
            ViewGroup parent = (ViewGroup) sv.getParent();
            View child = sv.getChildAt(0);
            
            parent.removeViewAt(0);
            sv.removeAllViews();
            
            sv = new BlurGlassScrollView(this, helper);
            sv.addView(child);
            parent.addView(sv, 0);
            
            contentView = sv;
        }
        
        return contentView;
    }
    
    private void setSeekBarValue(SeekBar seekBar, float value, float min, float max) {
        float span = max - min;
        value = (value - min) / span;
        seekBar.setProgress(Math.round(value * seekBar.getMax()));
    }
    
    private float getSeekBarValue(SeekBar seekBar, float min, float max) {
        float span = max - min;
        float value = seekBar.getProgress() / (float) seekBar.getMax();
        return min + value * span;
    }
    
    private void updateBlurRadiusTextView() {
        blurRadius_tv.setText("Blur radius=" + helper.getBlurRadius());
    }
    
    private void updateDownSamplingTextView() {
        downSampling_tv.setText("Down sampling=" + helper.getDownSampling());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
        {
            return;
        }
        
        if (seekBar == blurRadius_sb)
        {
            float value = getSeekBarValue(blurRadius_sb, BlurGlassUtil.MIN_BLUR_RADIUS, BlurGlassUtil.MAX_BLUR_RADIUS);
            helper.setBlurRadius(Math.round(value));
            updateBlurRadiusTextView();
        }
        else if (seekBar == downSampling_sb)
        {
            float value = getSeekBarValue(downSampling_sb, BlurGlassUtil.MIN_DOWNSAMPLING, BlurGlassUtil.MAX_DOWNSAMPLING);
            helper.setDownSampling(Math.round(value));
            updateDownSamplingTextView();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }
    
    private static class BlurGlassScrollView extends ScrollView {
        
        private final BlurGlassHelper helper;

        public BlurGlassScrollView(Context context, BlurGlassHelper helper) {
            super(context);
            this.helper = helper;
        }
        
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            helper.invalidate();
        }
        
        @Override
        protected float getLeftFadingEdgeStrength() {
            return 0.0f;
        }
        
        @Override
        protected float getTopFadingEdgeStrength() {
            return 0.0f;
        }
        
        @Override
        protected float getRightFadingEdgeStrength() {
            return 0.0f;
        }
        
        @Override
        protected float getBottomFadingEdgeStrength() {
            return 0.0f;
        }
    }
}