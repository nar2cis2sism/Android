package demo.activity.test;

import android.bluetooth.BluetoothAdapter;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import demo.android.util.SystemUtil;

import engine.android.util.AndroidUtil;
import engine.android.util.Util;
import engine.android.util.manager.MyTelephonyDevice;
import engine.android.util.manager.MyWifiManager;
import engine.android.util.manager.SDCardManager;
import engine.android.util.manager.SDCardManager.SDCardInfo;

import java.io.File;
import java.net.InetAddress;

public class TestOnHardWare extends TestOnBase {
    
    MyTelephonyDevice td;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        td = new MyTelephonyDevice(this);
        
        log("机器型号：" + android.os.Build.MODEL);
        log("系统版本：Android " + android.os.Build.VERSION.RELEASE);
        log("手机制造商：" + android.os.Build.MANUFACTURER);
        log("手机运营商：" + android.os.Build.BRAND);
        log("手机串号：" + td.getDeviceId());
        boolean root = false;
        try
        {
            root = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        log("root权限：" + (root ? "已获取" : "未获取"));
        
        log("");
        
        log(String.format("运行内存总量：%s（剩余：%s）", getSize(MyTelephonyDevice.getRunningTotalMemory()), getSize(td.getRunningFreeMemory())));
        log(String.format("手机内存总量：%s（剩余：%s）", getSize(MyTelephonyDevice.getTotalMemory()), getSize(MyTelephonyDevice.getFreeMemory())));
        if (MyTelephonyDevice.isInternalStorageEnabled())
        {
            log(String.format("内置存储容量：%s（剩余：%s）", getSize(MyTelephonyDevice.getTotalDeviceSpace()), getSize(MyTelephonyDevice.getFreeDeviceSpace())));
        }
        
        if (MyTelephonyDevice.isExternalSDCardEnabled())
        {
            log(String.format("SD存储卡容量：%s（剩余：%s）", getSize(MyTelephonyDevice.getTotalSDCardSpace()), getSize(MyTelephonyDevice.getFreeSDCardSpace())));
        }
        else
        {
            log("未插入SD卡");
        }
        log("应用程序可分配内存：" + AndroidUtil.getAllocatedMemory(this) + "MB");
        log("应用程序最大可用内存：" + getSize(Runtime.getRuntime().maxMemory()));
        
        log("");

//        DisplayMetrics dm = AndroidUtil.getResolution(this);
//        IntegerSwapper swap = Util.swapWidthAndHeight(dm.widthPixels, dm.heightPixels);
//        log("屏幕分辨率：" + swap.min + "*" + swap.max + "(" 
//        + categoryBySize() + "|" + categoryByLong() + "|" + categoryByDensity(dm.density) + ")");
//        log("像素密度：" + dm.densityDpi + " PPI");
        log("物理尺寸：" + AndroidUtil.getScreenSize(this) + "英寸");
        log("是否平板：" + SystemUtil.isTablet(this));
        log("");

        log("电量：" + MyTelephonyDevice.getFreeBattery() + "%");
        
        InetAddress ia = MyTelephonyDevice.getLocalIpAddress();
        log("device IP address：" + (ia == null ? "" : ia.getHostAddress()));
        
        MyWifiManager wm = new MyWifiManager(this);
        if (wm.isWifiEnabled())
        {
            WifiInfo wi = wm.getWifiInfo();
            if (wi != null)
            {
                log("WiFi 连接到：" + wi.getSSID());
                log("WiFi 地址：" + Formatter.formatIpAddress(wi.getIpAddress()));
                log("WiFi 连接速度：" + wi.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
                log("WiFi MAC地址：" + wi.getMacAddress());
            }
        }
        else
        {
            log("WiFi状态：" + "未开启");
        }
        
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled())
        {
            log("蓝牙状态：" + "未开启");
        }
        else
        {
            log("蓝牙名称：" + adapter.getName());
            log("蓝牙MAC地址：" + adapter.getAddress());
        }

        log("运营商：" + getNetworkOperatorName());
        log("手机号：" + td.getPhoneNumber());

        log("");
        //根级目录
        log("根级目录");
        log("getRootDirectory：" + Environment.getRootDirectory());                          // /system
        log("getDataDirectory：" + Environment.getDataDirectory());                          // /data
        log("getDownloadCacheDirectory：" + Environment.getDownloadCacheDirectory());        // /cache
        //包目录
        log("包目录");
        log("getFilesDir：" + getFilesDir());                                                // /data/data/package/files
        log("getDir：" + getDir("name", 0));                                                 // /data/data/package/app_name
        log("getCacheDir：" + getCacheDir());                                                // /data/data/package/cache
        log("getFileStreamPath：" + getFileStreamPath("name"));                              // /data/data/package/files/name
        log("getDatabasePath：" + getDatabasePath("name"));                                  // /data/data/package/databases/name
        //SD卡根级目录
        log("SD卡根级目录");
        log("openSDCardFile：" + SDCardManager.openSDCardFile(""));                          // /storage/emulated/0
        log("getExternalStoragePublicDirectory：" + 
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));    // /storage/emulated/0/Download
        //SD卡包目录
        log("SD卡包目录");
        log("getExternalFilesDir：" + getExternalFilesDir(Environment.DIRECTORY_DCIM));      // /storage/emulated/0/Android/data/package/files/DCIM
        log("getExternalCacheDir：" + getExternalCacheDir());                                // /storage/emulated/0/Android/data/package/cache

        log("");
        SDCardInfo[] infos = SDCardManager.getAvailableSDCard();
        for (SDCardInfo info : infos)
        {
            log(info.isSDCard() ? "外接SD卡" : "内置存储器" + "\n" +  Util.toString(info));
        }
        
        showContent();
    }
    
    private String getNetworkOperatorName()
    {
        String number = td.getNetworkOperator();
        if (!TextUtils.isEmpty(number))
        {
            if (number.endsWith("00") || number.endsWith("02"))
            {
                return "中国移动";
            }
            else if (number.endsWith("01"))
            {
                return "中国联通";
            }
            else if (number.endsWith("03"))
            {
                return "中国电信";
            }
            else
            {
                return "其他服务商";
            }
        }
        
        return null;
    }
    
    private String getSize(long value)
    {
        return Formatter.formatFileSize(this, value);
    }
    
    private String categoryBySize()
    {
        int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenLayout == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            return "小屏";
        }
        else if (screenLayout == Configuration.SCREENLAYOUT_SIZE_NORMAL)
        {
            return "中屏";
        }
        else if (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE)
        {
            return "大屏";
        }
        else if (screenLayout > Configuration.SCREENLAYOUT_SIZE_LARGE)
        {
            return "超大屏";
        }
        else
        {
            return "不知道";
        }
    }
    
    private String categoryByLong()
    {
        int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
        if (screenLayout == Configuration.SCREENLAYOUT_LONG_NO)
        {
            return "非宽屏";
        }
        else if (screenLayout == Configuration.SCREENLAYOUT_LONG_YES)
        {
            return "宽屏";
        }
        else
        {
            return "不知道";
        }
    }
    
    private String categoryByDensity(float density)
    {
        System.out.println("density:" + density);
        if (density <= 0.75)
        {
            return "低清";
        }
        else if (density == 1)
        {
            return "普清";
        }
        else if (density >= 2)
        {
            return "超清";
        }
        else if (density >= 1.5)
        {
            return "高清";
        }
        else
        {
            return "默认普清";
        }
    }
}