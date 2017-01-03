package engine.android.compiler.annotation;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

public class SavedStateAnnotation {
    
    private final VariableElement field;                        // 注解的域
    
    public SavedStateAnnotation(VariableElement field) {
        this.field = field;
    }
    
    public String getFieldName() {
        return field.getSimpleName().toString();
    }
    
    public TypeName getFieldType() {
        return TypeName.get(field.asType());
    }
}