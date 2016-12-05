package engine.android.util.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * 链接去掉下划线
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class NoUnderlineURL extends ClickableSpan {

    private final String url;

    private final CharSequence text;

    public NoUnderlineURL(String url, CharSequence text) {
        this.url = url;
        this.text = text;
    }

    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(url);
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        context.startActivity(intent);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }

    public final CharSequence getText() {
        return text;
    }

    /**
     * 将文本框中的链接找出来并替换成此样式
     */
    public static void replace(TextView tv) {
        CharSequence text = tv.getText();
        tv.setText(text, BufferType.SPANNABLE);
        Spannable sp = (Spannable) tv.getText();
        URLSpan[] urls = sp.getSpans(0, text.length(), URLSpan.class);

        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.clearSpans();

        for (URLSpan url : urls)
        {
            int start = sp.getSpanStart(url);
            int end = sp.getSpanEnd(url);
            NoUnderlineURL span = new NoUnderlineURL(url.getURL(), text.subSequence(start, end));
            style.setSpan(span, start, end, sp.getSpanFlags(url));
        }

        tv.setText(style, BufferType.SPANNABLE);
    }
}