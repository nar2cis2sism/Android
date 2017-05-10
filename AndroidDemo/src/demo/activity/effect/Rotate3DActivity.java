package demo.activity.effect;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import demo.android.R;

public class Rotate3DActivity extends Activity {
    
    Rotate3DButton1Animation anim1;
    boolean showIcons = false;
    ViewGroup rotate3d_icons;
    View rotate3d_button;
    View rotate3d_icon_plus;

    Rotate3DButton2Animation anim2;
    boolean showLevel2 = true,showLevel3 = true;
    ViewGroup rotate3d_level2,rotate3d_level3;
    View rotate3d_home,rotate3d_menu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rotate3d);
		
		anim1 = new Rotate3DButton1Animation(this);
		
		rotate3d_icons = (ViewGroup) findViewById(R.id.rotate3d_icons);
		rotate3d_button = findViewById(R.id.rotate3d_button);
		rotate3d_icon_plus = findViewById(R.id.rotate3d_icon_plus);
		
		rotate3d_button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!showIcons)
                {
                    anim1.showIconAnimation(rotate3d_icons, 300);
                    rotate3d_icon_plus.startAnimation(anim1.getRotateAnimation(0, -225, 300));
                }
                else
                {
                    anim1.hideIconAnimation(rotate3d_icons, 300);
                    rotate3d_icon_plus.startAnimation(anim1.getRotateAnimation(-225, 0, 300));
                }
                
                showIcons = !showIcons;
            }
        });
		
		for (int i = 0, count = rotate3d_icons.getChildCount(); i < count; i++)
        {
            final View icon = rotate3d_icons.getChildAt(i);
            final int index = i;
            icon.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (showIcons)
                    {
                        rotate3d_icon_plus.startAnimation(anim1.getRotateAnimation(-225, 0, 300));
                        icon.startAnimation(anim1.getIconZoomInAnimation(400));
                        for (int i = 0, count = rotate3d_icons.getChildCount(); i < count; i++)
                        {
                            if (i != index)
                            {
                                rotate3d_icons.getChildAt(i).startAnimation(anim1.getIconZoomOutAnimation(300));
                            }
                        }
                        
                        showIcons = false;
                    }
                }
            });
        }
		
		rotate3d_icon_plus.startAnimation(anim1.getRotateAnimation(0, 360, 200));
		
		anim2 = new Rotate3DButton2Animation();
		
		rotate3d_level2 = (ViewGroup) findViewById(R.id.rotate3d_level2);
        rotate3d_level3 = (ViewGroup) findViewById(R.id.rotate3d_level3);
        rotate3d_home = findViewById(R.id.rotate3d_home);
        rotate3d_menu = findViewById(R.id.rotate3d_menu);
        
        rotate3d_home.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!showLevel2)
                {
                    anim2.startRotateIn(rotate3d_level2, 500);
                }
                else
                {
                    if (showLevel3)
                    {
                        anim2.startRotateOut(rotate3d_level3, 0, 500);
                        anim2.startRotateOut(rotate3d_level2, 500, 500);
                        showLevel3 = !showLevel3;
                    }
                    else
                    {
                        anim2.startRotateOut(rotate3d_level2, 0, 500);
                    }
                }
                
                showLevel2 = !showLevel2;
            }
        });
        
        rotate3d_menu.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!showLevel3)
                {
                    anim2.startRotateIn(rotate3d_level3, 500);
                }
                else
                {
                    anim2.startRotateOut(rotate3d_level3, 0, 500);
                }
                
                showLevel3 = !showLevel3;
            }
        });
	}
	
	public static final class Rotate3DButton1Animation {
	    
	    private int xOffset;
	    private int yOffset;
	    
	    public Rotate3DButton1Animation(Context context) {
            float density = context.getResources().getDisplayMetrics().density;
            xOffset = (int) (10.667f * density);
            yOffset = -(int) (8.667f * density);
        }
	    
	    /**
	     * '+'号旋转动画
	     */
	    
	    public Animation getRotateAnimation(float fromDegrees, float toDegrees, long durationMillis)
	    {
	        RotateAnimation anim = new RotateAnimation(fromDegrees, toDegrees, 
	                Animation.RELATIVE_TO_SELF, 0.5f, 
	                Animation.RELATIVE_TO_SELF, 0.5f);
	        anim.setDuration(durationMillis);
	        anim.setFillAfter(true);
	        return anim;
	    }
	    
	    /**
	     * 显示图标动画
	     */
	    
	    public void showIconAnimation(ViewGroup icons, long durationMillis)
	    {
	        for (int i = 0, count = icons.getChildCount(); i < count; i++)
	        {
	            View icon = icons.getChildAt(i);
	            icon.setVisibility(View.VISIBLE);
	            icon.setClickable(true);
	            icon.setFocusable(true);
	            
	            MarginLayoutParams params = (MarginLayoutParams) icon.getLayoutParams();
	            Animation anim = new TranslateAnimation(params.rightMargin - xOffset, 0, 
	                    yOffset + params.bottomMargin, 0);
	            anim.setDuration(durationMillis);
	            anim.setFillAfter(true);
	            anim.setStartOffset(i * 100 / (count - 1));
	            anim.setInterpolator(new OvershootInterpolator(2));
	            icon.startAnimation(anim);
	        }
	    }
        
        /**
         * 隐藏图标动画
         */
        
        public void hideIconAnimation(ViewGroup icons, long durationMillis)
        {
            for (int i = 0, count = icons.getChildCount(); i < count; i++)
            {
                final View icon = icons.getChildAt(i);
                
                MarginLayoutParams params = (MarginLayoutParams) icon.getLayoutParams();
                Animation anim = new TranslateAnimation(0, params.rightMargin - xOffset, 
                        0, yOffset + params.bottomMargin);
                anim.setDuration(durationMillis);
                anim.setFillAfter(true);
                anim.setStartOffset((count - i) * 100 / (count - 1));
                anim.setAnimationListener(new AnimationListener() {
                    
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub
                        
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub
                        
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        icon.setVisibility(View.GONE);
                        icon.setClickable(false);
                        icon.setFocusable(false);
                    }
                });
                icon.startAnimation(anim);
            }
        }
        
        /**
         * 图标放大渐变消失的动画
         */
        
        public Animation getIconZoomInAnimation(long durationMillis)
        {
            AnimationSet set = new AnimationSet(true);
            
            Animation anim = new ScaleAnimation(1, 4, 1, 4, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            set.addAnimation(anim);
            anim = new AlphaAnimation(1, 0);
            set.addAnimation(anim);
            
            set.setDuration(durationMillis);
            set.setFillAfter(true);
            return set;
        }
        
        /**
         * 图标缩小消失的动画
         */
        
        public Animation getIconZoomOutAnimation(long durationMillis)
        {
            Animation anim = new ScaleAnimation(1, 0, 1, 0, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(durationMillis);
            anim.setFillAfter(true);
            return anim;
        }
	}
    
    public static final class Rotate3DButton2Animation {
        
        public void startRotateIn(ViewGroup level, long durationMillis)
        {
            level.setVisibility(View.VISIBLE);
            for (int i = 0, count = level.getChildCount(); i < count; i++)
            {
                View v = level.getChildAt(i);
                v.setVisibility(View.VISIBLE);
                v.setClickable(true);
                v.setFocusable(true);
            }
            
            RotateAnimation anim = new RotateAnimation(-180, 0, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 1.0f);
            anim.setDuration(durationMillis);
            anim.setFillAfter(true);
            level.startAnimation(anim);
        }
        
        public void startRotateOut(final ViewGroup level, long startOffset, long durationMillis)
        {
            RotateAnimation anim = new RotateAnimation(0, -180, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 1.0f);
            anim.setStartOffset(startOffset);
            anim.setDuration(durationMillis);
            anim.setFillAfter(true);
            anim.setAnimationListener(new AnimationListener() {
                
                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    level.setVisibility(View.GONE);
                    for (int i = 0, count = level.getChildCount(); i < count; i++)
                    {
                        View v = level.getChildAt(i);
                        v.setVisibility(View.GONE);
                        v.setClickable(false);
                        v.setFocusable(false);
                    }
                }
            });
            level.startAnimation(anim);
        }
    }
}