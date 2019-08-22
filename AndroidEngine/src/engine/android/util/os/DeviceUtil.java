package engine.android.util.os;

import android.os.Build;
import android.os.Build.VERSION;

import java.util.UUID;

/**
 * Android设备工具类
 * 
 * @author Daimon
 * @since 3/26/2012
 */
public final class DeviceUtil {

    @SuppressWarnings("deprecation")
    public static void test() {
        System.out.println("--------" + Build.BOARD); // "MLA-AL10"
        System.out.println("--------" + Build.BRAND); // "HUAWEI"
        System.out.println("--------" + Build.CPU_ABI); // "arm64-v8a"
        System.out.println("--------" + Build.DEVICE); // "HWMLA"
        System.out.println("--------" + Build.DISPLAY); // "MLA-AL10C00B352"
        System.out.println("--------" + Build.HOST); // "wuhjk0407cna"
        System.out.println("--------" + Build.ID); // "HUAWEIMLA-AL10"
        System.out.println("--------" + Build.MANUFACTURER); // "HUAWEI"
        System.out.println("--------" + Build.MODEL); // "HUAWEI MLA-AL10"
        System.out.println("--------" + Build.PRODUCT); // "MLA-AL10"
        System.out.println("--------" + Build.TAGS); // "release-keys"
        System.out.println("--------" + Build.TYPE); // "user"
        System.out.println("--------" + Build.USER); // "test"
    }

    /**
     * 获取设备唯一标识（无需读取手机状态权限）
     *
     * @return "00000000-5a73-78fc-ffff-ffffed4b375f"
     */
    public static String getDeviceId() {
        String serial = String.valueOf(Build.SERIAL);
        String display = String.valueOf(Build.DISPLAY);
        return new UUID(display.hashCode(), serial.hashCode()).toString();
    }

    /**
     * 获取设备信息
     *
     * @return "HUAWEI MLA-AL10(7.0)"
     */
    public static String getDeviceInfo() {
        return String.format("%s(%s)", Build.MODEL, VERSION.RELEASE);
    }
}
