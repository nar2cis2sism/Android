package engine.android.compiler.annotation;

import com.squareup.javapoet.TypeName;

import java.util.HashMap;

import javax.lang.model.element.VariableElement;

import engine.android.compiler.AnnotationConst;

public class InjectViewAnnotation {
    
    private static final HashMap<Integer, InjectViewAnnotation> viewMap
    = new HashMap<Integer, InjectViewAnnotation>();             // viewId对应元素查找表
    
    private final VariableElement field;                        // 注解的View元素
    
    private final int viewId;
    
    public InjectViewAnnotation(VariableElement field) {
        viewId = (this.field = field).getAnnotation(AnnotationConst.InjectView).value();
        viewMap.put(viewId, this);
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
    
    public static InjectViewAnnotation findByViewId(int viewId) {
        return viewMap.get(viewId);
    }
}