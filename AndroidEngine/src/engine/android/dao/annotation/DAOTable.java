package engine.android.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表配置
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DAOTable {

    /** 数据库表名 */
    String name() default "";
}