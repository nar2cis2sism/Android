package engine.android.core;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import engine.android.core.annotation.BindDialog;
import engine.android.core.annotation.IInjector;
import engine.android.core.annotation.IInjector.ViewFinder;
import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.core.annotation.SavedState;

/**
 * 注入机制
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Injector {
    
    private static final IInjector NO_INJECTOR = new NoInjector();
    
    private static final HashMap<Class, IInjector> INJECTOR_MAP
    = new HashMap<Class, IInjector>();
    
    private static boolean apt;
    
    /**
     * 开启APT编译
     */
    public static final void enableAptBuild() {
        apt = true;
    }

    private static IInjector getInjector(Object target) {
        return loadInjector(target.getClass(), apt);
    }
    
    private static IInjector loadInjector(Class targetCls, boolean apt) {
        IInjector injector = INJECTOR_MAP.get(targetCls);
        if (injector != null)
        {
            return injector;
        }
        
        String targetName = targetCls.getName();
        if (targetName.startsWith("android.") || targetName.startsWith("java."))
        {
            return NO_INJECTOR;
        }
        
        if (apt)
        {
            try {
                Class injectorCls = Class.forName(targetName + IInjector.INJECTOR_SUFFIX);
                injector = (IInjector) injectorCls.newInstance();
            } catch (ClassNotFoundException e) {
                injector = loadInjector(targetCls.getSuperclass(), apt);
            } catch (Exception e) {
                throw new RuntimeException("Unable to create injector for " + targetCls, e);
            }
        }
        else
        {
            injector = new ReflectionInjector(targetCls, loadInjector(targetCls.getSuperclass(), apt));
        }
        
        INJECTOR_MAP.put(targetCls, injector);
        return injector;
    }

    private static void inject(Object target, ViewFinder finder) {
        getInjector(target).inject(target, finder);
    }

    private static final ViewFinder<Activity> FINDER_ACTIVITY = new ViewFinder<Activity>() {

        @Override
        public Object findViewById(Activity source, int id) {
            return source.findViewById(id);
        }
    };
    
    private static final ViewFinder<Fragment> FINDER_FRAGMENT = new ViewFinder<Fragment>() {

        @Override
        public Object findViewById(Fragment source, int id) {
            return source.getView().findViewById(id);
        }
    };
    
    private static final ViewFinder<Dialog> FINDER_DIALOG = new ViewFinder<Dialog>() {
    
        @Override
        public Object findViewById(Dialog source, int id) {
            return source.findViewById(id);
        }
    };
    
    private static final ViewFinder<View> FINDER_VIEW = new ViewFinder<View>() {
    
        @Override
        public Object findViewById(View source, int id) {
            return source.findViewById(id);
        }
    };
    
    /**
     * 注入口
     * 
     * @param target 注入对象
     * @param source 控件查找对象
     */
    public static void inject(Object target, final View source) {
        inject(target, new ViewFinder<Object>() {

            @Override
            public Object findViewById(Object obj, int id) {
                return source.findViewById(id);
            }
        });
    }

    public static void inject(Activity a) {
        inject(a, FINDER_ACTIVITY);
    }

    public static void inject(Fragment f) {
        inject(f, FINDER_FRAGMENT);
    }
    
    public static void inject(Dialog d) {
        inject(d, FINDER_DIALOG);
    }

    public static void inject(View v) {
        inject(v, FINDER_VIEW);
    }
    
    static void onRestoreDialogShowing(Object target, String name) {
        getInjector(target).onRestoreDialogShowing(target, name);
    }
    
    static void saveState(Object target, HashMap<String, Object> savedMap) {
        getInjector(target).stash(target, true, savedMap);
    }
    
    static void restoreState(Object target, HashMap<String, Object> savedMap) {
        getInjector(target).stash(target, false, savedMap);
    }
}

class NoInjector implements IInjector<Object> {

    @Override
    public void inject(Object target, ViewFinder<Object> finder) {}

    @Override
    public void onRestoreDialogShowing(Object target, String name) {}

    @Override
    public void stash(Object target, boolean saveOrRestore, Map<String, Object> savedMap) {}
}

class ReflectionInjector extends NoInjector {
    
    private final Class<?> cls;
    private final IInjector<Object> parent;
    
    private final LinkedHashMap<Field, Integer> injectViewFields
    = new LinkedHashMap<Field, Integer>();
    
    private final LinkedList<Field> savedStateFields
    = new LinkedList<Field>();
    
    private final LinkedHashMap<Method, int[]> onClickMethods
    = new LinkedHashMap<Method, int[]>();
    
    private final LinkedHashMap<String, Method> bindDialogMethods
    = new LinkedHashMap<String, Method>();
    
    public ReflectionInjector(Class<?> cls, IInjector<Object> parent) {
        this.cls = cls;
        this.parent = parent;
        init();
    }
    
    private void init() {
        for (Field field : cls.getDeclaredFields())
        {
            InjectView injectView = field.getAnnotation(InjectView.class);
            if (injectView != null)
            {
                field.setAccessible(true);
                injectViewFields.put(field, injectView.value());
            }

            SavedState savedState = field.getAnnotation(SavedState.class);
            if (savedState != null)
            {
                field.setAccessible(true);
                savedStateFields.add(field);
            }
        }
        
        for (Method method : cls.getDeclaredMethods())
        {
            OnClick onClick = method.getAnnotation(OnClick.class);
            if (onClick != null)
            {
                method.setAccessible(true);
                onClickMethods.put(method, onClick.value());
            }

            BindDialog bindDialog = method.getAnnotation(BindDialog.class);
            if (bindDialog != null)
            {
                method.setAccessible(true);
                bindDialogMethods.put(bindDialog.value(), method);
            }
        }
    }

    @Override
    public void inject(final Object target, ViewFinder<Object> finder) {
        parent.inject(target, finder);
        
        SparseArray<View> viewMap = new SparseArray<View>();
        if (!injectViewFields.isEmpty())
        {
            try {
                for (Map.Entry<Field, Integer> entry : injectViewFields.entrySet())
                {
                    int viewId = entry.getValue();
                    Object view = finder.findViewById(target, viewId);
                    if (view != null)
                    {
                        viewMap.append(viewId, (View) view);
                        entry.getKey().set(target, view);
                    }
                }
            } catch (Exception e) {
                throwInjectException(e);
            }
        }
        
        if (!onClickMethods.isEmpty())
        {
            OnClickListener onClickListener = new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    try {
                        int id = v.getId();
                        for (Map.Entry<Method, int[]> entry : onClickMethods.entrySet())
                        {
                            for (int viewId : entry.getValue())
                            {
                                if (viewId == id)
                                {
                                    entry.getKey().invoke(target);
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        throwInjectException(e);
                    }
                }
            };
            
            for (int[] viewIds : onClickMethods.values())
            {
                for (int viewId : viewIds)
                {
                    View view = viewMap.get(viewId);
                    if (view == null)
                    {
                        view = (View) finder.findViewById(target, viewId);
                    }
                    
                    view.setOnClickListener(onClickListener);
                }
            }
        }
    }
    
    @Override
    public void onRestoreDialogShowing(Object target, String name) {
        parent.onRestoreDialogShowing(target, name);
        
        Method method = bindDialogMethods.get(name);
        if (method != null)
        {
            try {
                method.invoke(target);
            } catch (Exception e) {
                throwInjectException(e);
            }
        }
    }
    
    @Override
    public void stash(Object target, boolean saveOrRestore, Map<String, Object> savedMap) {
        parent.stash(target, saveOrRestore, savedMap);
        
        if (savedStateFields.isEmpty())
        {
            return;
        }
        
        try {
            if (saveOrRestore)
            {
                for (Field field : savedStateFields)
                {
                    savedMap.put(field.getName(), field.get(target));
                }
            }
            else
            {
                for (Field field : savedStateFields)
                {
                    field.set(target, savedMap.get(field.getName()));
                }
            }
        } catch (Exception e) {
            throwInjectException(e);
        }
    }
    
    static void throwInjectException(Exception e) {
        throw new RuntimeException("Inject error", e);
    }
}