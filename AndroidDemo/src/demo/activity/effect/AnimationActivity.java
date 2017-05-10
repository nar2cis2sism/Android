package demo.activity.effect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import demo.android.R;
import demo.android.ui.ActivityAnimationAdapter;
import demo.android.ui.util.AnimationEffect.FolderAnimationEffect;

public class AnimationActivity extends Activity {
    
    static final String EXTRA_ANIMATION = "animation";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ImageView iv = new ImageView(this);
        iv.setScaleType(ScaleType.CENTER_CROP);
        
        if (getIntent().getBooleanExtra(EXTRA_ANIMATION, false))
        {
            iv.setImageResource(R.drawable.animation_next);
        }
        else
        {
            iv.setImageResource(R.drawable.animation_prev);
        }
        
        iv.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ActivityAnimationAdapter anim = new ActivityAnimationAdapter(new FolderAnimationEffect());
                anim.startActivity(AnimationActivity.this, 
                        new Intent(AnimationActivity.this, AnimationActivity.class)
                        .putExtra(EXTRA_ANIMATION, true));
            }
        });
        
        setContentView(iv);
    }
}