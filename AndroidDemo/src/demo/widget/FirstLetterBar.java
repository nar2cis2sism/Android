package demo.widget;

import engine.android.util.ui.UIUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import demo.android.R;

/**
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public final class FirstLetterBar extends View {

    public static final String[] firstLetters = { 
        "#", "A", "B", "C", "D", "E", "F", "G", "H",
        "I", "J", "K", "L", "M", "N", "O", "P", "Q",
        "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    private static final String invalidateLetter = "\u00b7"/* "·" */;

    private final int bgColor = Color.parseColor("#40222222");
    private boolean showBg;

    private String[] showLetters;
    private int selectedIndex = -1;

    private Paint bgPaint;
    private Paint paint;
    private Paint selectedPaint;

    private final RectF bgRect = new RectF();

    private float verticalMargin;

    private OnFirstLetterChangedListener listener;

    public FirstLetterBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FirstLetterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FirstLetterBar(Context context) {
        super(context);
        init();
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setColor(bgColor);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(30);

        selectedPaint = new Paint(paint);
        selectedPaint.setColor(Color.parseColor("#3399ff"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else
        {
            width = (int) FloatMath.ceil(paint.measureText("W"));
            width += getPaddingLeft() + getPaddingRight();
            width = Math.max(width, getSuggestedMinimumWidth());
            if (widthMode == MeasureSpec.AT_MOST)
            {
                width = Math.min(width, widthSize);
            }
        }

        height = (int) FloatMath.ceil(getLineHeight(paint) * firstLetters.length);
        height += getPaddingTop() + getPaddingBottom();
        boolean recalculate = false;
        if (heightMode == MeasureSpec.EXACTLY)
        {
            if (height > heightSize)
            {
                recalculate = true;
            }
            else if (height < heightSize)
            {
                verticalMargin = (heightSize - height) / 2;
            }

            height = heightSize;
        }
        else
        {
            height = Math.max(height, getSuggestedMinimumHeight());
            if (heightMode == MeasureSpec.AT_MOST)
            {
                if (height > heightSize)
                {
                    height = heightSize;
                    recalculate = true;
                }
            }
        }

        setMeasuredDimension(width, height);
        bgRect.set(0, 0, width, height);

        showLetters = firstLetters;
        if (recalculate)
        {
            height -= getPaddingTop() + getPaddingBottom();
            int length = (int) (height / getLineHeight(paint));
            if (length >= showLetters.length)
            {
                return;
            }

            int num = (length - 1) / 3;
            if (num > 0)
            {
                int size = num * 3;
                showLetters = new String[length];
                for (int i = 0; i < size; i++)
                {
                    if ((i + 1) % num == 0)
                    {
                        showLetters[i] = invalidateLetter;
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

    private static float getLineHeight(Paint paint) {
        return paint.getFontSpacing();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (showLetters == null)
        {
            return;
        }

        if (showBg)
        {
            canvas.drawRoundRect(bgRect, 15, 15, bgPaint);
        }

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        float centerX = getPaddingLeft() + width / 2;

        float dy = getLineHeight(paint);
        float y = getStartY() + dy;

        for (int i = 0; i < showLetters.length; i++, y += dy)
        {
            if (i == selectedIndex)
            {
                canvas.drawText(showLetters[i], centerX, y, selectedPaint);
            }
            else
            {
                canvas.drawText(showLetters[i], centerX, y, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        int index = (int) ((y - getStartY()) / getLineHeight(paint));
        boolean process = index >= 0 && index < showLetters.length;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (process)
                {
                    showBg = true;
                    if (index != selectedIndex && !showLetters[index].equals(invalidateLetter))
                    {
                        selectedIndex = index;
                        if (listener != null)
                        {
                            listener.onFirstLetterChanged(showLetters[index]);
                        }
                    }

                    invalidate();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (process && index != selectedIndex
                && !showLetters[index].equals(invalidateLetter))
                {
                    selectedIndex = index;
                    invalidate();
                    if (listener != null)
                    {
                        listener.onFirstLetterChanged(showLetters[index]);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                showBg = false;
                selectedIndex = -1;
                invalidate();

                break;
        }

        return true;
    }

    private float getStartY() {
        return verticalMargin + getPaddingTop();
    }

    public void setOnFirstLetterChangedListener(OnFirstLetterChangedListener listener) {
        this.listener = listener;
    }

    public static interface OnFirstLetterChangedListener {

        public void onFirstLetterChanged(String firstLetter);
    }

    /**
     * @deprecated
     */

    public static abstract class FirstLetterAdapter extends BaseAdapter implements
            OnItemClickListener {

        private final String[] firstLetters = FirstLetterBar.firstLetters;

        private final Context context;

        public FirstLetterAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return firstLetters.length;
        }

        @Override
        public String getItem(int position) {
            return firstLetters[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(context).inflate(R.layout.first_letter_listitem,
                        parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.content = (ListView) convertView.findViewById(R.id.content);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            if (holder != null)
            {
                String firstLetter = firstLetters[position];
                ListAdapter adapter = getData(firstLetter);
                if (adapter.getCount() == 0)
                {
                    holder.title.setVisibility(View.GONE);
                    holder.content.setVisibility(View.GONE);
                }
                else
                {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);

                    holder.title.setText(getTitle(firstLetter));
                    holder.content.setAdapter(adapter);
                    UIUtil.modifyListViewHeight(holder.content);

                    holder.content.setOnItemClickListener(this);
                }
            }

            return convertView;
        }

        public abstract ListAdapter getData(String firstLetter);

        public abstract String getTitle(String firstLetter);

        private static final class ViewHolder {

            TextView title;

            ListView content;
        }
    }
}