package engine.android.library;

import engine.android.framework.ui.BaseActivity;
import engine.android.util.os.LocationUtil;
import engine.android.util.os.PermissionUtil;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

public class TestActivity extends BaseActivity implements View.OnClickListener {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        setupView();
    }

    private void setupView() {
        text = findViewById(R.id.text);
        findViewById(R.id.qq).setOnClickListener(this);
        findViewById(R.id.weixin).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.location).setOnClickListener(this);
    }

    @Override
    protected void onDestroy(boolean finish) {
        stopLocation();
        super.onDestroy(finish);
    }

    void showText(final String s) {
        System.out.println(s);
        text.post(new Runnable() {
            @Override
            public void run() {
                text.setText(s);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.qq)
        {
            authorize(QQ.NAME);
        }
        else if (id == R.id.weixin)
        {
            authorize(Wechat.NAME);
        }
        else if (id == R.id.share)
        {
            share();
        }
        else if (id == R.id.location)
        {
            LocationUtil location = new LocationUtil(this);
            if (location.isGpsEnabled())
            {
                requestPermission(new PermissionCallback() {
                    @Override
                    public void onGrant(PermissionUtil permission, boolean success) {
                        if (success)
                        {
                            location();
                        }
                        else
                        {
                            permission.showTipDialog();
                        }
                    }
                }, Manifest.permission.ACCESS_FINE_LOCATION);
            }
            else
            {
                location.showTipDialog();
            }
        }
    }

    private void authorize(String name) {
        final StringBuilder sb = new StringBuilder();

        Platform platform = ShareSDK.getPlatform(name);
        platform.removeAccount(true); // 移除授权状态和本地缓存，下次授权会重新授权
        platform.SSOSetting(false); // SSO授权，传false默认是客户端授权，没有客户端授权或者不支持客户端授权会跳web授权

        if (platform.isAuthValid()) {
            // 判断是否已经存在授权状态，可以根据自己的登录逻辑设置
            Toast.makeText(this, "已经授权过了", Toast.LENGTH_SHORT).show();
            return;
        }

        if (platform.isClientValid()) {
            // 判断是否存在授权凭条的客户端，true是有客户端，false是无
            sb.append("有客户端\n");
        }

        // 授权回调监听
        platform.setPlatformActionListener(new PlatformActionListener() {

            // 回调信息，可以在这里获取基本的授权返回的信息，但是注意如果做提示和UI操作要传到主线程handler里去执行
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                sb.append(platform.getDb().exportData()).append("\n");
                sb.append(hashMap);
                showText(sb.toString());
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                sb.append(throwable);
                showText(sb.toString());
            }

            @Override
            public void onCancel(Platform platform, int i) {
                sb.append("取消授权");
                showText(sb.toString());
            }
        });

//        platform.authorize(); // 要功能，不要数据
        platform.showUser(null); // 要数据不要功能，主要体现在不会重复出现授权界面
    }

    private void share() {
        OnekeyShare oks = new OnekeyShare();
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();
        // 隐藏QQ空间分享
        oks.addHiddenPlatform(QZone.NAME);
        // 对于微信好友必须在分享完成之后的弹出框选择返回APP，才能获取成功回调，否则点击留在微信则不能。
        // 对于微信朋友圈如果分享成功则会直接回调APP，执行到成功的回调
        // 总结：没什么卵用
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                showText("分享完成");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                showText(throwable.toString());
            }

            @Override
            public void onCancel(Platform platform, int i) {
                // 微信客户端版本从6.7.2以上开始，取消分享提示分享成功；即取消分享和分享成功都返回成功事件
                showText("取消分享");
            }
        });
        // title标题，微信、QQ和QQ空间等平台使用
        oks.setTitle("帮好友答题，助TA赢道具");
        // titleUrl QQ和QQ空间跳转链接
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("在玩同学战，你敢来挑战吗？");
        // 两个一起设置可显示注册在微信平台上的LOGO图片
        oks.setImagePath(" ");
        oks.setImageData(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));
        // url在微信、微博，Facebook等平台中使用
        oks.setUrl("http://sharesdk.cn");
        // 启动分享GUI
        oks.show(this);
    }

    LocationClient locationClient;
    private void location() {
        stopLocation();
        // 定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        locationClient = new LocationClient(getApplicationContext());
        // 注册监听函数
        locationClient.registerLocationListener(new BDAbstractLocationListener() {

            int index = 1;

            @Override
            public void onReceiveLocation(BDLocation location) {
                // 此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                // 以下只列举部分获取经纬度相关（常用）的结果信息
                // 更多结果信息获取说明，请参照类参考中BDLocation类中的说明

                // 获取经度信息
                double longitude = location.getLongitude();
                // 获取纬度信息
                double latitude = location.getLatitude();
                // 获取定位精度，默认值为0.0f
                float radius = location.getRadius();
                // 获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
                String coorType = location.getCoorType();
                // 获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
                int errorCode = location.getLocType();

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("第%d次定位", index++)).append("\n");
                sb.append("errorCode:").append(errorCode).append("\n");
                sb.append("errorMsg:").append(location.getLocTypeDescription()).append("\n");
                sb.append("longitude:").append(longitude).append("\n");
                sb.append("latitude:").append(latitude).append("\n");
                sb.append("radius:").append(radius).append("\n");
                sb.append("coorType:").append(coorType).append("\n");
                sb.append("address:").append(location.getAddrStr()).append("\n");
                sb.append("location:").append(location.getLocationDescribe()).append("\n");

                showText(sb.toString());
            }
        });
        // 配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
        // 可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
        // 可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        // 可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        // 可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
        // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
        // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
        // 可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
        // 可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
        // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
        // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        // 开始定位
        locationClient.setLocOption(locationOption);
        locationClient.start();
    }

    private void stopLocation() {
        if (locationClient != null)
        {
            locationClient.stop();
            locationClient = null;
        }
    }
}
