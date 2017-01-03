package engine.android.compiler;

import com.squareup.javapoet.ClassName;

import engine.android.core.annotation.BindDialog;
import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.annotation.SavedState;

public interface AnnotationConst {
    
    public static final Class<InjectView> InjectView = InjectView.class;
    public static final Class<OnClick> OnClick = OnClick.class;
    public static final Class<BindDialog> BindDialog = BindDialog.class;
    public static final Class<SavedState> SavedState = SavedState.class;
    
    public static final ClassName View = ClassName.get("android.view", "View");
    public static final ClassName OnClickListener = ClassName.get("android.view", "View", "OnClickListener");
}