package engine.android.compiler.annotation;

import javax.lang.model.element.ExecutableElement;

import engine.android.compiler.AnnotationConst;

public class BindDialogAnnotation {
    
    private final ExecutableElement method;     // 注解的函数
    
    private final String dialogName;
    
    public BindDialogAnnotation(ExecutableElement method) {
        dialogName = (this.method = method).getAnnotation(AnnotationConst.BindDialog).value();
    }
    
    public String getMethodName() {
        return method.getSimpleName().toString();
    }
    
    public String getDialogName() {
        return dialogName;
    }
}