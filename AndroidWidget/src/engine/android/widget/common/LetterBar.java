package engine.android.widget.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import engine.android.util.ui.FloatingWindow;
import engine.android.widget.R;
import engine.android.widget.base.CustomView.TouchEventDelegate;

import java.util.Arrays;

/**
 * 字母搜索控件
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class LetterBar extends View {
    
    private static final String INVALID_LETTER = "\u00b7";          // "·"
    
    private String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", 
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
    
    private OnLetterChangedListener listener;
    
    private FloatingWindow overlayWindow;
    private TextView overlay;

    public LetterBar(Context context) {
        this(context, null);
    }

    public LetterBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.LetterBar);
    }

    public LetterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LetterBar, defStyleAttr, R.style.LetterBar);
        
        background = a.getDrawable(R.styleable.LetterBar_background);
        int textSize = a.getDimensionPixelSize(R.styleable.LetterBar_textSize, 0);
        int textColor = a.getColor(R.styleable.LetterBar_textColor, 0);
        textColorHighlight = a.getColor(R.styleable.LetterBar_textColorHighlight, 0);
        
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
        setRawTextSize(TypedValue.applyDimension(
                unit, size, getResources().getDisplayMetrics()));
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
    
    public String[] getLetters() {
        return letters;
    }
    
    public void setLetters(String[] letters) {
        this.letters = letters;
        if (showLetters != null)
        {
            requestLayout();
            invalidate();
        }
    }
    
    public void replaceLetter(int index, Drawable drawable) {
        if (replaceMap == null) replaceMap = new SparseArray<Drawable>();
        replaceMap.append(index, drawable);
        if (showLetters != null) invalidate();
    }
    
    public void replaceLetter(String letter, Drawable drawable) {
        int index = Arrays.binarySearch(letters, letter, null);
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Letter is not exists.");
        }
        
        replaceLetter(index, drawable);
    }
    
    private Drawable replacedLetter(int index) {
        if (replaceMap == null) return null;
        return replaceMap.get(index);
    }
    
    public void setOnLetterChangedListener(OnLetterChangedListener listener) {
        this.listener = listener;
    }
    
    public void setOverlay(TextView overlay) {
        if (overlayWindow != null) overlayWindow.hide();
        overlayWindow = new FloatingWindow(this.overlay = overlay);
    }
    
    private void initOverlay() {
        if (overlayWindow == null)
        {
            TextView overlay = new TextView(getContext());
            overlay.setBackgroundColor(Color.parseColor("#D1DFF2"));
            overlay.setGravity(Gravity.CENTER);
            overlay.setTextSize(48);
            overlay.setTextColor(Color.parseColor("#5077c5"));
            
            overlayWindow = new FloatingWindow(this.overlay = overlay);
            overlayWindow.setPosition(Gravity.CENTER, 0, 0);
            int size = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 70, getResources().getDisplayMetrics());
            overlayWindow.setSize(size, size);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        
        showLetters = letters;
        
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
                            showLetters[i] = letters[(i % num) + 9 * (i / num)];
                        }
                    }
                    
                    for (int i = size; i < length; i++)
                    {
                        showLetters[i] = letters[letters.length - (length - i)];
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
        int width = getWidth() - x - getPaddingRight();
        int centerX = x + (width >> 1);
        
        int y = getStartY();
        int dy = getLineHeight();
        int halfSize = dy >> 1;
        
        for (int i = 0, len = showLetters.length; i < len; i++, y += dy)
        {
            Drawable drawable = replacedLetter(i);
            if (drawable != null)
            {
                drawable.setBounds(centerX - halfSize, y, centerX + halfSize, y + dy);
                drawable.draw(canvas);
            }
            else
            {
                drawLetter(i, canvas, centerX, y + dy, paint);
            }
        }
    }
    
    private int getStartY() {
        return getPaddingTop() + topMargin;
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
    
    private void notifySelectChanged(int index) {
        initOverlay();
        if ((selectedIndex = index) == -1)
        {
            overlayWindow.hide();
        }
        else
        {
            overlayWindow.show();
            overlay.setText(showLetters[index]);
            if (listener != null)
            {
                listener.onLetterChanged(showLetters[index]);
            }
        }
    }
    
    private boolean setSelected(int y) {
        int dy = y - getStartY();
        if (dy < 0) dy = 0;
        
        int index = dy / getLineHeight();
        if (index >= showLetters.length)
        {
            index = showLetters.length - 1;
        }
        
        if (index == selectedIndex || showLetters[index].equals(INVALID_LETTER))
        {
            return false;
        }
        
        notifySelectChanged(index);
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return delegate.onTouchEvent(event);
    }
    
    private final TouchEventDelegate delegate = new TouchEventDelegate() {
        
        @Override
        public boolean handleActionDown(MotionEvent event, int x, int y) {
            showBg = true;
            setSelected(y);
            invalidate();
            return true;
        }
        
        @Override
        public void handleActionMove(MotionEvent event, int x, int y) {
            if (!showBg) return;
            
            if (background.getBounds().contains(x, y))
            {
                if (setSelected(y)) invalidate();
            }
            else
            {
                reset();
            }
        }
        
        @Override
        public void handleActionUp(MotionEvent event, int x, int y) {
            reset();
        }
        
        public void handleActionCancel(MotionEvent event) {
            reset();
        };
        
        private void reset() {
            if (showBg)
            {
                showBg = false;
                notifySelectChanged(-1);
                invalidate();
            }
        }
    };
    
    public interface OnLetterChangedListener {
        
        void onLetterChanged(String letter);
    }
}