package demo.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * A RemoteViews object (and, consequently, an App Widget) can support the following layout classes: 

    * FrameLayout 
    * LinearLayout 
    * RelativeLayout 
    * GridLayout（骗人，4.0就不支持）

    And the following widget classes: 

    * AnalogClock 
    * Button 
    * Chronometer 
    * ImageButton 
    * ImageView 
    * ProgressBar 
    * TextView 
    * ViewFlipper
    * ListView
    * GridView
    * StackView
    * AdapterViewFlipper

    Descendants of these classes are not supported.

 */

public class AppWidget extends AppWidgetProvider {
    
    private static AppWidgetData appWidgetData;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("onReceive:" + intent.getAction());
        super.onReceive(context, intent);
        
        if (!isWidgetAvailable(context))
        {
            return;
        }
        
        getAppWidgetData(context).onReceive(context, intent);
    }
    
    /**
     * 创建及更新时调用（在配置界面前）
     */

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        for (int i : appWidgetIds)
        {
            System.out.println(i + "onUpdate");
        }

        appWidgetManager.updateAppWidget(appWidgetIds, getAppWidgetData(context).buildWidget());
    }

    /**
     * 删除时调用
     */
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    	for (int i : appWidgetIds)
    	{
    		System.out.println(i + "onDeleted");
    	}
    }

    @Override
    public void onEnabled(Context context) {
        System.out.println("onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        System.out.println("onDisabled");
    }

    public static final AppWidgetManager getAppWidgetManager(Context context) {
        return AppWidgetManager.getInstance(context);
    }

    public static void updateWidget(Context context) {
        if (!isWidgetAvailable(context))
        {
            return;
        }

        getAppWidgetManager(context).updateAppWidget(
                new ComponentName(context, AppWidget.class), getAppWidgetData(context).buildWidget());
    }

    public static void notifyAppWidgetViewDataChanged(Context context, int viewId) {
        if (!isWidgetAvailable(context))
        {
            return;
        }
        
        getAppWidgetManager(context).notifyAppWidgetViewDataChanged(
        getAppWidgetManager(context).getAppWidgetIds(
        new ComponentName(context, AppWidget.class)), viewId);
    }

    private static boolean isWidgetAvailable(Context context) {
        int[] ids = getAppWidgetManager(context).getAppWidgetIds(new ComponentName(context, AppWidget.class));
        return ids != null && ids.length > 0;
    }
    
    private static AppWidgetData getAppWidgetData(Context context) {
        if (appWidgetData == null)
        {
            synchronized (AppWidget.class) {
                if (appWidgetData == null)
                {
                    appWidgetData = createAppWidgetData(context);
                }
            }
        }
        
        return appWidgetData;
    }
    
    private static AppWidgetData createAppWidgetData(Context context) {
        return new AppWidgetData(context) {
            
            @Override
            protected int getLayoutId() {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            protected void bindData(RemoteViews views) {
                // TODO Auto-generated method stub
                
            }
        };
    }
    
    public static abstract class AppWidgetData {

        protected final Context mContext;

        public AppWidgetData(Context context) {
            mContext = context;
        }

        RemoteViews buildWidget() {
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), getLayoutId());
            bindData(remoteViews);
            return remoteViews;
        }

        protected abstract int getLayoutId();

        protected abstract void bindData(RemoteViews views);

        protected void onReceive(Context context, Intent intent) {}
    }
}