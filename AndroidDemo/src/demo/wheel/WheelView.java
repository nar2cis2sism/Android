package demo.wheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import demo.android.R;

import java.util.LinkedList;
import java.util.List;

/**
 * 自定义滑动滚轮<br>
 * @author Daimon
 * @version 3.0
 * @since 8/21/2012
 */

public class WheelView extends View {
    
    /** Scrolling duration */
    private static final int SCROLLING_DURATION = 400;

    /** Minimum delta for scrolling */
    private static final int MIN_DELTA_FOR_SCROLLING = 1;

    /** Current value & label text color */
    private static final int VALUE_TEXT_COLOR = 0xF0F00000;

    /** Items text color */
    private static final int ITEMS_TEXT_COLOR = 0xFF000000;

    /** Top and bottom shadows colors */
    private static final int[] SHADOWS_COLORS = new int[]{0xFF111111, 0x00AAAAAA, 0x00AAAAAA};

    /** Additional items height (is added to standard text item height) */
    private static final int ADDITIONAL_ITEM_HEIGHT = 25;

    /** Text size */
    private static final int TEXT_SIZE = 40;

    /** Top and bottom items offset (to hide that) */
    private static final int ITEM_OFFSET = TEXT_SIZE / 5;

    /** Additional width for items layout */
    private static final int ADDITIONAL_ITEMS_SPACE = 20;

    /** Label offset */
    private static final int LABEL_OFFSET = 8;

    /** Left and right padding value */
    private static final int PADDING = 10;

    /** Default count of visible items */
    private static final int DEF_VISIBLE_ITEMS = 5;

    //Wheel Values
    private WheelAdapter adapter;
    private int currentItem = 0;
    
    //Widths
    private int itemsWidth = 0;
    private int labelWidth = 0;

    //Count of visible items
    private int visibleItems = DEF_VISIBLE_ITEMS;
    
    //Item height
    private int itemHeight = 0;

    //Text paints
    private TextPaint itemsPaint;
    private TextPaint valuePaint;

    //Layouts
    private StaticLayout itemsLayout;
    private StaticLayout labelLayout;
    private StaticLayout valueLayout;

    //Label & background
    private String label;
    private Drawable centerDrawable;

    //Shadows drawables
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    //Scrolling
    private boolean isScrollingPerformed; 
    private int scrollingOffset;

    //Scrolling animation
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;

    //Cyclic
    private boolean isCyclic = false;
    
    //Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();

    public WheelView(Context context) {
        super(context);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context)
    {
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
        
        scroller = new Scroller(context);
    }

    public WheelAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(WheelAdapter adapter) {
        this.adapter = adapter;
        invalidateLayouts();
        invalidate();
    }
    
    /**
     * Set the specified scrolling interpolator
     */
    
    public void setInterpolator(Interpolator interpolator)
    {
        scroller.forceFinished(true);
        scroller = new Scroller(getContext(), interpolator);
    }
    
    public int getVisibleItems() {
        return visibleItems;
    }

    public void setVisibleItems(int visibleItems) {
        this.visibleItems = visibleItems;
        invalidate();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (this.label == null && label != null 
        || (this.label != null && !this.label.equals(label)))
        {
            this.label = label;
            labelLayout = null;
            invalidate();
        }
    }
    
    /**
     * Adds wheel changing listener
     */
    
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     */
    
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }
    
    /**
     * Notifies changing listeners
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    
    private void notifyChangingListeners(int oldValue, int newValue)
    {
        for (OnWheelChangedListener listener : changingListeners)
        {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds wheel scrolling listener
     */
    
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     */
    
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }
    
    /**
     * Notifies listeners about starting scrolling
     */
    
    private void notifyScrollingListenersAboutStart()
    {
        for (OnWheelScrollListener listener : scrollingListeners)
        {
            listener.onScrollingStarted(this);
        }
    }
    
    /**
     * Notifies listeners about finishing scrolling
     */
    
    private void notifyScrollingListenersAboutFinish()
    {
        for (OnWheelScrollListener listener : scrollingListeners)
        {
            listener.onScrollingFinished(this);
        }
    }

    public int getCurrentItem() {
        return currentItem;
    }
    
    /**
     * Sets the current item w/o animation
     */
    
    public void setCurrentItem(int currentItem) {
        setCurrentItem(currentItem, false);
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     * @param index the item index
     * @param animate the animation flag
     */

    public void setCurrentItem(int index, boolean animate) {
        if (adapter == null || adapter.getCount() == 0)
        {
            return;
        }
        
        int count = adapter.getCount();
        if (index < 0 || index >= count)
        {
            if (isCyclic)
            {
                while (index < 0)
                {
                    index += count;
                }
                
                index %= count;
            }
            else
            {
                return;
            }
        }
        
        if (index != currentItem)
        {
            if (animate)
            {
                scroll(index - currentItem, SCROLLING_DURATION);
            }
            else
            {
                invalidateLayouts();
                
                notifyChangingListeners(currentItem, currentItem = index);
                
                invalidate();
            }
        }
    }

    public boolean isCyclic() {
        return isCyclic;
    }

    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
        invalidate();
        invalidateLayouts();
    }
    
    /**
     * Scroll the wheel
     */
    
    public void scroll(int itemsToScroll, int duration)
    {
        scroller.forceFinished(true);
        
        lastScrollY = scrollingOffset;
        int offset = itemsToScroll * getItemHeight();
        
        scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, duration);
        setNextMessage(MESSAGE_SCROLL);
        
        startScroll();
    }

    private void invalidateLayouts()
    {
        itemsLayout = null;
        valueLayout = null;
        scrollingOffset = 0;
    }
    
    /**
     * Returns height of wheel item
     */
    
    private int getItemHeight()
    {
        if (itemHeight != 0)
        {
            return itemHeight;
        }
        else if (itemsLayout != null && itemsLayout.getLineCount() > 2)
        {
            return itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
        }
        else
        {
            return getHeight() / visibleItems;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    
        int width = calculateWidth(widthSize, widthMode);
        int height;
        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        }
        else
        {
            height = getDesiredHeight();
            if (heightMode == MeasureSpec.AT_MOST)
            {
                height = Math.min(height, heightSize);
            }
        }
    
        setMeasuredDimension(width, height);
    }

    /**
     * Calculates wheel width and creates text layouts
     */
    
    private int calculateWidth(int widthSize, int mode)
    {
        initResourcesIfNecessary();
        
        int maxLength = getMaxTextLength();
        if (maxLength > 0)
        {
            float textWidth = FloatMath.ceil(Layout.getDesiredWidth("0", itemsPaint));
            itemsWidth = (int) (maxLength * textWidth);
        }
        else
        {
            itemsWidth = 0;
        }
        
        itemsWidth += ADDITIONAL_ITEMS_SPACE;
        
        if (!TextUtils.isEmpty(label))
        {
            labelWidth = (int) FloatMath.ceil(Layout.getDesiredWidth(label, valuePaint));
        }
        else
        {
            labelWidth = 0;
        }

        int width;
        boolean recalculate = false;
        if (mode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
            recalculate = true;
        }
        else
        {
            width = itemsWidth + labelWidth + 2 * PADDING;
            if (labelWidth > 0)
            {
                width += LABEL_OFFSET;
            }
            
            //Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());
            
            if (mode == MeasureSpec.AT_MOST && width > widthSize)
            {
                width = widthSize;
                recalculate = true;
            }
        }
        
        if (recalculate)
        {
            //recalculate width
            int pureWidth = width - LABEL_OFFSET - 2 * PADDING;
            if (pureWidth <= 0)
            {
                itemsWidth = labelWidth = 0;
            }
            
            if (labelWidth > 0)
            {
                double newItemsWidth = (double) itemsWidth * pureWidth / (itemsWidth + labelWidth);
                itemsWidth = (int) newItemsWidth;
                labelWidth = pureWidth - itemsWidth;
            }
            else
            {
                //no label
                itemsWidth = pureWidth + LABEL_OFFSET;
            }
        }
        
        if (itemsWidth > 0)
        {
            createLayouts(itemsWidth, labelWidth);
        }
        
        return width;
    }

    /**
     * Initializes resources
     */
    
    private void initResourcesIfNecessary()
    {
        if (itemsPaint == null)
        {
            itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            itemsPaint.setTextSize(TEXT_SIZE);
        }
        
        if (valuePaint == null)
        {
            valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
            valuePaint.setTextSize(TEXT_SIZE);
            valuePaint.setShadowLayer(0.1f, 0, 0.1f, 0xFFC0C0C0);
        }
        
        if (centerDrawable == null)
        {
            centerDrawable = getResources().getDrawable(R.drawable.wheel_val);
        }
        
        if (topShadow == null)
        {
            topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }
        
        if (bottomShadow == null)
        {
            bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }
        
        setBackgroundResource(R.drawable.wheel_bg);
    }

    /**
     * Returns the max item length that can be present
     * @return the max length
     */
    
    private int getMaxTextLength()
    {
        WheelAdapter adapter = this.adapter;
        if (adapter == null)
        {
            return 0;
        }
        
        int maxLength = adapter.getMaximumLength();
        if (maxLength > 0)
        {
            return maxLength;
        }
        
        maxLength = 0;
        int addItems = visibleItems / 2;
        for (int i = Math.max(currentItem - addItems, 0), 
             size = Math.min(currentItem + visibleItems, adapter.getCount()); i < size; i++)
        {
            String text = adapter.getItem(i);
            if (!TextUtils.isEmpty(text))
            {
                maxLength = Math.max(maxLength, text.length());
            }
        }
    
        return maxLength;
    }

    /**
     * Creates layouts
     * @param widthItems width of items layout
     * @param widthLabel width of label layout
     */
    
    private void createLayouts(int widthItems, int widthLabel)
    {
        if (itemsLayout == null || itemsLayout.getWidth() > widthItems)
        {
            itemsLayout = new StaticLayout(buildText(isScrollingPerformed), itemsPaint, widthItems, 
                    widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER, 
                    1, ADDITIONAL_ITEM_HEIGHT, false);
        }
        else
        {
            itemsLayout.increaseWidthTo(widthItems);
        }
        
        if (!isScrollingPerformed && (valueLayout == null || valueLayout.getWidth() > widthItems))
        {
            String text = adapter != null ? adapter.getItem(currentItem) : null;
            valueLayout = new StaticLayout(text != null ? text : "", valuePaint, widthItems, 
                    widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER, 
                    1, ADDITIONAL_ITEM_HEIGHT, false);
        }
        else if (isScrollingPerformed)
        {
            valueLayout = null;
        }
        else
        {
            valueLayout.increaseWidthTo(widthItems);
        }
        
        if (widthLabel > 0)
        {
            if (labelLayout == null || labelLayout.getWidth() > widthLabel)
            {
                labelLayout = new StaticLayout(label, valuePaint, widthLabel, 
                        Layout.Alignment.ALIGN_NORMAL, 
                        1, ADDITIONAL_ITEM_HEIGHT, false);
            }
            else
            {
                labelLayout.increaseWidthTo(widthLabel);
            }
        }
        else
        {
            labelLayout = null;
        }
    }
    
    /**
     * Builds text depending on current value
     */
    
    private String buildText(boolean useCurrentValue)
    {
        StringBuilder sb = new StringBuilder();
        int addItems = visibleItems / 2 + 1;
        for (int i = currentItem - addItems, size = currentItem + addItems; i <= size; i++)
        {
            if (useCurrentValue || i != currentItem)
            {
                String text = getTextItem(i);
                if (text != null)
                {
                    sb.append(text);
                }
            }
            
            if (i < size)
            {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns text item by index
     */
    
    private String getTextItem(int index)
    {
        if (adapter == null || adapter.getCount() == 0)
        {
            return null;
        }
        
        int count = adapter.getCount();
        if ((index < 0 || index >= count) && !isCyclic)
        {
            return null;
        }
        
        while (index < 0)
        {
            index += count;
        }
        
        index %= count;
        return adapter.getItem(index);
    }

    /**
     * Calculates wheel height
     */
    
    private int getDesiredHeight()
    {
        int height = getItemHeight() * visibleItems - ITEM_OFFSET * 2 - ADDITIONAL_ITEM_HEIGHT;
        //Check against our minimum height
        height = Math.max(height, getSuggestedMinimumHeight());
        
        return height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (itemsLayout == null)
        {
            if (itemsWidth == 0)
            {
                calculateWidth(getWidth(), MeasureSpec.EXACTLY);
            }
            else
            {
                createLayouts(itemsWidth, labelWidth);
            }
        }
        
        if (itemsWidth > 0)
        {
            canvas.save();
            
            //Skip padding space and hide a part of top and bottom items
            canvas.translate(PADDING, -ITEM_OFFSET);
            drawItems(canvas);
            drawValue(canvas);
            
            canvas.restore();
        }

        drawCenterRect(canvas);
        drawShadows(canvas);
    }
    
    /**
     * Draws items
     */
    
    private void drawItems(Canvas canvas)
    {
        canvas.save();
        
        int top = itemsLayout.getLineTop(1);
        canvas.translate(0, scrollingOffset - top);
        
        itemsPaint.setColor(ITEMS_TEXT_COLOR);
        itemsPaint.drawableState = getDrawableState();
        itemsLayout.draw(canvas);
        
        canvas.restore();
    }
    
    /**
     * Draws value and label layout
     */
    
    private void drawValue(Canvas canvas)
    {
        valuePaint.setColor(VALUE_TEXT_COLOR);
        valuePaint.drawableState = getDrawableState();
    
        Rect bounds = new Rect();
        itemsLayout.getLineBounds(visibleItems / 2, bounds);
    
        //draw label
        if (labelLayout != null)
        {
            canvas.save();
            
            canvas.translate(itemsLayout.getWidth() + LABEL_OFFSET, bounds.top);
            labelLayout.draw(canvas);
            
            canvas.restore();
        }
    
        //draw current value
        if (valueLayout != null)
        {
            canvas.save();
            
            canvas.translate(0, bounds.top + scrollingOffset);
            valueLayout.draw(canvas);
            
            canvas.restore();
        }
    }

    /**
     * Draws rectangle for current value
     */
    
    private void drawCenterRect(Canvas canvas)
    {
        int center = getHeight() / 2;
        int offset = getItemHeight() / 2;
        centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        centerDrawable.draw(canvas);
    }

    /**
     * Draws shadows on top and bottom of wheel
     */
    
    private void drawShadows(Canvas canvas)
    {
        topShadow.setBounds(0, 0, getWidth(), getHeight() / visibleItems);
        topShadow.draw(canvas);

        bottomShadow.setBounds(0, getHeight() - getHeight() / visibleItems, getWidth(), getHeight());
        bottomShadow.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (adapter == null)
        {
            return true;
        }
        
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP)
        {
            justify();
        }
        
        return true;
    }
    
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener()
    {
        public boolean onDown(MotionEvent e) {
            if (isScrollingPerformed)
            {
                scroller.forceFinished(true);
                clearMessages();
                return true;
            }
            
            return false;
        };
        
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            startScroll();
            doScroll((int) -distanceY);
            return true;
        };
        
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lastScrollY = currentItem * getItemHeight() + scrollingOffset;
            int maxY = isCyclic ? 0x7FFFFFFF : adapter.getCount() * getItemHeight();
            int minY = isCyclic ? -maxY : 0;
            scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        };
    };
    
    /** Messages **/
    private static final int MESSAGE_SCROLL     = 0;
    private static final int MESSAGE_JUSTIFY    = 1;
    
    private final Handler scrollHandler = new Handler()
    {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int dy = lastScrollY - currY;
            lastScrollY = currY;
            if (dy != 0)
            {
                doScroll(dy);
            }
            
            //scrolling is not finished when it comes to final Y
            //so, finish it manually
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING)
            {
                scroller.forceFinished(true);
            }
            
            if (!scroller.isFinished())
            {
                sendEmptyMessage(msg.what);
            }
            else if (msg.what == MESSAGE_SCROLL)
            {
                justify();
            }
            else
            {
                finishScroll();
            }
        };
    };
    
    /**
     * Set next message to queue. Clears queue before.
     */
    
    private void setNextMessage(int message)
    {
        clearMessages();
        scrollHandler.sendEmptyMessage(message);
    }
    
    /**
     * Clears messages from queue
     */
    
    private void clearMessages()
    {
        scrollHandler.removeMessages(MESSAGE_SCROLL);
        scrollHandler.removeMessages(MESSAGE_JUSTIFY);
    }
    
    private void startScroll()
    {
        if (!isScrollingPerformed)
        {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
    }
    
    private void finishScroll()
    {
        if (isScrollingPerformed)
        {
            notifyScrollingListenersAboutFinish();
            isScrollingPerformed = false;
        }
        
        invalidateLayouts();
        invalidate();
    }
    
    /**
     * Scroll the wheel
     * @param delta the scrolling value
     */
    
    private void doScroll(int delta)
    {
        scrollingOffset += delta;
        
        int index = scrollingOffset / getItemHeight();
        int pos = currentItem - index;
        int count = adapter.getCount();
        if (isCyclic && count > 0)
        {
            //fix position by rotating
            while (pos < 0)
            {
                pos += count;
            }
            
            pos %= count;
        }
        else if (isScrollingPerformed)
        {
            if (pos < 0)
            {
                index = currentItem;
                pos = 0;
            }
            else if (pos >= count)
            {
                index = currentItem - count + 1;
                pos = count - 1;
            }
        }
        else
        {
            //fix position
            pos = Math.min(Math.max(pos, 0), count - 1);
        }

        int offset = scrollingOffset;
        if (pos != currentItem)
        {
            setCurrentItem(pos, false);
        }
        else
        {
            invalidate();
        }
        
        //update offset
        scrollingOffset = offset - index * getItemHeight();
        if (scrollingOffset > getHeight())
        {
            scrollingOffset = scrollingOffset % getHeight() + getHeight();
        }
    }
    
    /**
     * justify the wheel
     */
    
    private void justify()
    {
        if (adapter == null)
        {
            return;
        }
        
        lastScrollY = 0;
        int offset = scrollingOffset;
        int itemHeight = getItemHeight();
        boolean needToIncrease = offset > 0 ? currentItem < adapter.getCount() : currentItem > 0;
        if ((isCyclic || needToIncrease) && Math.abs((float) offset) > (float) itemHeight / 2)
        {
            if (offset < 0)
            {
                offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
            }
            else
            {
                offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
            }
        }
        
        if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING)
        {
            scroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION);
            setNextMessage(MESSAGE_JUSTIFY);
        }
        else
        {
            finishScroll();
        }
    }
}