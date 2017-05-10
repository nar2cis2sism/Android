package demo.activity.test;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestOnTouch extends TestOnBase {
    
    MyViewGroup1 group1;
    MyViewGroup2 group2;
    MyView view;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        group1 = new MyViewGroup1(this);
        group2 = new MyViewGroup2(this);
        view = new MyView(this);
        view.setText("Touch me");
        view.setTextSize(40);
        
        group1.addView(group2, params);
        group2.addView(view, params);
        layout.addView(group1);
        
        final Button button1 = new Button(this);
        button1.setText("(MyViewGroup1)onInterceptTouchEvent：" + group1.onInterceptTouchEvent);
        button1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                button1.setText("(MyViewGroup1)onInterceptTouchEvent：" + (group1.onInterceptTouchEvent = !group1.onInterceptTouchEvent));
            }
        });
        layout.addView(button1);
        
        final Button button3 = new Button(this);
        button3.setText("(MyViewGroup2)onInterceptTouchEvent：" + group2.onInterceptTouchEvent);
        button3.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                button3.setText("(MyViewGroup2)onInterceptTouchEvent：" + (group2.onInterceptTouchEvent = !group2.onInterceptTouchEvent));
            }
        });
        layout.addView(button3);
        
        final Button button5 = new Button(this);
        button5.setText("(MyView)onTouchEvent：" + view.onTouchEvent);
        button5.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                button5.setText("(MyView)onTouchEvent：" + (view.onTouchEvent = !view.onTouchEvent));
            }
        });
        layout.addView(button5);
        
        final Button button4 = new Button(this);
        button4.setText("(MyViewGroup2)onTouchEvent：" + group2.onTouchEvent);
        button4.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                button4.setText("(MyViewGroup2)onTouchEvent：" + (group2.onTouchEvent = !group2.onTouchEvent));
            }
        });
        layout.addView(button4);
        
        final Button button2 = new Button(this);
        button2.setText("(MyViewGroup1)onTouchEvent：" + group1.onTouchEvent);
        button2.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                button2.setText("(MyViewGroup1)onTouchEvent：" + (group1.onTouchEvent = !group1.onTouchEvent));
            }
        });
        layout.addView(button2);
        
        setContentView(layout);
    }
    
    static String getAction(MotionEvent ev)
    {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            return "ACTION_DOWN";
        case MotionEvent.ACTION_MOVE:
            return "ACTION_MOVE";
        case MotionEvent.ACTION_UP:
            return "ACTION_UP";
        case MotionEvent.ACTION_CANCEL:
            return "ACTION_CANCEL";
        default:
            return String.valueOf(ev.getAction());
        }
    }
    
    private class MyViewGroup1 extends LinearLayout {
        
        public boolean onInterceptTouchEvent;
        public boolean onTouchEvent;

        public MyViewGroup1(Context context) {
            super(context);
        }
        
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            log("onInterceptTouchEvent:" + getAction(ev));
            return onInterceptTouchEvent;
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            log("onTouchEvent:" + getAction(event));
            return onTouchEvent;
        }
        
        private void log(String content)
        {
            TestOnTouch.super.log("(MyViewGroup1)" + content);
        }
    }
    
    private class MyViewGroup2 extends LinearLayout {
        
        public boolean onInterceptTouchEvent;
        public boolean onTouchEvent;

        public MyViewGroup2(Context context) {
            super(context);
        }
        
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            log("onInterceptTouchEvent:" + getAction(ev));
            return onInterceptTouchEvent;
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            log("onTouchEvent:" + getAction(event));
            return onTouchEvent;
        }
        
        private void log(String content)
        {
            TestOnTouch.super.log("(MyViewGroup2)" + content);
        }
    }
    
    private class MyView extends TextView {
        
        public boolean onTouchEvent;

        public MyView(Context context) {
            super(context);
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            log("onTouchEvent:" + getAction(event));
            return onTouchEvent;
        }
        
        private void log(String content)
        {
            TestOnTouch.super.log("(MyView)" + content);
        }
    }
}
