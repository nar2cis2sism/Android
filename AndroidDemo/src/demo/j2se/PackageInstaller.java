package demo.j2se;

import java.io.IOException;

public class PackageInstaller {
    
    private static final String INSTALL_ACTION   = "install";
    private static final String UNINSTALL_ACTION = "uninstall";
    
    public void installApp(String appPath)
    {
        try {
            Runtime.getRuntime().exec("pm install -r " + appPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void uninstallApp(String packageName)
    {
        try {
            Runtime.getRuntime().exec("pm uninstall " + packageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        if (args == null || args.length < 2)
        {
            return;
        }
        
        PackageInstaller pi = new PackageInstaller();
        if (INSTALL_ACTION.equals(args[0]))
        {
            pi.installApp(args[1]);
        }
        else if (UNINSTALL_ACTION.equals(args[0]))
        {
            pi.uninstallApp(args[1]);
        }
    }
}