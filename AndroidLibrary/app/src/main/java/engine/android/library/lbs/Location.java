package engine.android.library.lbs;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import engine.android.core.ApplicationManager;
import engine.android.library.Library.Function;
import engine.android.library.lbs.Location.IN;
import engine.android.library.lbs.Location.OUT;

/**
 * 定位
 *
 * @author Daimon
 * @version N
 * @since 1/14/2019
 */
public class Location {

    public static class IN {

        public boolean once;                // 仅定位一次,or连续定位
    }

    public static class OUT {

        public double longitude,latitude;   // 经纬度
        public BDLocation location;

        private final LocationClient locationClient;
        private final BDAbstractLocationListener listener;

        public OUT(LocationClient locationClient, BDAbstractLocationListener listener) {
            this.locationClient = locationClient;
            this.listener = listener;
        }

        public void stopLocation() {
            locationClient.unRegisterLocationListener(listener);
            locationClient.stop();
        }
    }

    public static LocationFunction FUNCTION() {
        return new LocationFunction();
    }
}

class LocationFunction implements Function<IN, OUT> {

    @Override
    public void doFunction(final IN params, final Callback<OUT> callback) {
        // 定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        final LocationClient locationClient = new LocationClient(ApplicationManager.getMainApplication());
        // 注册监听函数
        locationClient.registerLocationListener(new BDAbstractLocationListener() {

            final OUT result = new OUT(locationClient, this);

            @Override
            public void onReceiveLocation(BDLocation location) {
                result.longitude = location.getLongitude();
                result.latitude = location.getLatitude();
                result.location = location;

                callback.doResult(result);
                if (params.once)
                {
                    result.stopLocation();
                }
            }
        });
        // 配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        if (!params.once)
        {
            // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
            locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        }
        // 可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        // 可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        // 可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(true);
        locationClient.setLocOption(locationOption);
        // 开始定位
        locationClient.start();
    }
}