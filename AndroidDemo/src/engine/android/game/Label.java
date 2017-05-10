package engine.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏标签
 * 
 * @author Daimon
 * @version 3.0
 * @since 9/4/2012
 */

public class Label extends Sprite {

    public static final int CENTER  = 0;            // 居中对齐
    public static final int TOP     = 1;            // 居上对齐
    public static final int LEFT    = 2;            // 居左对齐
    public static final int BOTTOM  = 3;            // 居下对齐
    public static final int RIGHT   = 4;            // 居右对齐

    int horizontalAlignment = CENTER;               // 水平对齐方式，默认居中

    int verticalAlignment = CENTER;                 // 垂直对齐方式，默认居中

    int horizontalTextPosition = CENTER;            // 水平文本位置，默认文本和图像水平重叠

    int verticalTextPosition = BOTTOM;              // 垂直文本位置，默认文本在图像的下面

    int iconTextGap = 4;                            // 文本与图像的间距

    int top, left, bottom, right;                   // 各边距

    private int textX, textY, iconX, iconY;         // 文本，图片的位置
    private int textW, textH, iconW, iconH;         // 文本，图片的大小

    private String text;                            // 显示文本
    int textGap;                                    // 文本行间距

    private String[] show;                          // 文本显示字符串数组
    private float[] showPosition;                   // 文本显示字符串位置

    boolean wrapContent = true;                     // 文本自适应大小

    private Attribute attribute;                    // 属性设置

    public Label() {
        super(0, 0);
        paint = new Paint();
    }

    public Label(String text) {
        this();
        setText(text);
    }

    public Label(String text, Bitmap icon) {
        super(icon);
        paint = new Paint();
        setText(text);
    }

    public String getText() {
        return text;
    }

    /**
     * 更换文本
     */

    public void setText(String text) {
        if (!TextUtils.isEmpty(this.text))
        {
            if (!this.text.equals(text))
            {
                this.text = text;
                invalidate();
            }
        }
        else
        {
            if (!TextUtils.isEmpty(text))
            {
                this.text = text;
                invalidate();
            }
        }
    }

    @Override
    public void setImage(Bitmap image) {
        this.image = image;
        if (image != null)
        {
            iconW = image.getWidth();
            iconH = image.getHeight();
        }

        invalidate();
    }

    @Override
    protected boolean doPixelCollision(int x, int y) {
        return super.doPixelCollision(x - iconX, y - iconY);
    }

    @Override
    public void setPaint(Paint paint) {
        boolean isPaintChanged = isPaintChanged(this.paint, paint);
        super.setPaint(paint);
        if (isPaintChanged)
        {
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (image != null)
        {
            canvas.drawBitmap(image, x + iconX, y + iconY, paint);
        }

        if (!TextUtils.isEmpty(text) && show != null)
        {
            int x = this.x + textX;
            int y = this.y + textY;
            for (int i = 0; i < show.length; i++)
            {
                if (showPosition[i] < 0)
                {
                    return;
                }

                if (showPosition[i] > height)
                {
                    showPosition[i] = -1;
                    return;
                }

                canvas.drawText(show[i], x, y + showPosition[i], paint);
            }
        }
    }

    void invalidate() {
        calculateTextSize();
        adjust();
    }

    /**
     * 计算文本大小
     */

    private void calculateTextSize() {
        if (!TextUtils.isEmpty(text))
        {
            float w = 0;
            if (!wrapContent)
            {
                w = width - left - right;
                if (horizontalTextPosition != CENTER && image != null)
                {
                    w -= iconW - iconTextGap;
                }
            }

            show = split(text, w, paint);
            showPosition = new float[show.length];

            FontMetrics fm = paint.getFontMetrics();
            float baseline = fm.descent - fm.ascent;
            float y = baseline; // 由于系统基于字体的底部来绘制文本，所以需要加上字体的高度
            float dy = baseline + fm.leading + textGap; // 添加字体行间距

            for (int i = 0; i < show.length; i++)
            {
                showPosition[i] = y;
                w = (int) Math.max(w, paint.measureText(show[i]));
                y += dy;
            }

            textW = (int) Math.ceil(w);
            textH = (int) Math.ceil((y -= dy));
            if (!wrapContent)
            {
                textH = height - top - bottom;
                if (verticalTextPosition != CENTER && image != null)
                {
                    textH -= iconH - iconTextGap;
                }
            }
        }
    }

    /**
     * 自动分割文本
     * 
     * @param text 需要分割的文本
     * @param width 可显示宽度
     * @param paint 画笔，用来根据字体测量宽度用
     * @return 分割好的字符串数组
     */

    private static String[] split(String text, float width, Paint paint) {
        float count = 0;
        List<String> list = new ArrayList<String>();
        int len = text.length();
        char c;
        for (int i = 0; i < len;)
        {
            c = text.charAt(i);
            if (c == '\n')
            {
                list.add(text.substring(0, i));
                text = text.substring(i + 1);
                len = text.length();
                i = 0;
            }
            else if (c == '\r')
            {
                c = text.charAt(i + 1);
                if (c == '\n')
                {
                    list.add(text.substring(0, i));
                    text = text.substring(i + 2);
                    len = text.length();
                    i = 0;
                }
                else
                {
                    text = text.substring(0, i) + text.substring(i + 1);
                }
            }
            else
            {
                if (width > 0)
                {
                    count = paint.measureText(text, 0, i + 1);
                    if (count > width)
                    {
                        list.add(text.substring(0, i));
                        text = text.substring(i);
                        len = text.length();
                        i = 0;
                        continue;
                    }
                }

                i++;
            }
        }

        list.add(text);
        return list.toArray(new String[list.size()]);
    }

    /**
     * 调整标签尺寸以及确定文本图片位置
     */

    private void adjust() {
        int width, height;
        if (!TextUtils.isEmpty(text) && image != null)
        {
            int textW = this.textW;
            int textH = this.textH;
            int iconW = this.iconW;
            int iconH = this.iconH;

            if (horizontalTextPosition == CENTER)
            {
                int maxW = Math.max(textW, iconW);
                width = left + maxW + right;
                if (horizontalAlignment == CENTER)
                {
                    textX = left + (maxW - textW) / 2;
                    iconX = left + (maxW - iconW) / 2;
                }
                else if (horizontalAlignment == LEFT)
                {
                    textX = iconX = left;
                }
                else if (horizontalAlignment == RIGHT)
                {
                    textX = width - right - textW;
                    iconX = width - right - iconW;
                }
                else
                {
                    // 参数不合法
                    throw new IllegalArgumentException();
                }
            }
            else if (horizontalTextPosition == LEFT)
            {
                width = (iconX = (textX = left) + textW + iconTextGap) + iconW + right;
            }
            else if (horizontalTextPosition == RIGHT)
            {
                width = (textX = (iconX = left) + iconW + iconTextGap) + textW + right;
            }
            else
            {
                // 参数不合法
                throw new IllegalArgumentException();
            }

            if (verticalTextPosition == CENTER)
            {
                int maxH = Math.max(textH, iconH);
                height = top + maxH + bottom;
                if (verticalAlignment == CENTER)
                {
                    textY = top + (maxH - textH) / 2;
                    iconY = top + (maxH - iconH) / 2;
                }
                else if (verticalAlignment == TOP)
                {
                    textY = iconY = top;
                }
                else if (verticalAlignment == BOTTOM)
                {
                    textY = height - bottom - textH;
                    iconY = height - bottom - iconH;
                }
                else
                {
                    // 参数不合法
                    throw new IllegalArgumentException();
                }
            }
            else if (verticalTextPosition == TOP)
            {
                height = (iconY = (textY = top) + textH + iconTextGap) + iconH + bottom;
            }
            else if (verticalTextPosition == BOTTOM)
            {
                height = (textY = (iconY = top) + iconH + iconTextGap) + textH + bottom;
            }
            else
            {
                // 参数不合法
                throw new IllegalArgumentException();
            }
        }
        else if (image != null)
        {
            width = (iconX = left) + iconW + right;
            height = (iconY = top) + iconH + bottom;
        }
        else if (!TextUtils.isEmpty(text))
        {
            width = (textX = left) + textW + right;
            height = (textY = top) + textH + bottom;
        }
        else
        {
            width = height = 0;
        }

        sizeChanged(width, height);
    }

    /**
     * 判断画笔是否改变
     */

    private boolean isPaintChanged(Paint p1, Paint p2) {
        FontMetrics src = p1.getFontMetrics();
        FontMetrics dst = p2.getFontMetrics();
        return src.ascent != dst.ascent || src.descent != dst.descent || src.leading != dst.leading;
    }

    public Attribute getAttribute() {
        return attribute == null ? attribute = new Attribute(this) : attribute;
    }

    @Override
    public String toString() {
        return String.format("Text:%s ", text) + super.toString();
    }

    /**
     * 标签属性
     */

    public static final class Attribute {

        private final Label label;

        Attribute(Label label) {
            this.label = label;
        }

        public Attribute setHorizontalAlignment(int horizontalAlignment) {
            label.horizontalAlignment = horizontalAlignment;
            return this;
        }

        public Attribute setHorizontalTextPosition(int horizontalTextPosition) {
            label.horizontalTextPosition = horizontalTextPosition;
            return this;
        }

        public Attribute setVerticalAlignment(int verticalAlignment) {
            label.verticalAlignment = verticalAlignment;
            return this;
        }

        public Attribute setVerticalTextPosition(int verticalTextPosition) {
            label.verticalTextPosition = verticalTextPosition;
            return this;
        }

        public Attribute setIconTextGap(int iconTextGap) {
            label.iconTextGap = iconTextGap;
            return this;
        }

        /**
         * 设置边距
         * 
         * @param top,left,bottom,right 上，左，下，右边距
         */

        public Attribute setMargin(int top, int left, int bottom, int right) {
            label.top = top;
            label.left = left;
            label.bottom = bottom;
            label.right = right;
            return this;
        }

        /**
         * 设置文本行间距
         */

        public Attribute setTextGap(int textGap) {
            label.textGap = textGap;
            return this;
        }

        public Attribute setWarpContent(boolean warpContent) {
            label.wrapContent = warpContent;
            return this;
        }

        /**
         * 更新属性生效
         */

        public void invalidateAttribute() {
            label.invalidate();
        }
    }
}