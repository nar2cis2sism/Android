package engine.android.core;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.view.View;

import java.util.HashMap;

import engine.android.core.annotation.IInjector;
import engine.android.core.annotation.IInjector.ViewFinder;

/**
 * 注入机制
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Injector {
    
    private static final HashMap<Class, IInjector> INJECTOR_MAP
    = new HashMap<Class, IInjector>();
    
    private static IInjector getInjector(Object target) {
        Class targetCls = target.getClass();
        
        try {
            IInjector injector = INJECTOR_MAP.get(targetCls);
            if (injector == null)
            {
                Class injectorCls = Class.forName(targetCls.getName() + IInjector.INJECTOR_SUFFIX);
                INJECTOR_MAP.put(targetCls, injector = (IInjector) injectorCls.newInstance());
            }
            
            injector.inject(target);
            return injector;
        } catch (Exception e) {
            throw new RuntimeException("Unable to find injector for " + targetCls, e);
        }
    }
    
    private static void inject(Object target, ViewFinder finder) {
        getInjector(target).bindView(finder);
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
        getInjector(target).onRestoreDialogShowing(name);
    }
    
    static void saveState(Object target, HashMap<String, Object> savedMap) {
        getInjector(target).stash(true, savedMap);
    }
    
    static void restoreState(Object target, HashMap<String, Object> savedMap) {
        getInjector(target).stash(false, savedMap);
    }
}