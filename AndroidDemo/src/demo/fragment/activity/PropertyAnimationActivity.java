package demo.fragment.activity;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import demo.android.R;
import demo.fragment.PropertyAnimationFragment;

public class PropertyAnimationActivity extends FragmentActivity {
    
    PropertyAnimationFragment leftFrag;
    PropertyAnimationFragment rightFrag;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.property_animation);

        final int MARGIN = 16;
        leftFrag  = new PropertyAnimationFragment(Color.parseColor("#FFA4C639"), 
                1f, MARGIN, MARGIN / 2, MARGIN, MARGIN, new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        leftFragAnimation();
                    }
                });
        rightFrag = new PropertyAnimationFragment(Color.parseColor("#FF58BAED"), 
                2f, MARGIN / 2, MARGIN, MARGIN, MARGIN, new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        rightFragAnimation();
                    }
                });
        
        getSupportFragmentManager().beginTransaction()
        .add(R.id.root, leftFrag)
        .add(R.id.root, rightFrag)
        .commit();
    }
    
    private void leftFragAnimation()
    {
        //rotate
        if (leftFrag != null)
        {
            ObjectAnimator.ofFloat(leftFrag.getView(), "rotationY", 0, 180).setDuration(500).start();
        }
    }
    
    private void rightFragAnimation()
    {
        //alpha
        if (rightFrag != null)
        {
            ObjectAnimator anim = ObjectAnimator.ofFloat(rightFrag.getView(), "alpha", 1, 0).setDuration(800);
            anim.setRepeatMode(ObjectAnimator.REVERSE);
            anim.setRepeatCount(1);
            anim.start();
        }
    }
}