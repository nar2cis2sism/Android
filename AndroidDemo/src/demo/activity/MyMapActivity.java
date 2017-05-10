package demo.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import demo.android.R;
import demo.search.SearchProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyMapActivity extends MapActivity implements OnClickListener {

	/**需要使用GOOGLE地图库 <uses-library android:name="com.google.android.maps" />**/
	MapView mapView;							        //地图视图
	
	View popupView;                                     //信息显示框
	
	TextView popup_title;
	TextView popup_text;
	
	MapController mc;							        //地图控制器
	
	Projection projection;						        //地图与屏幕之间的坐标转换工具
	
	MyLocation myLocation;					            //我的位置标注
	
	SearchLocation searchLocation;                      //搜索位置标注
	
	Handler handler = new Handler(){
	    public void handleMessage(android.os.Message msg) {
	        switch (msg.what) {
            case 0:
                GeoPoint gp = (GeoPoint) msg.obj;
                if (gp != null)
                {
                    Address address = getAddress(gp);
                    if (address != null)
                    {
                        showAddress(address);
                    }
                }
                
                break;
            }
	    };
	};
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.mymap);

		mapView = (MapView) findViewById(R.id.mv);
		initPopupView();
		mc = mapView.getController();
		projection = mapView.getProjection();
		
//		mapView.setTraffic(true);//设置为交通模式
//		mapView.setSatellite(true);//设置为卫星模式
//		mapView.setStreetView(true);//设置为街道模式
		mapView.setBuiltInZoomControls(true);//设置地图支持缩放
		mapView.displayZoomControls(true);//显示地图缩放的按钮
		
		GeoPoint gp = new GeoPoint(39909230, 116397428);
		mc.animateTo(gp);//移动到gp点（北京市中心）
		mc.setZoom(16);//设置缩放级别（1-21），可通过zoomIn()和zoomOut()放大或缩小
		
		//显示我的位置标注信息
		myLocation = new MyLocation(this, mapView);
		//第一次获取到我的位置时设置为地图的中心
		myLocation.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				mc.setCenter(myLocation.getMyLocation());
			}});
		mapView.getOverlays().add(myLocation);
		
		searchLocation = new SearchLocation();
		
		Button search = (Button) findViewById(R.id.search);
		search.setOnClickListener(this);
		
		ImageButton point_what = (ImageButton) findViewById(R.id.point_what);
		point_what.setOnClickListener(this);
        
        ImageButton layer = (ImageButton) findViewById(R.id.layer);
        layer.setOnClickListener(this);
		
		ImageButton location = (ImageButton) findViewById(R.id.location);
		location.setOnClickListener(this);
	}
	
	private void initPopupView()
	{
	    popupView = getLayoutInflater().inflate(R.layout.mymap_popup, null);
	    mapView.addView(popupView, new MapView.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
	    popupView.setVisibility(View.GONE);
	    
	    popup_title = (TextView) popupView.findViewById(R.id.title);
	    popup_text = (TextView) popupView.findViewById(R.id.text);
	}
	
	@Override
	public void onNewIntent(Intent newIntent) {
	    setIntent(newIntent);
        handleSearchQuery(newIntent);
	}

    private void handleSearchQuery(Intent queryIntent) {
        if (Intent.ACTION_SEARCH.equals(queryIntent.getAction())) {
            final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
            onSearch(queryString);
        }
    }

    private void onSearch(String queryString) {
        Address address = getAddress(queryString);
        if (address != null)
        {
            GeoPoint gp = getGeoByLocation(address);
            
            searchLocation.setAddress(address);
            searchLocation.setLocation(gp);
            if (!mapView.getOverlays().contains(searchLocation))
            {
                mapView.getOverlays().add(searchLocation);
            }
            
            showPopupView(gp, address);
            
            mc.setCenter(gp);
            mc.setZoom(12);
        }
        
        SearchRecentSuggestions srs = new SearchRecentSuggestions(this, SearchProvider.AUTHORITY, SearchProvider.MODE);
        srs.saveRecentQuery(queryString, null);
    }

    public void showPopupView(GeoPoint gp, Address address)
    {
        MapView.LayoutParams params = (MapView.LayoutParams) popupView.getLayoutParams();
        params.point = gp;
        mapView.updateViewLayout(popupView, params);
        showAddress(address);
        popupView.setVisibility(View.VISIBLE);
    }
    
    public void showAddress(Address address)
    {
        int lines = address.getMaxAddressLineIndex();
        if (lines > 0)
        {
            String title = "";
            String text = "";
            if (lines == 3)
            {
                title = address.getAddressLine(lines--);
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= lines; i++)
            {
                sb.append(address.getAddressLine(i));
            }
            
            text = sb.toString();
            
            if (TextUtils.isEmpty(title))
            {
                popup_title.setText(text);
                popup_text.setVisibility(View.GONE);
            }
            else
            {
                popup_title.setText(title);
                popup_text.setText(text);
                popup_text.setVisibility(View.VISIBLE);
            }
        }
    }
	
	@Override
	protected void onStart() {
		myLocation.enableMyLocation();//允许定位我的位置
		myLocation.enableCompass();//允许定位方向（罗盘仪）
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		myLocation.disableMyLocation();
		myLocation.disableCompass();
		super.onStop();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 给出地图上指定点的经纬度
	 */
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			GeoPoint gp = toGeoPoint(event);
			System.out.println("latitude:" + gp.getLatitudeE6() / 1e6);
			System.out.println("longitude:" + gp.getLongitudeE6() / 1e6);
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * 通过位置信息获取当前坐标
	 */
	
	public GeoPoint getGeoByLocation(Location loc)
	{
		GeoPoint gp = null;
		try {
			if (loc != null)
			{
				gp = new GeoPoint((int) (loc.getLatitude() * 1e6), 
						(int) (loc.getLongitude() * 1e6));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gp;
	}
	
	/**
	 * 通过地址获取当前坐标
	 */
	
	public GeoPoint getGeoByLocation(Address address)
	{
		GeoPoint gp = null;
		try {
			if (address != null)
			{
				gp = new GeoPoint((int) (address.getLatitude() * 1e6), 
						(int) (address.getLongitude() * 1e6));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gp;
	}
	
	/**
	 * 根据地图坐标获取屏幕坐标
	 */
	
	public Point fromGeoPoint(GeoPoint gp)
	{
		Point p = new Point();
		projection.toPixels(gp, p);
		return p;
	}
	
	/**
	 * 将屏幕坐标转换为地图坐标
	 */
	
	public GeoPoint toGeoPoint(MotionEvent event)
	{
        return projection.fromPixels((int) event.getX(), (int) event.getY());
	}
	
	/**
	 * 查询地址
	 */
    
    public Address getAddress(GeoPoint gp)
    {
        //根据当前系统设定语言确定编码
        Geocoder g = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> list = g.getFromLocation(gp.getLatitudeE6() / 1E6, gp.getLongitudeE6() / 1E6, 1);
            if (!list.isEmpty())
            {
                return list.get(0);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 根据地名查询地址（经纬度）
     */
    
    public Address getAddress(String locationName)
    {
        //根据当前系统设定语言确定编码
        Geocoder g = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> list = g.getFromLocationName(locationName, 1);
            if (!list.isEmpty())
            {
                return list.get(0);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
	
	private class MyLocation extends MyLocationOverlay implements OnGestureListener, OnDoubleTapListener {
	    
	    private GestureDetector detector;
	    
	    private Bitmap location_orientation;
	    
	    private boolean showMyLocation = false;
	    
	    private float bearing;
	    private GeoPoint myLocation;

        public MyLocation(Context context, MapView mapView) {
            super(context, mapView);
            
            location_orientation = BitmapFactory.decodeResource(
                    getResources(), R.drawable.map_location_orientation);
            
            detector = new GestureDetector(context, this);
        }
        
        @Override
        public synchronized boolean draw(Canvas canvas, MapView mapView,
                boolean shadow, long when) {
            super.draw(canvas, mapView, shadow, when);
            
            GeoPoint myLocation = this.myLocation;
            if (myLocation != null)
            {
                Point p = fromGeoPoint(myLocation);
                
                canvas.save();
                
                canvas.rotate(-bearing, p.x, p.y);
                canvas.drawBitmap(location_orientation, p.x - 16, p.y - 14, null);
                
                canvas.restore();
            }
            
            return true;
        }
        
        @Override
        protected void drawCompass(Canvas canvas, float bearing) {
//            super.drawCompass(canvas, bearing);
            
            this.bearing = bearing;
        }
        
        @Override
        protected void drawMyLocation(Canvas canvas, MapView mapView,
                Location lastFix, GeoPoint myLocation, long when) {
//            super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
            
            this.myLocation = myLocation;
        }
        
        @Override
        protected boolean dispatchTap() {
            showMyLocation = true;
            showPopupView(getMyLocation());
            return true;
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent e, MapView mapView) {
            return detector.onTouchEvent(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            GeoPoint gp = projection.fromPixels((int) e.getX(), (int) e.getY());
            showPopupView(gp);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            // TODO Auto-generated method stub
            return false;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (showMyLocation)
            {
                showMyLocation = false;
            }
            else
            {
                popupView.setVisibility(View.GONE);
            }
            
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mc.zoomInFixing((int) e.getX(), (int) e.getY());
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        private void showPopupView(GeoPoint gp)
        {
            MapView.LayoutParams params = (MapView.LayoutParams) popupView.getLayoutParams();
            params.point = gp;
            mapView.updateViewLayout(popupView, params);
            
            popup_title.setText("正在加载地址...");
            popup_text.setVisibility(View.GONE);
            
            popupView.setVisibility(View.VISIBLE);
            
            Message.obtain(handler, 0, gp).sendToTarget();
        }
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.search:
            //搜索
            onSearchRequested();
            break;
        case R.id.point_what:
            //标注
            if (mapView.getOverlays().contains(searchLocation))
            {
                mc.animateTo(searchLocation.getLocation());
            }
            
            break;
        case R.id.layer:
            //图层（暂时清除搜索记录）
            mapView.getOverlays().remove(searchLocation);
            
            if (popupView.getVisibility() == View.VISIBLE)
            {
                MapView.LayoutParams params = (MapView.LayoutParams) popupView.getLayoutParams();
                if (params.point == searchLocation.getLocation())
                {
                    popupView.setVisibility(View.GONE);
                }
            }
            
            mapView.invalidate();
            break;
        case R.id.location:
            //定位
            mc.animateTo(myLocation.getMyLocation());
            break;

        default:
            break;
        }
    }
    
    private class SearchLocation extends Overlay {
        
        private Bitmap point_where;
        
        private GeoPoint gp;
        private Address address;
        
        public SearchLocation() {
            point_where = BitmapFactory.decodeResource(getResources(), R.drawable.map_point_where);
        }
        
        public void setLocation(GeoPoint gp) {
            this.gp = gp;
        }
        
        public GeoPoint getLocation() {
            return gp;
        }
        
        public void setAddress(Address address) {
            this.address = address;
        }
        
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (gp != null)
            {
                Point p = fromGeoPoint(gp);
                canvas.drawBitmap(point_where, p.x - 11, p.y, null);
            }
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent e, MapView mapView) {
            if (e.getAction() == MotionEvent.ACTION_DOWN)
            {
                if (judge(new Point((int) e.getX(), (int) e.getY()), fromGeoPoint(gp)))
                {
                    showPopupView(gp, address);
                    return true;
                }
            }
            
            return false;
        }
        
        private boolean judge(Point p1, Point p2)
        {
            if (Math.abs(p1.x - p2.x) <= point_where.getWidth() 
            &&  Math.abs(p1.y - p2.y) <= point_where.getHeight())
            {
                return true;
            }
            
            return false;
        }
    }
}