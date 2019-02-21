//package engine.android.util.manager;
//
//import android.content.Context;
//import android.location.Address;
//import android.location.Criteria;
//import android.location.Geocoder;
//import android.location.GpsSatellite;
//import android.location.GpsStatus;
//import android.location.GpsStatus.Listener;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.net.wifi.WifiInfo;
//import android.telephony.CellLocation;
//import android.telephony.NeighboringCellInfo;
//import android.telephony.TelephonyManager;
//import android.telephony.gsm.GsmCellLocation;
//import android.text.TextUtils;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.Locale;
//
///**
// * 我的定位管理器<br>
// * 需要声明权限<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
// * 
// * @author Daimon
// * @version 3.0
// * @since 3/26/2012
// */
//public class MyLocationManager {
//
//    private final Context context;
//
//    private final TelephonyManager tm;					// 电话管理器
//
//    private final LocationManager lm;					// 定位管理器
//
//    /********************* GPS定位属性 *********************/
//
//    private Listener stateListener;						// Gps状态监听器
//
//    private Criteria criteria;							// 用来自动适配定位方式
//
//    /********************* 基站定位属性 ********************/
//
//    private boolean addressAllowed;						// 是否允许获取地址
//
//    private AddressInfo address;						// 地址信息（需允许获取地址）
//
//    public MyLocationManager(Context context) {
//        this.context = context.getApplicationContext();
//        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//    }
//
//    /**
//     * 基站定位（通过Google Gears获取位置信息）
//     */
//    public Location callGear(List<CellInfo> cells, WifiInfo wifi) {
//        if (cells == null || cells.isEmpty())
//        {
//            return null;
//        }
//
//        try {
//            CellInfo info = cells.get(0);
//            // 组装JSON查询字符串
//            JSONObject holder = new JSONObject();
//            holder.put("version", "1.1.0");
//            holder.put("host", "maps.google.com");
//            holder.put("home_mobile_country_code", info.mobileCountryCode);
//            holder.put("home_mobile_network_code", info.mobileNetworkCode);
//            holder.put("radio_type", info.radioType);
//            holder.put("request_address", addressAllowed);
//            if ("460".equals(info.mobileCountryCode))
//                holder.put("address_language", "zh_CN");
//            else
//                holder.put("address_language", "en_US");
//
//            JSONArray array = new JSONArray();
//            JSONObject data = new JSONObject();
//            data.put("cell_id", info.cellId);
//            data.put("location_area_code", info.locationAreaCode);
//            data.put("mobile_country_code", info.mobileCountryCode);
//            data.put("mobile_network_code", info.mobileNetworkCode);
//            data.put("age", 0);
//            array.put(data);
//            if (cells.size() > 1)
//            {
//                ListIterator<CellInfo> iter = cells.listIterator(1);
//                while (iter.hasNext())
//                {
//                    info = iter.next();
//                    data = new JSONObject();
//                    data.put("cell_id", info.cellId);
//                    data.put("location_area_code", info.locationAreaCode);
//                    data.put("mobile_country_code", info.mobileCountryCode);
//                    data.put("mobile_network_code", info.mobileNetworkCode);
//                    data.put("age", 0);
//                    array.put(data);
//                }
//            }
//
//            holder.put("cell_towers", array);
//            if (wifi != null)
//            {
//                String mac = wifi.getBSSID();// MAC地址
//                if (mac != null)
//                {
//                    data = new JSONObject();
//                    data.put("mac_address", mac);
//                    data.put("signal_strength", 8);
//                    data.put("age", 0);
//                    array = new JSONArray();
//                    array.put(data);
//                    holder.put("wifi_towers", array);
//                }
//            }
//
//            // =============json封装成StringEntity,StringEntity存入post,post让client执行
//
//            // 创建连接，发送请求并接受回应
//            DefaultHttpClient client = new DefaultHttpClient();
//
//            HttpPost post = new HttpPost("http://www.google.com/loc/json");
//
//            StringEntity se = new StringEntity(holder.toString());
//            post.setEntity(se);
//            HttpResponse resp = client.execute(post);
//
//            HttpEntity entity = resp.getEntity();
//            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
//            StringBuilder sb = new StringBuilder();
//
//            String s;
//            while ((s = br.readLine()) != null)
//            {
//                sb.append(s);
//            }
//
//            if ((data = new JSONObject(sb.toString()).getJSONObject("location")) != null)
//            {
//                Location loc = new Location(LocationManager.NETWORK_PROVIDER);
//                loc.setLatitude((Double) data.get("latitude"));
//                loc.setLongitude((Double) data.get("longitude"));
//                loc.setTime(System.currentTimeMillis());
//                loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
//                if (addressAllowed && (data = data.getJSONObject("address")) != null)
//                {
//                    address = new AddressInfo(data);
//                }
//
//                return loc;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    /**
//     * 获取基站信息（目前只支持GSM网络）
//     */
//    public List<CellInfo> getCellInfo() {
//        CellLocation cell = tm.getCellLocation();
//        if (cell instanceof GsmCellLocation)
//        {
//            // GSM网络
//            GsmCellLocation gcl = (GsmCellLocation) cell;
//
//            String s = tm.getNetworkOperator();
//            if (TextUtils.isEmpty(s))
//            {
//                return null;
//            }
//
//            List<CellInfo> cells = new LinkedList<CellInfo>();
//            int lac = gcl.getLac();
//            String mcc = s.substring(0, 3);
//            String mnc = s.substring(3, 5);
//
//            CellInfo info = new CellInfo();
//            info.cellId = gcl.getCid();
//            info.mobileCountryCode = mcc;
//            info.mobileNetworkCode = mnc;
//            info.locationAreaCode = lac;
//            info.radioType = "gsm";
//            cells.add(info);
//
//            List<NeighboringCellInfo> list = tm.getNeighboringCellInfo();
//            if (list != null && list.size() > 1)
//            {
//                for (NeighboringCellInfo i : list)
//                {
//                    info = new CellInfo();
//                    info.cellId = i.getCid();
//                    info.mobileCountryCode = mcc;
//                    info.mobileNetworkCode = mnc;
//                    info.locationAreaCode = lac;
//                    cells.add(info);
//                }
//            }
//
//            return cells;
//        }
//        // else if (cell instanceof CdmaCellLocation)
//        // {
//        // //CDMA网络
//        // CdmaCellLocation ccl = (CdmaCellLocation) cell;
//        // }
//
//        return null;
//    }
//
//    /**
//     * 封装基站信息
//     */
//    private static class CellInfo {
//
//        int cellId;
//
//        String mobileCountryCode;
//
//        String mobileNetworkCode;
//
//        int locationAreaCode;
//
//        String radioType;
//
//    }
//
//    /**
//     * 封装地址信息
//     */
//    public static class AddressInfo {
//
//        public String country;								// 国家（中国）
//
//        public String country_code;						    // 国家编码（CN）
//
//        public String city;                                 // 城市（北京市）
//
//        public String region;								// 地区（丰台区）
//
//        public String street;								// 街道（顺源街）
//
//        public String street_number;						// 街道编号（5号）
//
//        AddressInfo(JSONObject address) {
//            try {
//                country = address.getString("country");
//                country_code = address.getString("country_code");
//                region = address.getString("region");
//                city = address.getString("city");
//                street = address.getString("street");
//                street_number = address.getString("street_number");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public String toString() {
//            return new StringBuilder()
//                    .append(country).append(city).append(region).append(street)
//                    .append(street_number).toString();
//        }
//    }
//
//    /**
//     * 注册位置监听器
//     * 
//     * @param minTime 自动更新最小时间（单位：毫秒）
//     * @param minDistance 自动更新最小距离（单位：米）
//     */
//    public void registerLocationListener(String provider, long minTime,
//            float minDistance, LocationListener listener) {
//        if (listener != null)
//        {
//            lm.requestLocationUpdates(provider, minTime, minDistance, listener);
//        }
//    }
//
//    /**
//     * 取消位置监听
//     */
//    public void unregisterLocationListener(LocationListener listener) {
//        if (listener != null)
//        {
//            lm.removeUpdates(listener);
//        }
//    }
//
//    /**
//     * 注册卫星状态监听器
//     */
//    public void registerStateListener(Listener listener) {
//        if (listener != null && stateListener == null)
//        {
//            lm.addGpsStatusListener(stateListener = listener);
//        }
//    }
//
//    /**
//     * 取消状态监听
//     */
//    public void unregisterStateListener() {
//        if (stateListener != null)
//        {
//            lm.removeGpsStatusListener(stateListener);
//            stateListener = null;
//        }
//    }
//
//    /**
//     * 自动适配GPS定位方式
//     * 
//     * @return 卫星定位返回LocationManager.GPS_PROVIDER<br>
//     *         辅助定位返回LocationManager.NETWORK_PROVIDER<br>
//     *         未开启GPS功能返回Null
//     */
//    public String getLocationProvider() {
//        if (criteria == null)
//        {
//            criteria = new Criteria();;
//            criteria.setAccuracy(Criteria.ACCURACY_FINE);// 精确度
//            criteria.setAltitudeRequired(false);// 海拔不需要
//            criteria.setBearingRequired(false);// 地轴线不需要
//            criteria.setCostAllowed(true);// 允许产生现金消费
//            criteria.setPowerRequirement(Criteria.POWER_LOW);// 耗电低
//        }
//
//        return lm.getBestProvider(criteria, true);
//    }
//
//    /**
//     * 根据定位方式获取位置信息
//     */
//    public Location getLocation(String provider) {
//        return lm.getLastKnownLocation(provider);
//    }
//
//    /**
//     * 获取卫星数量
//     * 
//     * @return int[0]:可见卫星数,int[1]:已连接卫星数
//     */
//    public int[] getSatelliteNumber(GpsStatus g) {
//        int[] num = new int[2];
//        Iterator<GpsSatellite> iter = g.getSatellites().iterator();
//        while (iter.hasNext())
//        {
//            GpsSatellite gps = iter.next();
//            if (gps != null)
//            {
//                num[0]++;
//                if (gps.usedInFix())
//                {
//                    num[1]++;
//                }
//            }
//        }
//
//        return num;
//    }
//
//    /**
//     * 获取GPS状态
//     */
//    public GpsStatus getGpsStatus() {
//        return lm.getGpsStatus(null);
//    }
//
//    /**
//     * GPS获取地址信息
//     */
//    public Address getAddress(Location loc) {
//        // 根据当前系统设定语言确定编码
//        Geocoder g = new Geocoder(context, Locale.getDefault());
//        try {
//            List<Address> list = g.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
//            if (!list.isEmpty())
//            {
//                return list.get(0);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    /**
//     * 根据地名查询地址（经纬度）
//     */
//    public Address getAddress(String locationName) {
//        // 根据当前系统设定语言确定编码
//        Geocoder g = new Geocoder(context, Locale.getDefault());
//        try {
//            List<Address> list = g.getFromLocationName(locationName, 1);
//            if (!list.isEmpty())
//            {
//                return list.get(0);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    /**
//     * 基站获取地址信息
//     */
//    public AddressInfo getAddress() {
//        return address;
//    }
//
//    /**
//     * 设置是否允许获取地址
//     */
//    public void setAddressAllowed(boolean addressAllowed) {
//        this.addressAllowed = addressAllowed;
//    }
//}