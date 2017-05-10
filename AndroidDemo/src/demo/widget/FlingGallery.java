package demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Switch view with gesture
 * @author Daimon
 * @version 3.0
 * @since 5/2/2012
 */

public class FlingGallery extends FrameLayout {
	
	/***** Constants *****/
	
	private final float FLING_MIN_DISTANCE = 120.0f;
	private final float FLING_MAX_OFF_PATH = 250.0f;
	private final float FLING_MIN_VELOCITY = 400.0f;
	
	/***** Properties *****/
	
	private int viewPaddingWidth = 0;
	private int animationDuration = 250;
	private float snapRatio = 0.5f;
	private boolean isGalleryCircular = true;
	
	/***** Members *****/
	
	private int galleryWidth;
	private boolean isTouched;
	private boolean isDragging;
	private float currentOffset;
	private long scrollTimestamp;
	private int flingDirection;
	private int currentPosition;
	private int currentViewNumber;
	
	private Adapter adapter;
	private FlingGalleryView[] views;
	private FlingGalleryAnimation animation;
	private GestureDetector detector;
	private Interpolator decelerateInterpolator;
	private FlingGalleryListener listener;

	public FlingGallery(Context context) {
		super(context);
		init(context);
	}

	public FlingGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FlingGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		views = new FlingGalleryView[3];
		views[0] = new FlingGalleryView(0, this);
		views[1] = new FlingGalleryView(1, this);
		views[2] = new FlingGalleryView(2, this);

		animation = new FlingGalleryAnimation();
		detector = new GestureDetector(context, new FlingGalleryDetector());
		decelerateInterpolator = AnimationUtils.loadInterpolator(context, android.R.anim.decelerate_interpolator);
	}

	public void setPaddingWidth(int viewPaddingWidth)
	{
		this.viewPaddingWidth = viewPaddingWidth;
	}

	public void setAnimationDuration(int animationDuration)
	{
		this.animationDuration = animationDuration;
	}
	
	public void setSnapRatio(float snapRatio)
	{
		this.snapRatio = snapRatio;
	}

	public void setGalleryCircular(boolean isGalleryCircular) 
	{
		if (this.isGalleryCircular != isGalleryCircular)
		{
			this.isGalleryCircular = isGalleryCircular;
	
			if (currentPosition == getFirstPosition())
			{
				//We need to reload the view immediately to the left to change it to circular view or blank
		    	views[getPrevViewNumber(currentViewNumber)].recycleView(getPrevPosition(currentPosition));			
			}
	
			if (currentPosition == getLastPosition())
			{
				//We need to reload the view immediately to the right to change it to circular view or blank
				views[getNextViewNumber(currentViewNumber)].recycleView(getNextPosition(currentPosition));			
			}
		}
	}

	public void setAdapter(Adapter adapter)
    {
    	this.adapter = adapter;
    	currentPosition = 0;
        currentViewNumber = 0;

        //Load the initial views from adapter
        views[0].recycleView(currentPosition);
        views[1].recycleView(getNextPosition(currentPosition));
        views[2].recycleView(getPrevPosition(currentPosition));

    	//Position views at correct starting offsets
        views[0].setOffset(0, 0, currentViewNumber);
        views[1].setOffset(0, 0, currentViewNumber);
        views[2].setOffset(0, 0, currentViewNumber);
    }
	
	public void setFlingGalleryListener(FlingGalleryListener listener)
	{
		this.listener = listener;
	}

	public int getGalleryCount()
	{
		return adapter == null ? 0 : adapter.getCount();
	}
	
	int getFirstPosition()
	{
		return 0;
	}

	int getLastPosition()
	{
		return getGalleryCount() == 0 ? 0 : getGalleryCount() - 1;
	}

	private int getPrevPosition(int relativePosition)
	{
		int prevPosition = relativePosition - 1;

		if (prevPosition < getFirstPosition())
		{
			prevPosition = isGalleryCircular ? getLastPosition() : getFirstPosition() - 1;
		}

		return prevPosition;
	}

	private int getNextPosition(int relativePosition)
	{
		int nextPosition = relativePosition + 1;

		if (nextPosition > getLastPosition())
		{
			nextPosition = isGalleryCircular ? getFirstPosition() : getLastPosition() + 1;
		}

		return nextPosition;
	}

	int getPrevViewNumber(int relativeViewNumber)
	{
		return relativeViewNumber == 0 ? 2 : relativeViewNumber - 1;
	}

	int getNextViewNumber(int relativeViewNumber)
	{
		return relativeViewNumber == 2 ? 0 : relativeViewNumber + 1;
	}

	int getViewOffset(int viewNumber, int relativeViewNumber)
	{
		//Determine width including configured padding width
		int offsetWidth = galleryWidth + viewPaddingWidth;

		//Position the previous view one measured width to left
		if (viewNumber == getPrevViewNumber(relativeViewNumber))
		{
			return offsetWidth;
		}
		//Position the next view one measured width to the right
		else if (viewNumber == getNextViewNumber(relativeViewNumber))
		{
			return -offsetWidth;
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//Calculate our view width
		galleryWidth = right - left;
		if (changed)
		{
			//Position views at correct starting offsets
	        views[0].setOffset(0, 0, currentViewNumber);
	        views[1].setOffset(0, 0, currentViewNumber);
	        views[2].setOffset(0, 0, currentViewNumber);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			movePrevious();
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			moveNext();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean consumed = detector.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			if (isTouched || isDragging)
			{
				processScrollSnap();
				processGesture();
			}
		}
		
		return consumed;
	}

	public int getPosition()
	{
		return currentPosition;
	}

	public FlingGallery movePrevious()
	{
		//Slide to previous view
		flingDirection = 1;
		processGesture();
		return this;
	}

	public FlingGallery moveNext()
	{
		//Slide to next view
		flingDirection = -1;
		processGesture();
		return this;
	}

	void processGesture()
	{
		int newViewNumber = currentViewNumber;
		int reloadViewNumber = 0;
		int reloadPosition = 0;

		isTouched = false;
		isDragging = false;

		if (flingDirection > 0)
		{
			if (currentPosition > getFirstPosition() || isGalleryCircular)
			{
				//Determine previous view and outgoing view to recycle
				newViewNumber = getPrevViewNumber(currentViewNumber);
				currentPosition = getPrevPosition(currentPosition);
				reloadViewNumber = getNextViewNumber(currentViewNumber); 
				reloadPosition = getPrevPosition(currentPosition);
			}
		}
		else if (flingDirection < 0)
		{
			if (currentPosition < getLastPosition() || isGalleryCircular)
			{
				//Determine the next view and outgoing view to recycle
				newViewNumber = getNextViewNumber(currentViewNumber);
				currentPosition = getNextPosition(currentPosition);
				reloadViewNumber = getPrevViewNumber(currentViewNumber);
				reloadPosition = getNextPosition(currentPosition);
			}
		}

		if (newViewNumber != currentViewNumber)
		{
			currentViewNumber = newViewNumber; 

			//Reload outgoing view from adapter in new position
			views[reloadViewNumber].recycleView(reloadPosition);
		}

		//Ensure input focus on the current view
		views[currentViewNumber].requestFocus();

		//Run the slide animations for view transitions
		animation.prepareAnimation(currentViewNumber);
		startAnimation(animation);

		//Reset fling state
		flingDirection = 0;
	}

	private void processScrollSnap()
	{
		//Snap to next view if scrolled passed snap position
		float rollEdgeWidth = galleryWidth * snapRatio;
		int rollOffset = galleryWidth - (int) rollEdgeWidth;
		int currentOffset = views[currentViewNumber].getCurrentOffset();

		if (currentOffset <= -rollOffset)
		{
			//Snap to previous view
			flingDirection = 1;
		}
		else if (currentOffset >= rollOffset)
		{
			//Snap to next view
			flingDirection = -1;
		}
	}
	
	private class FlingGalleryView {
		
		private int viewNumber;
		
		private FrameLayout invalidLayout;
		private LinearLayout internalLayout;
		
		private View externalView;
		
		public FlingGalleryView(int viewNumber, FrameLayout parentLayout) {
			this.viewNumber = viewNumber;

			//Invalid layout is used when outside gallery
			invalidLayout = new FrameLayout(getContext());

			//Internal layout is permanent for duration
			internalLayout = new LinearLayout(getContext());
			internalLayout.setLayoutParams(new LinearLayout.LayoutParams(
	                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			parentLayout.addView(internalLayout);
		}
		
		public void recycleView(int newPosition)
		{
			if (externalView != null)
			{
				internalLayout.removeView(externalView);
			}

			if (adapter != null)
			{
				if (newPosition >= getFirstPosition() && newPosition <= getLastPosition())
				{
					externalView = adapter.getView(newPosition, externalView, internalLayout);
				}
				else
				{
					externalView = invalidLayout;
				}
			}

			if (externalView != null)
			{
				internalLayout.addView(externalView, new LinearLayout.LayoutParams( 
	                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}
		}

		public void setOffset(int xOffset, int yOffset, int relativeViewNumber)
		{
			//Scroll the target view relative to its own position relative to currently displayed view
			internalLayout.scrollTo(getViewOffset(viewNumber, relativeViewNumber) + xOffset, yOffset);
		}
		
		public int getCurrentOffset()
		{
			//Return the current scroll position
			return internalLayout.getScrollX();
		}

		public void requestFocus()
		{
			internalLayout.requestFocus();
		}
	}
	
	private class FlingGalleryAnimation extends Animation {
		
		private boolean isAnimationInProgress;
		private int relativeViewNumber;
		private int initialOffset;
		private int targetOffset;
		private int targetDistance;
		private boolean isPositionChanged;
 
    	public void prepareAnimation(int relativeViewNumber)
    	{
    		//If we are animating relative to a new view
    		if (isPositionChanged = this.relativeViewNumber != relativeViewNumber)
    		{
				if (isAnimationInProgress)
				{
					//We only have three views so if requested again to animate in same direction we must snap 
					int newDirection = relativeViewNumber == getPrevViewNumber(this.relativeViewNumber) ? 1 : -1;
	    			int animDirection = targetDistance < 0 ? 1 : -1;

	    			//If animation in same direction
	    			if (animDirection == newDirection)
	    			{
		        		//Ran out of time to animate so snap to the target offset
		        		views[0].setOffset(targetOffset, 0, relativeViewNumber);
		                views[1].setOffset(targetOffset, 0, relativeViewNumber);
		                views[2].setOffset(targetOffset, 0, relativeViewNumber);
	    			}
				}
	
				//Set relative view number for animation
				this.relativeViewNumber = relativeViewNumber;
    		}

			//Note: In this implementation the targetOffset will always be zero
    		//as we are centering the view; but we include the calculations of
			//targetOffset and targetDistance for use in future implementations

    		initialOffset = views[relativeViewNumber].getCurrentOffset();
    		targetOffset = getViewOffset(relativeViewNumber, relativeViewNumber);
    		targetDistance = targetOffset - initialOffset;

			//Configure base animation properties
			setDuration(animationDuration);
			setInterpolator(decelerateInterpolator);

			//Start/continued animation
			isAnimationInProgress = true;
		}
    	
    	@Override
    	protected void applyTransformation(float interpolatedTime,
    			Transformation t) {
    		//Ensure interpolatedTime does not over-shoot then calculate new offset
        	interpolatedTime = interpolatedTime > 1.0f ? 1.0f : interpolatedTime;
			int offset = initialOffset + (int) (targetDistance * interpolatedTime);

			for (int viewNumber = 0; viewNumber < views.length; viewNumber++)
			{
				//Only need to animate the visible views as the other view will always be off-screen
				if ((targetDistance > 0 && viewNumber != getNextViewNumber(relativeViewNumber)) ||
					(targetDistance < 0 && viewNumber != getPrevViewNumber(relativeViewNumber)))
				{
					views[viewNumber].setOffset(offset, 0, relativeViewNumber);
				}
			}
    	}
    	
    	@Override
    	public boolean getTransformation(long currentTime,
    			Transformation outTransformation) {
    		if (!super.getTransformation(currentTime, outTransformation))
    		{
    			//Perform final adjustment to offsets to cleanup animation
    			views[0].setOffset(targetOffset, 0, relativeViewNumber);
    			views[1].setOffset(targetOffset, 0, relativeViewNumber);
    			views[2].setOffset(targetOffset, 0, relativeViewNumber);
    			
    			//Reached the animation target
    			isAnimationInProgress = false;
    			
    			if (listener != null)
    			{
    				listener.onFling(isPositionChanged);
    			}
    			
    			return false;
    		}
    		
        	//Cancel if the screen touched
        	if (isTouched || isDragging)
        	{
        		//Note that at this point we still consider ourselves to be animating
        		//because we have not yet reached the target offset; it's just that the
        		//user has temporarily interrupted the animation with a touch gesture

        		return false;
        	}

        	return true;
    	}
	}
	
	private class FlingGalleryDetector extends SimpleOnGestureListener {
		
		@Override
		public boolean onDown(MotionEvent e) {
			//Stop animation
    		isTouched = true;

    		//Reset fling state
    		flingDirection = 0;
    		
            return true;
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (!isDragging)
			{
				//Reconfigure scroll
				isDragging = true;
				flingDirection = 0;
				scrollTimestamp = System.currentTimeMillis();
				currentOffset = views[currentViewNumber].getCurrentOffset();
			}
		
		    float maxVelocity = galleryWidth / (animationDuration / 1000.0f);
			long timestampDelta = System.currentTimeMillis() - scrollTimestamp;
			float maxScrollDelta = maxVelocity * (timestampDelta / 1000.0f); 
			float currentScrollDelta = e1.getX() - e2.getX();
		
			if (currentScrollDelta < -maxScrollDelta)
			{
				currentScrollDelta = -maxScrollDelta;
			}
			else if (currentScrollDelta > maxScrollDelta)
			{
				currentScrollDelta = maxScrollDelta;
			}
			
			int scrollOffset = Math.round(currentOffset + currentScrollDelta);
		
			//We can't scroll more than the width of our own frame layout
			if (scrollOffset < -galleryWidth)
			{
				scrollOffset = -galleryWidth;
			}
			else if (scrollOffset > galleryWidth)
			{
				scrollOffset = galleryWidth;
			}
			
			views[0].setOffset(scrollOffset, 0, currentViewNumber);
			views[1].setOffset(scrollOffset, 0, currentViewNumber);
			views[2].setOffset(scrollOffset, 0, currentViewNumber);
		
		    return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) <= FLING_MAX_OFF_PATH)
			{
				if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY)
				{
					//slide left
		        	moveNext();
				}
				else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY)
				{
					//slide right
		        	movePrevious();
				}
			}
		
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			//Finalise scrolling
    		flingDirection = 0;
            processGesture();
		}
	}
	
	public static interface FlingGalleryListener {
		
		public void onFling(boolean isPositionChanged);
	}
}