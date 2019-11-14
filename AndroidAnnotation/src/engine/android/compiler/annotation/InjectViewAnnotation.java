package engine.android.compiler.annotation;

import engine.android.compiler.AnnotationConst;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

public class InjectViewAnnotation {
    
    private final VariableElement field;                        // 注解的View元素
    
    private final int viewId;
    
    public InjectViewAnnotation(VariableElement field) {
        viewId = (this.field = field).getAnnotation(AnnotationConst.InjectView).value();
    }
    
    public String getFieldName() {
        return field.getSimpleName().toString();
    }
    
    public TypeName getFieldType() {
        return TypeName.get(field.asType());
    }
    
    public int getViewId() {
        return viewId;
    }
}