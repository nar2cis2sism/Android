package engine.android.util.ui;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/**
 * 显示'*'替代默认的'.'
 */
public class MyPasswordTransformationMethod extends PasswordTransformationMethod {

    private static final char ASTERISK = '*';

    private static MyPasswordTransformationMethod sInstance;

    private MyPasswordTransformationMethod() {}

    public static MyPasswordTransformationMethod getInstance() {
        if (sInstance != null)
            return sInstance;

        return sInstance = new MyPasswordTransformationMethod();
    }

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private static class PasswordCharSequence implements CharSequence {

        private final CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source;
        }

        @Override
        public int length() {
            return mSource.length();
        }

        @Override
        public char charAt(int index) {
            return ASTERISK;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }

        @Override
        public String toString() {
            return mSource.toString();
        }
    }
}