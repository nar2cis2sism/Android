package demo.appwidget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        try {
            String name = intent.getAction();
            return (RemoteViewsFactory) Class.forName(name).getConstructor(Context.class)
                    .newInstance(this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static final Intent buildFactory(Context context, Class<? extends WidgetFactory> cls) {
        return new Intent(context, WidgetService.class).setAction(cls.getName());
    }
    
    public static abstract class WidgetFactory implements RemoteViewsFactory {
        
        protected final Context mContext;
        
        public WidgetFactory(Context context) {
            mContext = context;
        }
    }
    
    public static abstract class WidgetCursorFactory extends WidgetFactory {
        
        protected Cursor mCursor;
        
        public WidgetCursorFactory(Context context) {
            super(context);
        }

        @Override
        public void onCreate() {}

        @Override
        public void onDataSetChanged() {
            System.out.println("onDataSetChanged");
            changeCursor(createCursor());
        }
        
        private void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null)
            {
                old.close();
            }
        }
        
        private Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor)
            {
                return null;
            }
            
            Cursor oldCursor = mCursor;
            mCursor = newCursor;
            
            return oldCursor;
        }

        @Override
        public void onDestroy() {
            if (mCursor != null)
            {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            if (mCursor != null)
            {
                return mCursor.getCount();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (mCursor == null)
            {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            
            if (!mCursor.moveToPosition(position))
            {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            
            RemoteViews views = new RemoteViews(mContext.getPackageName(), getLayoutId());
            bindView(views, mContext, mCursor);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
        
        public abstract Cursor createCursor();
        
        public abstract int getLayoutId();
        
        public abstract void bindView(RemoteViews views, Context context, Cursor cursor);
    }
}