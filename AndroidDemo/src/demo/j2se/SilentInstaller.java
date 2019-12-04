package demo.j2se;

import android.content.Context;
import android.text.TextUtils;

import engine.android.util.AndroidUtil;
import engine.android.util.Util;
import engine.android.util.os.ShellUtil;
import engine.android.util.os.ShellUtil.CommandResult;

import java.io.File;

public class SilentInstaller {

    /**
     * App installation location flags of android system
     */
    public static final int APP_INSTALL_AUTO     = 0;
    public static final int APP_INSTALL_INTERNAL = 1;
    public static final int APP_INSTALL_EXTERNAL = 2;

    private final Context context;

    public SilentInstaller(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Default pm install params is "-r"
     * 
     * @see #installSilent(String, String)
     */

    public void installSilent(String apkPath) throws Exception {
        installSilent(apkPath, " -r " + getInstallLocationParam());
    }

    /**
     * Install package silent by root
     * <ul>
     * <strong>Attentions:</strong>
     * <li>Don't call this on the ui thread, it may costs some times.</li>
     * <li>You should add <strong>android.permission.INSTALL_PACKAGES</strong>
     * in manifest, so no need to request root permission, if you are system
     * app.</li>
     * </ul>
     */

    public void installSilent(String apkPath, String installParam) throws Exception {
        if (TextUtils.isEmpty(apkPath))
        {
            throw new Exception("INSTALL_FAILED_INVALID_URI");
        }

        File file = new File(apkPath);
        if (!file.exists() || !file.isFile())
        {
            throw new Exception("INSTALL_FAILED_INVALID_URI");
        }

        StringBuilder command = new StringBuilder()
                .append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install ")
                .append(installParam == null ? "" : installParam).append(" ")
                .append(apkPath.replace(" ", "\\ "));
        CommandResult result = new CommandResult();
        if (ShellUtil.exeCommand(new String[] { command.toString() },
                !AndroidUtil.isSystemApplication(context), result))
        {
            return;
        }

        throw new Exception(result.getErrorMsg());
    }

    private String getInstallLocationParam() {
        int location = getInstallLocation();
        switch (location) {
            case APP_INSTALL_INTERNAL:
                return "-f";
            case APP_INSTALL_EXTERNAL:
                return "-s";

            default:
                return "";
        }
    }

    /**
     * 获取当前系统安装应用的默认位置
     * 
     * @return {@link #APP_INSTALL_AUTO} or {@link #APP_INSTALL_INTERNAL} or
     *         {@link #APP_INSTALL_EXTERNAL}
     */

    public static int getInstallLocation() {
        int res = APP_INSTALL_AUTO;
        String[] commands = { "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm get-install-location" };
        CommandResult result = new CommandResult();
        ShellUtil.exeCommand(commands, false, result);
        if (result.getResult() == 0 && !TextUtils.isEmpty(result.getResponseMsg()))
        {
            res = Util.getInt(result.getResponseMsg().substring(0, 1), res);
        }

        return res;
    }

    /**
     * Uninstall package silent by root
     * <ul>
     * <strong>Attentions:</strong>
     * <li>Don't call this on the ui thread, it may costs some times.</li>
     * <li>You should add <strong>android.permission.DELETE_PACKAGES</strong> in
     * manifest, so no need to request root permission, if you are system app.</li>
     * </ul>
     * 
     * @param keepData whether keep the data and cache directories around after
     *            package removal
     */

    public void uninstallSilent(String packageName, boolean keepData) throws Exception {
        if (TextUtils.isEmpty(packageName))
        {
            throw new Exception("DELETE_FAILED_INVALID_PACKAGE");
        }

        StringBuilder command = new StringBuilder()
                .append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall")
                .append(keepData ? " -k " : " ")
                .append(packageName.replace(" ", "\\ "));
        CommandResult result = new CommandResult();
        if (ShellUtil.exeCommand(new String[] { command.toString() },
                !AndroidUtil.isSystemApplication(context), result))
        {
            return;
        }

        throw new Exception(result.getErrorMsg());
    }
}