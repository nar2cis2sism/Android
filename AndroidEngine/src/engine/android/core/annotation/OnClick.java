package engine.android.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为控件注入点击事件
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClick {

    /** View的ID（可设置多个） */
    public int[] value();
}