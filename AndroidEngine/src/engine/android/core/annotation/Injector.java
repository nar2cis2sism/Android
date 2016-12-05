package engine.android.core.annotation;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * 注入机制
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class Injector {

    private static class InjectException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public InjectException(Throwable throwable) {
            super(throwable);
        }
    }
    
    private static void injectView(ViewFinder<?> finder, Class<?> topCls) {
        try {
            Object obj = finder.obj;
            for (Class<?> c = obj.getClass(); c != topCls; c = c.getSuperclass())
            {
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields)
                {
                    InjectView injectView = field.getAnnotation(InjectView.class);
                    if (injectView != null)
                    {
                        View view = finder.findViewById(injectView.value());
                        if (view != null)
                        {
                            field.setAccessible(true);
                            field.set(obj, view);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new InjectException(e);
        }
    }

    public static void injectView(Activity a) {
        injectView(new ActivityHolder(a), Activity.class);
    }

    public static void injectView(Fragment f) {
        injectView(new FragmentHolder(f), Fragment.class);
    }

    public static void injectView(ViewGroup v) {
        injectView(new ViewHolder(v), ViewGroup.class);
    }
    
    private static abstract class ViewFinder<T> {
        
        final T obj;
        
        public ViewFinder(T obj) {
            this.obj = obj;
        }
        
        public abstract View findViewById(int id);
    }
    
    private static class ActivityHolder extends ViewFinder<Activity> {

        public ActivityHolder(Activity obj) {
            super(obj);
        }

        @Override
        public View findViewById(int id) {
            return obj.findViewById(id);
        }
    }
    
    private static class FragmentHolder extends ViewFinder<Fragment> {

        public FragmentHolder(Fragment obj) {
            super(obj);
        }

        @Override
        public View findViewById(int id) {
            View root = obj.getView();
            if (root == null)
            {
                return null;
            }
            
            return root.findViewById(id);
        }
    }
    
    private static class ViewHolder extends ViewFinder<View> {

        public ViewHolder(View obj) {
            super(obj);
        }

        @Override
        public View findViewById(int id) {
            return obj.findViewById(id);
        }
    }
}