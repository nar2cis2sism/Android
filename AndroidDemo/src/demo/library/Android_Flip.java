package demo.library;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.flip.FlipViewController.ViewFlipListener;

import demo.android.R;

public class Android_Flip extends Activity {
    
    FlipViewController flipView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.android_flip);
        
        flipView = (FlipViewController) findViewById(R.id.flipView);
        flipView.setAdapter(new BaseAdapter() {
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                NumberTextView view;
                if (convertView == null)
                {
                    final Context context = parent.getContext();
                    view = new NumberTextView(context, position);
                    view.setTextSize(TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 128, context.getResources().getDisplayMetrics()));
                }
                else
                {
                    view = (NumberTextView) convertView;
                    view.setNumber(position);
                }
                
                return view;
            }
            
            @Override
            public long getItemId(int position) {
                return position;
            }
            
            @Override
            public Object getItem(int position) {
                return null;
            }
            
            @Override
            public int getCount() {
                return 10;
            }
        });
        flipView.setOnViewFlipListener(new ViewFlipListener() {
            
            @Override
            public void onViewFlipped(View view, int position) {
                Toast.makeText(view.getContext(), "Flipped to page " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        flipView.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        flipView.onPause();
    }
    
    private static class NumberTextView extends TextView {
        
        public NumberTextView(Context context, int number) {
            super(context);
            setNumber(number);
            setTextColor(Color.BLACK);
            setBackgroundColor(Color.WHITE);
            setGravity(Gravity.CENTER);
        }
        
        public void setNumber(int number) {
            setText(String.valueOf(number));
        }
    }
}