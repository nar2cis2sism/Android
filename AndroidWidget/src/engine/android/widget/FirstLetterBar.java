package engine.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

/**
 * 快速搜索控件
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public class FirstLetterBar extends View {
    
    private static final String INVALID_LETTER = "\u00b7";          // "·"
    
    private String[] firstLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", 
                                     "J", "K", "L", "M", "N", "O", "P", "Q", "R", 
                                     "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    
    private SparseArray<Drawable> replaceMap;

    private boolean showBg;
    private Drawable background;
    
    private String[] showLetters;
    private int selectedIndex = -1;

    private Paint paint;
    private int textColorHighlight;
    
    private int topMargin;
    
    private OnFirstLetterChangedListener listener;

    public FirstLetterBar(Context context) {
        this(context, null);
    }

    public FirstLetterBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.FirstLetterBar);
    }
    
    public FirstLetterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FirstLetterBar, 
                defStyleAttr, R.style.FirstLetterBar);
        background = a.getDrawable(R.styleable.FirstLetterBar_background);
        int textSize = a.getDimensionPixelSize(R.styleable.FirstLetterBar_textSize, 0);
        int textColor = a.getColor(R.styleable.FirstLetterBar_textColor, 0);
        textColorHighlight = a.getColor(R.styleable.FirstLetterBar_textColorHighlight, 0);
        
        a.recycle();
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
    }
    
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        setRawTextSize(TypedValue.applyDimension(
                unit, size, r.getDisplayMetrics()));
    }

    private void setRawTextSize(float size) {
        if (size != paint.getTextSize())
        {
            paint.setTextSize(size);
            if (showLetters != null)
            {
                requestLayout();
                invalidate();
            }
        }
    }
    
    public void setTextColor(int color) {
        if (color != paint.getColor())
        {
            paint.setColor(color);
            if (showLetters != null) invalidate();
        }
    }
    
    public void setTextColorHighlight(int color) {
        if (color != textColorHighlight)
        {
            textColorHighlight = color;
            if (showLetters != null) invalidate();
        }
    }
    
    public void setFirstLetters(String[] firstLetters) {
        this.firstLetters = firstLetters;
        if (showLetters != null)
        {
            requestLayout();
            invalidate();
        }
    }
    
    public void replaceFirstLetter(int index, Drawable drawable) {
        if (replaceMap == null) replaceMap = new SparseArray<Drawable>();
        replaceMap.append(index, drawable);
        if (showLetters != null) invalidate();
    }
    
    public void replaceFirstLetter(String letter, Drawable drawable) {
        int index = Arrays.binarySearch(firstLetters, letter, null);
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Letter is not included.");
        }
        
        replaceFirstLetter(index, drawable);
    }
    
    private Drawable replacedFirstLetter(int index) {
        if (replaceMap == null) return null;
        return replaceMap.get(index);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        
        showLetters = firstLetters;
        
        boolean recalculate = false;
        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
            recalculate = true;
        }
        else
        {
            height = getLineHeight() * showLetters.length + getPaddingTop() + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize)
            {
                height = heightSize;
                recalculate = true;
            }
        }
        
        int measureHeight = height;
        if (recalculate)
        {
            height -= getPaddingTop() + getPaddingBottom();
            int length = height / getLineHeight();
            if (length >= showLetters.length)
            {
                int lineHeight = height / showLetters.length;
                float textSize = paint.getTextSize();
                do { paint.setTextSize(++textSize); }
                while (getLineHeight() <= lineHeight);
                
                paint.setTextSize(--textSize);
                topMargin = (height - getLineHeight() * showLetters.length) >> 1;
            }
            else
            {
                int num = length / 3;
                if (num > 0)
                {
                    int size = num * 3;
                    showLetters = new String[length];
                    for (int i = 0; i < size; i++)
                    {
                        if ((i + 1) % num == 0)
                        {
                            showLetters[i] = INVALID_LETTER;
                        }
                        else
                        {
                            showLetters[i] = firstLetters[(i % num) + 9 * (i / num)];
                        }
                    }
                    
                    for (int i = size; i < length; i++)
                    {
                        showLetters[i] = firstLetters[firstLetters.length - (length - i)];
                    }
                }
                else
                {
                    showLetters = null;
                }
            }
        }
        
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else
        {
            width = (int) FloatMath.ceil(paint.measureText("W"));
            width += getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize)
            {
                width = widthSize;
            }
        }
        
        setMeasuredDimension(width, measureHeight);
    }
    
    private int getLineHeight() {
        return paint.getFontMetricsInt(null);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        background.setBounds(0, 0, w, h);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (showLetters == null)
        {
            return;
        }
        
        if (showBg)
        {
            background.draw(canvas);
        }
        
        int x = getPaddingLeft();
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int centerX = x + (width >> 1);
        
        int y = getStartY();
        int dy = getLineHeight();
        
        for (int i = 0, len = showLetters.length; i < len; i++, y += dy)
        {
            Drawable drawable = replacedFirstLetter(i);
            if (drawable != null)
            {
                drawable.setBounds(x, y, x + width, y + dy);
                drawable.draw(canvas);
            }
            else
            {
                drawLetter(i, canvas, centerX, y + dy, paint);
            }
        }
    }
    
    private void drawLetter(int index, Canvas canvas, float centerX, float y, Paint paint) {
        if (index == selectedIndex)
        {
            int color = paint.getColor();
            paint.setColor(textColorHighlight);
            canvas.drawText(showLetters[index], centerX, y, paint);
            paint.setColor(color);
        }
        else
        {
            canvas.drawText(showLetters[index], centerX, y, paint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = (int) ((event.getY() - getStartY()) / getLineHeight());
        boolean handle = index >= 0 && index < showLetters.length;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (handle)
                {
                    showBg = true;
                    setSelected(index);
                    invalidate();
                }
                
                return handle;
            case MotionEvent.ACTION_MOVE:
                if (handle)
                {
                    showBg = true;
                    setSelected(index);
                    invalidate();
                }
                else
                {
                    showBg = false;
                    invalidate();
                }
                
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                showBg = false;
                selectedIndex = -1;
                invalidate();
                break;
        }

        return true;
    }
    
    private void setSelected(int index) {
        if (index != selectedIndex && !showLetters[index].equals(INVALID_LETTER))
        {
            selectedIndex = index;
            if (listener != null)
            {
                listener.onFirstLetterChanged(showLetters[index]);
            }
        }
    }
    
    private int getStartY() {
        return getPaddingTop() + topMargin;
    }
    
    public void setOnFirstLetterChangedListener(OnFirstLetterChangedListener listener) {
        this.listener = listener;
    }
    
    public interface OnFirstLetterChangedListener {
        
        void onFirstLetterChanged(String firstLetter);
    }
}