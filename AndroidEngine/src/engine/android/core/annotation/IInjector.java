package engine.android.core.annotation;

import java.util.Map;

/**
 * 注解处理器实现的接口
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public interface IInjector<T> {
    
    /** 注解编译器生成的类名后缀 **/
    public static final String INJECTOR_SUFFIX = "$$Injector";
    
    /**
     * 注入口（与View进行绑定）
     * 
     * @param target 注解源
     */
    void inject(T target, ViewFinder<T> finder);
    
    interface ViewFinder<S> {
        
        Object findViewById(S source, int id);
    }
    
    /**
     * 用于恢复对话框状态，与{@link BindDialog}关联，独家提供
     * 
     * @param name 显示对话框名称
     */
    void onRestoreDialogShowing(T target, String name);
    
    /**
     * 用于存取属性状态，与{@link SavedState}关联，独家提供
     * 
     * @param saveOrRestore True为存储属性状态,False为恢复属性状态
     * @param savedMap 属性查询表
     */
    void stash(T target, boolean saveOrRestore, Map<String, Object> savedMap);
}