package engine.android.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能：用于屏幕旋转时属性存储
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SavedState {}