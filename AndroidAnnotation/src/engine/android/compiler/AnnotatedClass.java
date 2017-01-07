package engine.android.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import engine.android.compiler.annotation.BindDialogAnnotation;
import engine.android.compiler.annotation.InjectViewAnnotation;
import engine.android.compiler.annotation.OnClickAnnotation;
import engine.android.compiler.annotation.SavedStateAnnotation;
import engine.android.core.annotation.IInjector;
import engine.android.core.annotation.IInjector.ViewFinder;

public class AnnotatedClass implements AnnotationConst {

    private final Elements elementUtils;
    private final TypeElement classElement;     // 类元素
    
    private TypeElement superClassElement;      // 父类元素
    
    private final LinkedHashMap<Integer, InjectViewAnnotation> injectViewFields
    = new LinkedHashMap<Integer, InjectViewAnnotation>();       // viewId对应元素查找表
    
    private final LinkedList<SavedStateAnnotation> savedStateFields
    = new LinkedList<SavedStateAnnotation>();
    
    private final LinkedList<OnClickAnnotation> onClickMethods
    = new LinkedList<OnClickAnnotation>();
    
    private final LinkedList<BindDialogAnnotation> bindDialogMethods
    = new LinkedList<BindDialogAnnotation>();
    
    public AnnotatedClass(Elements elementUtils, TypeElement classElement) {
        this.elementUtils = elementUtils;
        this.classElement = classElement;
    }
    
    public void setSuperClassElement(TypeElement superClassElement) {
        this.superClassElement = superClassElement;
    }
    
    public void addInjectViewField(InjectViewAnnotation field) {
        injectViewFields.put(field.getViewId(), field);
    }
    
    public void addSavedStateField(SavedStateAnnotation field) {
        savedStateFields.add(field);
    }
    
    public void addOnClickMethod(OnClickAnnotation method) {
        onClickMethods.add(method);
    }
    
    public void addBindDialogMethod(BindDialogAnnotation method) {
        bindDialogMethods.add(method);
    }
    
    /**
     * generate file
     */
    public JavaFile generateInjector() {
        return JavaFile.builder(getPackageName(classElement), buildInjectorClass()).build();
    }
    
    private TypeSpec buildInjectorClass() {
        // public class MainActivity$$Injector<T extends MainActivity>
        TypeSpec.Builder injector = TypeSpec.classBuilder(getInjectorName(classElement))
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", getClassTypeName()));
        if (superClassElement == null)
        {
            // implements IInjector<T>
            injector.addSuperinterface(ParameterizedTypeName.get(
                    ClassName.get(IInjector.class), 
                    getVariableTypeName()));
        }
        else
        {
            // extends BaseActivity$$Injector<T>
            injector.superclass(ParameterizedTypeName.get(
                    ClassName.get(getPackageName(superClassElement), getInjectorName(superClassElement)), 
                    getVariableTypeName()));
        }
        
        injector
        .addMethod(buildInjectMethod())
        .addMethod(buildBindDialogMethod())
        .addMethod(buildSaveStateMethod());
        
        if (!onClickMethods.isEmpty()) injector.addMethod(buildOnClickMethod());
        
        return injector.build();
    }
    
    private MethodSpec buildInjectMethod() {
        // public void inject(final T target, ViewFinder<T> finder)
        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getVariableTypeName(), "target", Modifier.FINAL)
                .addParameter(ParameterizedTypeName.get(ClassName.get(ViewFinder.class), getVariableTypeName()), 
                        "finder");
        
        if (superClassElement != null)
        {
            // super.inject(target, finder);
            inject.addStatement("super.inject(target, finder)");
        }
        
        for (InjectViewAnnotation field : injectViewFields.values())
        {
            // target.hello = (TextView) finder.findViewById(target, R.id.hello);
            inject.addStatement("target.$N = ($T) finder.findViewById(target, $L)", 
                    field.getFieldName(), field.getFieldType(), field.getViewId());
        }
        
        if (onClickMethods.isEmpty())
        {
            return inject.build();
        }
        
        /*
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity$$Injector.this.onClick(target, v);
            }
        };
        */
        StringBuilder onClickListener = new StringBuilder()
                .append("$T onClickListener = new $T() {").append("\n")
                .append("@Override").append("\n")
                .append("public void onClick(View v) {").append("\n")
                .append("$N.this.onClick(target, v);").append("\n")
                .append("}").append("\n")
                .append("}");
        inject.addStatement(onClickListener.toString(), OnClickListener, OnClickListener, getInjectorName(classElement));
        
        for (OnClickAnnotation method : onClickMethods)
        {
            for (int viewId : method.getViewIds())
            {
                InjectViewAnnotation injectView = findByViewId(viewId);
                if (injectView != null)
                {
                    // target.hello.setOnClickListener(onClickListener);
                    inject.addStatement("target.$N.setOnClickListener(onClickListener)", injectView.getFieldName());
                }
                else
                {
                    // ((View) finder.findViewById(target, R.id.hello)).setOnClickListener(onClickListener);
                    inject.addStatement("((View) finder.findViewById(target, $L)).setOnClickListener(onClickListener)", 
                            viewId);
                }
            }
        }
        
        return inject.build();
    }
    
    private MethodSpec buildBindDialogMethod() {
        // public void onRestoreDialogShowing(T target, String name)
        MethodSpec.Builder onRestoreDialogShowing = MethodSpec.methodBuilder("onRestoreDialogShowing")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getVariableTypeName(), "target")
                .addParameter(String.class, "name");
        
        if (superClassElement != null)
        {
            // super.onRestoreDialogShowing(target, name);
            onRestoreDialogShowing.addStatement("super.onRestoreDialogShowing(target, name)");
        }
        
        if (bindDialogMethods.isEmpty())
        {
            return onRestoreDialogShowing.build();
        }
        
        boolean first = true;
        for (BindDialogAnnotation method : bindDialogMethods)
        {
            if (first)
            {
                // if ("hello".equals(name))
                onRestoreDialogShowing.beginControlFlow("if ($S.equals(name))", method.getDialogName());
                first = false;
            }
            else
            {
                onRestoreDialogShowing.nextControlFlow("else if ($S.equals(name))", method.getDialogName());
            }
            
            // target.showHelloDialog();
            onRestoreDialogShowing.addStatement("target.$N()", method.getMethodName());
        }

        onRestoreDialogShowing.endControlFlow();
        
        return onRestoreDialogShowing.build();
    }
    
    private MethodSpec buildSaveStateMethod() {
        // public stash(T target, boolean saveOrRestore, Map<String, Object> savedMap)
        MethodSpec.Builder stash = MethodSpec.methodBuilder("stash")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getVariableTypeName(), "target")
                .addParameter(boolean.class, "saveOrRestore")
                .addParameter(ParameterizedTypeName.get(Map.class, String.class, Object.class), "savedMap");
        
        if (superClassElement != null)
        {
            // super.stash(target, saveOrRestore, savedMap);
            stash.addStatement("super.stash(target, saveOrRestore, savedMap)");
        }
        
        if (savedStateFields.isEmpty())
        {
            return stash.build();
        }
        
        // if (saveOrRestore)
        stash.beginControlFlow("if (saveOrRestore)");
        
        for (SavedStateAnnotation field : savedStateFields)
        {
            // savedMap.put("text", target.text);
            stash.addStatement("savedMap.put($S, target.$N)", field.getFieldName(), field.getFieldName());
        }
        
        // else
        stash.nextControlFlow("else");
        
        for (SavedStateAnnotation field : savedStateFields)
        {
            // target.text = (String) savedMap.get("text");
            stash.addStatement("target.$N = ($T) savedMap.get($S)", 
                    field.getFieldName(), field.getFieldType(), field.getFieldName());
        }
        
        stash.endControlFlow();
        
        return stash.build();
    }
    
    private MethodSpec buildOnClickMethod() {
        // void onClick(T target, View v)
        MethodSpec.Builder onClick = MethodSpec.methodBuilder("onClick")
                .addParameter(getVariableTypeName(), "target")
                .addParameter(View, "v");
        
        // switch (v.getId())
        onClick.beginControlFlow("switch (v.getId())");
        
        for (OnClickAnnotation method : onClickMethods)
        {
            StringBuilder caseSb = new StringBuilder();
            for (int viewId : method.getViewIds())
            {
                // case R.id.hello:
                caseSb.append("case ").append(viewId).append(":\n");
            }
            
            // target.hello();
            // break;
            onClick.addStatement(caseSb + "target.$N();\nbreak", method.getMethodName());
        }
        
        onClick.endControlFlow();
        
        return onClick.build();
    }

    public TypeElement getClassElement() {
        return classElement;
    }
    
    public String getFullClassName() {
        return classElement.getQualifiedName().toString();
    }
    
    private TypeName getClassTypeName() {
        return TypeName.get(classElement.asType());
    }
    
    /**
     * 获取类元素的包名
     */
    private String getPackageName(TypeElement classElement) {
        return elementUtils.getPackageOf(classElement).getQualifiedName().toString();
    }
    
    /**
     * 获取注入类的名称
     */
    private String getInjectorName(TypeElement classElement) {
        return classElement.getSimpleName() + IInjector.INJECTOR_SUFFIX;
    }
    
    /**
     * 获取泛型名
     */
    private TypeVariableName getVariableTypeName() {
        return TypeVariableName.get("T");
    }
    
    private InjectViewAnnotation findByViewId(int viewId) {
        return injectViewFields.get(viewId);
    }
}