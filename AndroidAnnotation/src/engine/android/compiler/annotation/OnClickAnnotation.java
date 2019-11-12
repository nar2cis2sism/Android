package engine.android.compiler.annotation;

import engine.android.compiler.AnnotationConst;

import javax.lang.model.element.ExecutableElement;

public class OnClickAnnotation {
    
    private final ExecutableElement method;     // 注解的函数
    
    private final int[] viewIds;
    
    public OnClickAnnotation(ExecutableElement method) {
        viewIds = (this.method = method).getAnnotation(AnnotationConst.OnClick).value();
    }
    
    public String getMethodName() {
        return method.getSimpleName().toString();
    }
    
    public int[] getViewIds() {
        return viewIds;
    }
}