package demo.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import demo.android.R;

public class PropertyAnimationFragment extends Fragment {
	
	private View view;
	private int color;
	private float weight;
	private int marginLeft, marginRight, marginTop, marginBottom;
	private OnClickListener listener;
	
	public PropertyAnimationFragment() {
		//make sure class name exists, is public, and has an empty constructor that is public
	}
	
	public PropertyAnimationFragment(int color, float weight, 
			int marginLeft, int marginRight, int marginTop, int marginBottom, 
			OnClickListener listener) {
		this.color = color;
		this.weight = weight;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.listener = listener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		view = new View(getActivity());
		
		GradientDrawable background = (GradientDrawable) getResources().getDrawable(R.drawable.property_animation);
		background.setColor(color);
		
		view.setBackgroundDrawable(background);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT, weight);
		params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		
		view.setLayoutParams(params);
		
		view.setOnClickListener(listener);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return view;
	}
}