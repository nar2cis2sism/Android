package engine.android.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
    
    private final LinkedList<InjectViewAnnotation> injectViewFields
    = new LinkedList<InjectViewAnnotation>();
    
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
    
    public void addInjectViewField(InjectViewAnnotation field) {
        injectViewFields.add(field);
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
    
    public JavaFile generateInjector() {
        // public class MainActivity$$Injector implements IInjector<MainActivity>
        TypeSpec injector = TypeSpec.classBuilder(classElement.getSimpleName() + IInjector.INJECTOR_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(IInjector.class), TypeName.get(classElement.asType())))
                .addSuperinterface(OnClickListener)
                .addField(buildTargetField())
                .addMethod(buildInjectMethod())
                .addMethod(buildBindViewMethod())
                .addMethod(buildOnClickMethod())
                .addMethod(buildBindDialogMethod())
                .addMethod(buildSaveStateMethod())
                .build();
        
        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
        
        // generate file
        return JavaFile.builder(packageName, injector).build();
    }
    
    private FieldSpec buildTargetField() {
        // private MainActivity target;
        return FieldSpec.builder(TypeName.get(classElement.asType()), "target", Modifier.PRIVATE).build();
    }
    
    private MethodSpec buildInjectMethod() {
        // public void inject(MainActivity target)
        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(classElement.asType()), "target");
        
        // this.target = target;
        inject.addStatement("this.target = target");
        
        return inject.build();
    }
    
    private MethodSpec buildBindViewMethod() {
        // @SuppressWarnings({ "rawtypes", "unchecked" })
        // public void bindView(ViewFinder<?> finder)
        MethodSpec.Builder bindView = MethodSpec.methodBuilder("bindView")
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "{ \"rawtypes\", \"unchecked\" }")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ViewFinder.class, "finder");
        
        for (InjectViewAnnotation field : injectViewFields)
        {
            // target.hello = (TextView) finder.findViewById(target, R.id.hello);
            bindView.addStatement("target.$N = ($T) finder.findViewById(target, $L)", 
                    field.getFieldName(), field.getFieldType(), field.getViewId());
        }
        
        for (OnClickAnnotation method : onClickMethods)
        {
            for (int viewId : method.getViewIds())
            {
                InjectViewAnnotation injectView = InjectViewAnnotation.findByViewId(viewId);
                if (injectView != null)
                {
                    // target.hello.setOnClickListener(this);
                    bindView.addStatement("target.$N.setOnClickListener(this)", injectView.getFieldName());
                }
                else
                {
                    // ((View) finder.findViewById(target, R.id.hello)).setOnClickListener(this);
                    bindView.addStatement("((View) finder.findViewById(target, $L)).setOnClickListener(this)", 
                            viewId);
                }
            }
        }
        
        return bindView.build();
    }
    
    private MethodSpec buildOnClickMethod() {
        // public void onClick(View v)
        MethodSpec.Builder onClick = MethodSpec.methodBuilder("onClick")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
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
    
    private MethodSpec buildBindDialogMethod() {
        // public void onRestoreDialogShowing(String name)
        MethodSpec.Builder onRestoreDialogShowing = MethodSpec.methodBuilder("onRestoreDialogShowing")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "name");
        
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
        
        if (!first)
            onRestoreDialogShowing.endControlFlow();
        
        return onRestoreDialogShowing.build();
    }
    
    private MethodSpec buildSaveStateMethod() {
        // public stash(boolean saveOrRestore, Map<String, Object> savedMap)
        MethodSpec.Builder stash = MethodSpec.methodBuilder("stash")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(boolean.class, "saveOrRestore")
                .addParameter(ParameterizedTypeName.get(Map.class, String.class, Object.class), "savedMap");
        
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
    
    public TypeElement getClassElement() {
        return classElement;
    }
    
    public String getFullClassName() {
        return classElement.getQualifiedName().toString();
    }
}