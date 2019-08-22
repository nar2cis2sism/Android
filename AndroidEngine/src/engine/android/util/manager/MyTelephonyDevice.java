package engine.android.util.manager;

import static engine.android.util.manager.SDCardManager.isEnabled;
import static engine.android.util.manager.SDCardManager.openSDCardFile;

import engine.android.util.file.FileManager;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 我的手机设备<br>
 * 需要声明权限<uses-permission android:name="android.permission.READ_PHONE_STATE" />
 * 
 * @author Daimon
 * @since 4/6/2012
 */
@SuppressWarnings("deprecation")
public class MyTelephonyDevice {

    /***** 外置存储卡目录名称 *****/
    public static String EXTERNAL_SD = "external_sd";

    private final Context context;

    private final TelephonyManager tm;              // 电话管理器

    public MyTelephonyDevice(Context context) {
        tm = (TelephonyManager) (this.context = context.getApplicationContext())
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * Get device IP address
     */
    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> e_ni = NetworkInterface.getNetworkInterfaces();
                    e_ni.hasMoreElements();)
            {
                NetworkInterface ni = e_ni.nextElement();
                for (Enumeration<InetAddress> e_ia = ni.getInetAddresses(); e_ia.hasMoreElements();)
                {
                    InetAddress ia = e_ia.nextElement();
                    if (!ia.isLoopbackAddress() && ia instanceof Inet4Address)
                    {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取运行内存总量<br>
     * parse content in "/proc/meminfo", like:
     * MemTotal:** KB
     * MemFree:** KB
     */
    public static long getRunningTotalMemory() {
        try {
            String s = FileManager.readFirstLine(new File("/proc/meminfo"));
            s = s.replaceAll(":", " ");
            String[] strs = s.split("\\s+");
            return Long.parseLong(strs[1]) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 获取可用运行内存
     */
    public long getRunningFreeMemory() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo info = new MemoryInfo();
        am.getMemoryInfo(info);
        return info.availMem;
    }

    /**
     * 获取手机存储总量
     */
    public static long getTotalMemory() {
        return getTotalSpace(Environment.getDataDirectory());
    }

    public static long getUsableMemory() {
        return getUsableSpace(Environment.getDataDirectory());
    }

    /**
     * 获取手机存储余量
     */
    public static long getFreeMemory() {
        return getFreeSpace(Environment.getDataDirectory());
    }

    /**
     * 获取剩余电量（百分比）<br>
     * parse content in "/sys/class/power_supply/battery/capacity"
     */
    public static int getFreeBattery() {
        try {
            return Integer.parseInt(FileManager.readFirstLine(new File(
                    "/sys/class/power_supply/battery/capacity")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 获取电话状态<br>
     * 0:CALL_STATE_IDLE 无活动<br>
     * 1:CALL_STATE_RINGING 响铃<br>
     * 2:CALL_STATE_OFFHOOK 摘机
     */
    public int getCallState() {
        return tm.getCallState();
    }

    /**
     * 获取手机串号：唯一的设备ID<br>
     * GSM手机的IMEI和CDMA手机的MEID<br>
     * 
     * @return Return null if device ID is not available
     */
    public String getDeviceId() {
        return tm.getDeviceId();
    }

    /**
     * 获取手机号：GSM手机的MSISDN
     * 
     * @return Return null if it is unavailable
     */
    public String getPhoneNumber() {
        return tm.getLine1Number();
    }

    /**
     * 获取ISO标准的国家码，即国际长途区号（e.g. cn）<br>
     * 注意：仅当用户已在网络注册后有效，在CDMA网络中结果也许不可靠
     */
    public String getNetworkCountryIso() {
        return tm.getNetworkCountryIso();
    }

    /**
     * MCC+MNC(mobile country code + mobile network code)（e.g. 46000）<br>
     * 注意：仅当用户已在网络注册后有效，在CDMA网络中结果也许不可靠
     */
    public String getNetworkOperator() {
        return tm.getNetworkOperator();
    }

    /**
     * 按照字母次序的current registered operator(当前已注册的用户)的名字（e.g. CMCC）<br>
     * 注意：仅当用户已在网络注册后有效，在CDMA网络中结果也许不可靠
     */
    public String getNetworkOperatorName() {
        return tm.getNetworkOperatorName();
    }

    /**
     * 返回当前使用的网络类型<br>
     * 0:NETWORK_TYPE_UNKNOWN 网络类型未知<br>
     * 1:NETWORK_TYPE_GPRS GPRS网络<br>
     * 2:NETWORK_TYPE_EDGE EDGE网络<br>
     * 3:NETWORK_TYPE_UMTS UMTS网络<br>
     * 4:NETWORK_TYPE_CDMA CDMA网络,IS95A或IS95B<br>
     * 5:NETWORK_TYPE_EVDO_0 EVDO网络,revision 0<br>
     * 6:NETWORK_TYPE_EVDO_A EVDO网络,revision A<br>
     * 7:NETWORK_TYPE_1xRTT 1xRTT网络<br>
     * 8:NETWORK_TYPE_HSDPA HSDPA网络<br>
     * 9:NETWORK_TYPE_HSUPA HSUPA网络<br>
     * 10:NETWORK_TYPE_HSPA HSPA网络
     */
    public int getNetworkType() {
        return tm.getNetworkType();
    }

    /**
     * 返回手机类型<br>
     * PHONE_TYPE_NONE 无信号<br>
     * PHONE_TYPE_GSM GSM信号<br>
     * PHONE_TYPE_CDMA CDMA信号<br>
     */
    public int getPhoneType() {
        return tm.getPhoneType();
    }

    /**
     * 获取ISO国家码，相当于提供SIM卡的国家码（e.g. cn）
     */
    public String getSimCountryIso() {
        return tm.getSimCountryIso();
    }

    /**
     * 获取SIM卡提供的移动国家码和移动网络码.5或6位的十进制数字（e.g. 46002）<br>
     * SIM卡的状态必须是 SIM_STATE_READY(使用getSimState()判断)
     */
    public String getSimOperator() {
        return tm.getSimOperator();
    }

    /**
     * 返回服务商名称（e.g. CMCC）<br>
     * SIM卡的状态必须是 SIM_STATE_READY(使用getSimState()判断)
     */
    public String getSimOperatorName() {
        return tm.getSimOperatorName();
    }

    /**
     * 获取SIM卡的序列号
     */
    public String getSimSerialNumber() {
        return tm.getSimSerialNumber();
    }

    /**
     * 获取SIM的状态信息<br>
     * 0:SIM_STATE_UNKNOWN 未知状态<br>
     * 1:SIM_STATE_ABSENT 没插卡<br>
     * 2:SIM_STATE_PIN_REQUIRED 锁定状态，需要用户的PIN码解锁<br>
     * 3:SIM_STATE_PUK_REQUIRED 锁定状态，需要用户的PUK码解锁<br>
     * 4:SIM_STATE_NETWORK_LOCKED 锁定状态，需要网络的PIN码解锁<br>
     * 5:SIM_STATE_READY 就绪状态
     */
    public int getSimState() {
        return tm.getSimState();
    }

    /**
     * 返回唯一的用户ID<br>
     * IMSI(国际移动用户识别码) for a GSM phone
     */
    public String getIMSI() {
        return tm.getSubscriberId();
    }

    /**
     * 判断手机是否漫游(在GSM用途下)
     */
    public boolean isNetworkRoaming() {
        return tm.isNetworkRoaming();
    }

    /**
     * 手机是否内置存储器
     */
    public static boolean isInternalStorageEnabled() {
        return isEnabled() && !Environment.isExternalStorageRemovable();
    }

    /**
     * 手机是否外挂SD卡
     */
    public static boolean isExternalSDCardEnabled() {
        if (isEnabled())
        {
            if (Environment.isExternalStorageRemovable())
            {
                return true;
            }
            else
            {
                return openSDCardFile(EXTERNAL_SD).exists();
            }
        }

        return false;
    }

    public static long getTotalDeviceSpace() {
        return getDeviceSpace(null);
    }

    public static long getUsableDeviceSpace() {
        return getDeviceSpace(false);
    }

    public static long getFreeDeviceSpace() {
        return getDeviceSpace(true);
    }

    /**
     * 获取内置存储容量
     */
    private static long getDeviceSpace(Boolean free) {
        if (isEnabled())
        {
            if (Environment.isExternalStorageRemovable())
            {
                return 0;
            }
            else
            {
                return getFileSpace(openSDCardFile(""), free);
            }
        }

        return 0;
    }

    public static long getTotalSDCardSpace() {
        return getSDCardSpace(null);
    }

    public static long getUsableSDCardSpace() {
        return getSDCardSpace(false);
    }

    public static long getFreeSDCardSpace() {
        return getSDCardSpace(true);
    }

    /**
     * 获取SD存储卡容量
     */
    private static long getSDCardSpace(Boolean free) {
        if (isEnabled())
        {
            if (Environment.isExternalStorageRemovable())
            {
                return getFileSpace(openSDCardFile(""), free);
            }
            else
            {
                File file = openSDCardFile(EXTERNAL_SD);
                if (file.exists())
                {
                    return getFileSpace(file, free);
                }
            }
        }

        return 0;
    }

    /**
     * 获取文件系统总容量
     */
    public static long getTotalSpace(File file) {
        if (VERSION.SDK_INT < 9)
        {
            StatFs sf = new StatFs(file.getPath());
            return 1l * sf.getBlockCount() * sf.getBlockSize();
        }

        return file.getTotalSpace();
    }

    /**
     * 获取文件系统可用容量
     */
    public static long getUsableSpace(File file) {
        if (VERSION.SDK_INT < 9)
        {
            StatFs sf = new StatFs(file.getPath());
            return 1l * sf.getAvailableBlocks() * sf.getBlockSize();
        }

        return file.getUsableSpace();
    }

    /**
     * 获取文件系统剩余容量
     */
    public static long getFreeSpace(File file) {
        if (VERSION.SDK_INT < 9)
        {
            StatFs sf = new StatFs(file.getPath());
            return 1l * sf.getFreeBlocks() * sf.getBlockSize();
        }

        return file.getFreeSpace();
    }

    private static long getFileSpace(File file, Boolean free) {
        return free == null ? getTotalSpace(file) : free ? 
                getFreeSpace(file) : getUsableSpace(file);
    }
}