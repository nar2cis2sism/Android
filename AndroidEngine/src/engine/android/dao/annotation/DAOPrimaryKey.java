package engine.android.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表主键配置
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DAOPrimaryKey {

    /** 数据库表主键名称 */
    public String column() default "";

    /** 数据必须为Integer类型才能自动增长 */
    public boolean autoincrement() default false;
}