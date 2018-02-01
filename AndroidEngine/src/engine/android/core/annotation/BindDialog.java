package engine.android.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 绑定对话框<p>
 * 功能：用于屏幕旋转时保存对话框状态
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindDialog {

    /** 对话框名称 */
    String value();
}