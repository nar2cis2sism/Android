package demo.activity.effect;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Toast;

import demo.android.R;
import demo.widget.BadgeView;

public class BadgeActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.badge);
        
        // *** default badge ***

        View target = findViewById(R.id.default_badge);
        BadgeView badge = new BadgeView(this, target);
        badge.setText("1");
        badge.show();

        // *** set position ***

        target = findViewById(R.id.Position);
        final BadgeView badge1 = new BadgeView(this, target);
        badge1.setText("12");
        badge1.setBadgePosition(Gravity.CENTER);
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                badge1.toggle();
            }
        });

        // *** badge/text size & color ***

        target = findViewById(R.id.Size_Color);
        final BadgeView badge2 = new BadgeView(this, target);
        badge2.setText("New!");
        badge2.setTextColor(Color.BLUE);
        badge2.setBadgeColor(Color.YELLOW);
        badge2.setTextSize(12);
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                badge2.toggle();
            }
        });

        // *** default animation ***

        target = findViewById(R.id.Animate_default);
        final BadgeView badge3 = new BadgeView(this, target);
        badge3.setText("84");
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                badge3.toggle(true);
            }
        });

        // *** custom animation ***

        target = (Button) findViewById(R.id.Animate_custom);
        final BadgeView badge4 = new BadgeView(this, target);
        badge4.setText("123");
        badge4.setBadgePosition(Gravity.LEFT | Gravity.TOP);
        badge4.setBadgeMargin(15, 10, 0, 0);
        badge4.setBadgeColor(Color.parseColor("#A4C639"));
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslateAnimation anim = new TranslateAnimation(-100, 0, 0, 0);
                anim.setInterpolator(new BounceInterpolator());
                anim.setDuration(1000);
                badge4.toggle(anim, null);
            }
        });

        // *** custom background ***

        target = findViewById(R.id.Custom);
        final BadgeView badge5 = new BadgeView(this, target);
        badge5.setText("37");
        badge5.setBackgroundResource(R.drawable.badge_bg);
        badge5.setTextSize(16);
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                badge5.toggle(true);
            }
        });

        // *** clickable badge ***

        target = (Button) findViewById(R.id.Clickable);
        final BadgeView badge6 = new BadgeView(this, target);
        badge6.setText("click me");
        badge6.setBadgeColor(Color.BLUE);
        badge6.setTextSize(16);
        badge6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BadgeActivity.this, "clicked badge", Toast.LENGTH_SHORT).show();
            }
        });
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                badge6.toggle();
            }
        });

        // *** increment ***

        target = (Button) findViewById(R.id.Increment);
        final BadgeView badge7 = new BadgeView(this, target);
        badge7.setText("0");
        target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (badge7.isShown())
                {
                    badge7.incrementAndGet(1);
                }
                else
                {
                    badge7.show();
                }
            }
        });
    }
}