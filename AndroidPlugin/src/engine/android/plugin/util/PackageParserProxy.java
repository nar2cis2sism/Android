package engine.android.plugin.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.PackageUserState;

import engine.android.util.extra.ReflectObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 因为版本的不同，使用反射执行PackageParser的方法
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public class PackageParserProxy {
    
    private PackageParser obj;
    private ReflectObject ref;
    
    private Method collectCertificates;
    private static Method generatePackageInfo;
    
    public PackageParserProxy() {
        ref = new ReflectObject(obj = new PackageParser());
    }
    
    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        return obj.parsePackage(packageFile, flags);
    }
    
    public void collectCertificates(Package pkg, int flags) throws Exception {
        if (collectCertificates == null)
        {
            collectCertificates = ref.getMethod("collectCertificates", Package.class, int.class);
        }
        
        ref.invoke(collectCertificates, pkg, flags);
    }

    public static PackageInfo generatePackageInfo(Package p,
            int gids[], int flags, long firstInstallTime, long lastUpdateTime,
            Set<String> grantedPermissions, PackageUserState state, int userId) {
        if (generatePackageInfo == null)
        {
            generatePackageInfo = ReflectObject.getMethod(PackageParser.class, "generatePackageInfo", 
                    Package.class, int[].class, int.class, long.class, long.class, 
                    Set.class, PackageUserState.class, int.class);
        }
        
        return (PackageInfo) ReflectObject.invokeStaticWithoutThrow(generatePackageInfo, 
                p, gids, flags, firstInstallTime, lastUpdateTime, 
                grantedPermissions, state, userId);
    }
}