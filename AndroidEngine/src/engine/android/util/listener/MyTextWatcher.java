package engine.android.util.listener;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

/**
 * 自定义输入监听器
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public class MyTextWatcher implements TextWatcher {

    private String text;                // 之前输入文本

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s))
        {
            if (!TextUtils.isEmpty(text))
            {
                changeToEmpty(text);
                return;
            }
        }
        else
        {
            if (TextUtils.isEmpty(text))
            {
                changeFromEmpty(s.toString());
                return;
            }
        }

        changeNotEmpty(text, s.toString());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        text = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    /**
     * 输入改变（从无到有）
     * 
     * @param after 输入的文本
     */
    protected void changeFromEmpty(String after) {};

    /**
     * 输入改变（从有到无）
     * 
     * @param before 之前输入的文本
     */
    protected void changeToEmpty(String before) {};

    /**
     * 输入改变（没有空文本）
     * 
     * @param before 输入前的文本
     * @param after 输入后的文本
     */
    protected void changeNotEmpty(String before, String after) {}
}