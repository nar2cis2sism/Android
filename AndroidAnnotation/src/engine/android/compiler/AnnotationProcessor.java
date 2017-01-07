package engine.android.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import engine.android.compiler.annotation.BindDialogAnnotation;
import engine.android.compiler.annotation.InjectViewAnnotation;
import engine.android.compiler.annotation.OnClickAnnotation;
import engine.android.compiler.annotation.SavedStateAnnotation;

public class AnnotationProcessor extends AbstractProcessor implements AnnotationConst {
    
    private final HashMap<String, AnnotatedClass> annotatedClassMap
    = new HashMap<String, AnnotatedClass>();
    
    private Filer filer;                        // 文件相关的辅助类
    private Types typeUtils;                    // 类型相关的辅助类
    private Elements elementUtils;              // 元素相关的辅助类
    private Messager messager;                  // 日志相关的辅助类
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
    
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(InjectView.getCanonicalName());
        set.add(OnClick.getCanonicalName());
        set.add(BindDialog.getCanonicalName());
        set.add(SavedState.getCanonicalName());
        return Collections.unmodifiableSet(set);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // process() will be called several times.
        annotatedClassMap.clear();
        
        try {
            processInjectView(roundEnv);
            processOnClick(roundEnv);
            processBindDialog(roundEnv);
            processSavedState(roundEnv);
        } catch (Error e) {
            e.printMessage();
        }
        
        for (AnnotatedClass annotatedClass : annotatedClassMap.values())
        {
            try {
                processSuperClass(annotatedClass);
                annotatedClass.generateInjector().writeTo(filer);
            } catch (Throwable e) {
                new Error(String.format("Failed to generating file for %s cause of '%s'", 
                        annotatedClass.getFullClassName(), e.getMessage()), 
                        annotatedClass.getClassElement()).printMessage();
            }
        }
        
        return true;
    }
    
    private void processInjectView(RoundEnvironment roundEnv) throws Error {
        for (Element element : roundEnv.getElementsAnnotatedWith(InjectView))
        {
            if (element.getKind() != ElementKind.FIELD)
            {
                throw new Error(String.format(
                        "Only fields can be annotated with @%s", InjectView.getSimpleName()), element);
            }
            
            if (element.getModifiers().contains(Modifier.PRIVATE))
            {
                throw new Error(String.format(
                        "Fields annotated with @%s can not be private", InjectView.getSimpleName()), element);
            }

            getAnnotatedClass(element).addInjectViewField(new InjectViewAnnotation((VariableElement) element));
        }
    }
    
    private void processOnClick(RoundEnvironment roundEnv) throws Error {
        for (Element element : roundEnv.getElementsAnnotatedWith(OnClick))
        {
            if (element.getKind() != ElementKind.METHOD)
            {
                throw new Error(String.format(
                        "Only methods can be annotated with @%s", OnClick.getSimpleName()), element);
            }
            
            if (element.getModifiers().contains(Modifier.PRIVATE))
            {
                throw new Error(String.format(
                        "Methods annotated with @%s can not be private", OnClick.getSimpleName()), element);
            }

            getAnnotatedClass(element).addOnClickMethod(new OnClickAnnotation((ExecutableElement) element));
        }
    }
    
    private void processBindDialog(RoundEnvironment roundEnv) throws Error {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindDialog))
        {
            if (element.getKind() != ElementKind.METHOD)
            {
                throw new Error(String.format(
                        "Only methods can be annotated with @%s", BindDialog.getSimpleName()), element);
            }
            
            if (element.getModifiers().contains(Modifier.PRIVATE))
            {
                throw new Error(String.format(
                        "Methods annotated with @%s can not be private", BindDialog.getSimpleName()), element);
            }

            getAnnotatedClass(element).addBindDialogMethod(new BindDialogAnnotation((ExecutableElement) element));
        }
    }
    
    private void processSavedState(RoundEnvironment roundEnv) throws Error {
        for (Element element : roundEnv.getElementsAnnotatedWith(SavedState))
        {
            if (element.getKind() != ElementKind.FIELD)
            {
                throw new Error(String.format(
                        "Only fields can be annotated with @%s", SavedState.getSimpleName()), element);
            }
            
            if (element.getModifiers().contains(Modifier.PRIVATE))
            {
                throw new Error(String.format(
                        "Fields annotated with @%s can not be private", SavedState.getSimpleName()), element);
            }

            getAnnotatedClass(element).addSavedStateField(new SavedStateAnnotation((VariableElement) element));
        }
    }
    
    private void processSuperClass(AnnotatedClass annotatedClass) {
        TypeElement classElement = annotatedClass.getClassElement();
        
        while (true)
        {
            TypeMirror superType = classElement.getSuperclass();
            if (superType.getKind() == TypeKind.NONE)
            {
                break;
            }
            
            String superClassName = superType.toString();
            if (superClassName.startsWith("android.") || superClassName.startsWith("java."))
            {
                break;
            }

            AnnotatedClass superClass = annotatedClassMap.get(superClassName);
            if (superClass != null)
            {
                annotatedClass.setSuperClassElement(superClass.getClassElement());
                break;
            }
            else
            {
                Element superElement = typeUtils.asElement(superType);
                if (!(superElement instanceof TypeElement))
                {
                    break;
                }
                
                classElement = (TypeElement) superElement;
            }
        }
    }
    
    private AnnotatedClass getAnnotatedClass(Element element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = classElement.getQualifiedName().toString();
        AnnotatedClass annotatedClass = annotatedClassMap.get(fullClassName);
        if (annotatedClass == null)
        {
            annotatedClass = new AnnotatedClass(elementUtils, classElement);
            annotatedClassMap.put(fullClassName, annotatedClass);
        }
        
        return annotatedClass;
    }
    
    private class Error extends Exception {
        
        private static final long serialVersionUID = 1L;
        
        private final String message;
        private final Element element;
        
        public Error(String message, Element element) {
            this.message = message;
            this.element = element;
        }
        
        public void printMessage() {
            messager.printMessage(Diagnostic.Kind.ERROR, 
                    message, 
                    element);
        }
    }
}