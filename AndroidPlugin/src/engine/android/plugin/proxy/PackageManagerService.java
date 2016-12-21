package engine.android.plugin.proxy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.android.internal.app.ResolverActivity;

import engine.android.plugin.Plugin;
import engine.android.plugin.PluginLog;
import engine.android.plugin.PluginManager;
import engine.android.plugin.util.IntentResolver;
import engine.android.plugin.util.PluginProxy;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PackageManagerService extends PluginProxy<IPackageManager> {

    final DisplayMetrics mMetrics;

    ApplicationInfo mAndroidApplication;
    final ActivityInfo mResolveActivity = new ActivityInfo();
    final ResolveInfo mResolveInfo = new ResolveInfo();
    ComponentName mResolveComponentName;

    // ----------------------------------------------------------------

    // Keys are String (package name), values are Package.  This also serves
    // as the lock for the global state.  Methods that must be called with
    // this lock held have the prefix "LP".
    final HashMap<String, PackageParser.Package> mPackages =
            new HashMap<String, PackageParser.Package>();

    // All available activities, for your resolving pleasure.
    final ActivityIntentResolver mActivities =
            new ActivityIntentResolver();

    // All available receivers, for your resolving pleasure.
    final ActivityIntentResolver mReceivers =
            new ActivityIntentResolver();

    // All available services, for your resolving pleasure.
    final ServiceIntentResolver mServices = new ServiceIntentResolver();

    // Keys are String (provider class name), values are Provider.
    final HashMap<ComponentName, PackageParser.Provider> mProvidersByComponent =
            new HashMap<ComponentName, PackageParser.Provider>();

    // Mapping from provider base names (first directory in content URI codePath)
    // to the provider information.
    final HashMap<String, PackageParser.Provider> mProviders =
            new HashMap<String, PackageParser.Provider>();

    // Mapping from instrumentation class names to info about them.
    final HashMap<ComponentName, PackageParser.Instrumentation> mInstrumentation =
            new HashMap<ComponentName, PackageParser.Instrumentation>();

    // Mapping from permission names to info about them.
    final HashMap<String, PackageParser.PermissionGroup> mPermissionGroups =
            new HashMap<String, PackageParser.PermissionGroup>();

    // Mapping from permission names to info about them.
    final HashMap<String, PackageParser.Permission> mPermissions =
            new HashMap<String, PackageParser.Permission>();
    
    // Broadcast actions that are only available to the system.
    final HashSet<String> mProtectedBroadcasts = new HashSet<String>();

    public PackageManagerService(IPackageManager obj) throws NameNotFoundException {
        super(obj);
        
        mMetrics = new DisplayMetrics();

        WindowManager wm = (WindowManager) Plugin.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        d.getMetrics(mMetrics);
        
        try {
            mAndroidApplication = obj.getApplicationInfo("android", 0, Plugin.getUserId());
        } catch (RemoteException e) {
            // Should not be arrived.
            throw new RuntimeException(e);
        }
        
        mResolveActivity.applicationInfo = mAndroidApplication;
        mResolveActivity.name = ResolverActivity.class.getName();
        mResolveActivity.packageName = mAndroidApplication.packageName;
        mResolveActivity.processName = "system:ui";
        mResolveActivity.launchMode = ActivityInfo.LAUNCH_MULTIPLE;
        mResolveActivity.flags = ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS;
        mResolveActivity.theme = com.android.internal.R.style.Theme_Holo_Dialog_Alert;
        mResolveActivity.exported = true;
        mResolveActivity.enabled = true;
        mResolveInfo.activityInfo = mResolveActivity;
        mResolveInfo.priority = 0;
        mResolveInfo.preferredOrder = 0;
        mResolveInfo.match = 0;
        mResolveComponentName = new ComponentName(
                mAndroidApplication.packageName, mResolveActivity.name);
    }
    
    public PackageParser.Package scanPackage(File scanFile) throws Exception {
        int parseFlags = PackageParser.PARSE_MUST_BE_APK;
        
        String scanPath = scanFile.getPath();
        PackageParser pp = new PackageParser(scanPath);
        final PackageParser.Package pkg = pp.parsePackage(scanFile,
                scanPath, mMetrics, parseFlags);
        if (pkg == null)
        {
            return null;
        }
        
        // Verify certificates against what was last scanned
        if (!pp.collectCertificates(pkg, parseFlags))
        {
            throw new Exception("Failed verifying certificates for package:" + pkg.packageName);
        }
        
        // Set application objects path explicitly.
        setApplicationInfoPaths(pkg);

        return scanPackage(pkg);
    }

    private static void setApplicationInfoPaths(PackageParser.Package pkg) {
        ApplicationInfo applicationInfo = pkg.applicationInfo;
        applicationInfo.publicSourceDir = applicationInfo.sourceDir = pkg.mScanPath;
    }

    private PackageParser.Package scanPackage(PackageParser.Package pkg) throws Exception {
        if (mPackages.containsKey(pkg.packageName))
        {
            throw new Exception("Application package " + pkg.packageName
                    + " already pluginned.");
        }
        
        // Only system apps can use these features.
        pkg.mOriginalPackages = null;
        pkg.mRealPackage = null;
        pkg.mAdoptPermissions = null;

        // writer
        synchronized (mPackages) {
            // Verify that this new package doesn't have any content providers
            // that conflict with existing packages.
            for (PackageParser.Provider p : pkg.providers)
            {
                if (p.info.authority != null)
                {
                    String names[] = p.info.authority.split(";");
                    for (String name : names)
                    {
                        if (mProviders.containsKey(name))
                        {
                            PackageParser.Provider other = mProviders.get(name);
                            throw new Exception("Can't plugin because provider name " + name +
                                    " (in package " + pkg.applicationInfo.packageName +
                                    ") is already used by "
                                    + ((other != null && other.getComponentName() != null)
                                            ? other.getComponentName().getPackageName() : "?"));
                        }
                    }
                }
            }
        }

        pkg.applicationInfo.processName = fixProcessName(
                pkg.applicationInfo.packageName,    // 插件有单独进程吗？
                pkg.applicationInfo.processName);
        
        File dataPath = getDataPathForPackage(pkg.packageName);
        dataPath.mkdirs();
        pkg.applicationInfo.dataDir = dataPath.getPath();

        // writer
        synchronized (mPackages) {
            // Add the new setting to mPackages
            mPackages.put(pkg.applicationInfo.packageName, pkg);

            for (PackageParser.Provider p : pkg.providers)
            {
                p.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        p.info.processName);
                mProvidersByComponent.put(new ComponentName(p.info.packageName,
                        p.info.name), p);
                p.syncable = p.info.isSyncable;
                if (p.info.authority != null)
                {
                    String names[] = p.info.authority.split(";");
                    p.info.authority = null;
                    for (int j = 0; j < names.length; j++)
                    {
                        if (j == 1 && p.syncable)
                        {
                            // We only want the first authority for a provider to possibly be
                            // syncable, so if we already added this provider using a different
                            // authority clear the syncable flag. We copy the provider before
                            // changing it because the mProviders object contains a reference
                            // to a provider that we don't want to change.
                            // Only do this for the second authority since the resulting provider
                            // object can be the same for all future authorities for this provider.
                            p = new PackageParser.Provider(p);
                            p.syncable = false;
                        }
                        
                        if (!mProviders.containsKey(names[j]))
                        {
                            mProviders.put(names[j], p);
                            if (p.info.authority == null)
                            {
                                p.info.authority = names[j];
                            }
                            else
                            {
                                p.info.authority = p.info.authority + ";" + names[j];
                            }
                        }
                        else
                        {
                            PackageParser.Provider other = mProviders.get(names[j]);
                            PluginLog.debug("scanPackage", "Skipping provider name " + names[j] +
                                    " (in package " + pkg.applicationInfo.packageName +
                                    "): name already used by "
                                    + ((other != null && other.getComponentName() != null)
                                            ? other.getComponentName().getPackageName() : "?"));
                        }
                    }
                }
            }

            for (PackageParser.Service s : pkg.services)
            {
                s.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        s.info.processName);
                mServices.addService(s);
            }

            for (PackageParser.Activity a : pkg.receivers)
            {
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName);
                mReceivers.addActivity(a, "receiver");
            }

            for (PackageParser.Activity a : pkg.activities)
            {
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName);
                mActivities.addActivity(a, "activity");
            }

            for (PackageParser.PermissionGroup pg : pkg.permissionGroups)
            {
                PackageParser.PermissionGroup cur = mPermissionGroups.get(pg.info.name);
                if (cur == null)
                {
                    mPermissionGroups.put(pg.info.name, pg);
                }
                else
                {
                    PluginLog.debug("Permission group " + pg.info.name + " from package "
                            + pg.info.packageName + " ignored: original from "
                            + cur.info.packageName);
                }
            }

            for (PackageParser.Permission p : pkg.permissions)
            {
                PackageParser.Permission cur = mPermissions.get(p.info.name);
                if (cur == null)
                {
                    p.group = mPermissionGroups.get(p.info.group);
                    mPermissions.put(p.info.name, p);
                }
                else
                {
                    PluginLog.debug("Permission " + p.info.name + " from package "
                            + p.info.packageName + " ignored: original from "
                            + cur.info.packageName);
                }
            }

            for (PackageParser.Instrumentation a : pkg.instrumentation)
            {
                a.info.packageName = pkg.applicationInfo.packageName;
                a.info.sourceDir = pkg.applicationInfo.sourceDir;
                a.info.publicSourceDir = pkg.applicationInfo.publicSourceDir;
                a.info.dataDir = pkg.applicationInfo.dataDir;
                a.info.nativeLibraryDir = pkg.applicationInfo.nativeLibraryDir;
                mInstrumentation.put(a.getComponentName(), a);
            }

            if (pkg.protectedBroadcasts != null)
            {
                for (String s : pkg.protectedBroadcasts)
                {
                    mProtectedBroadcasts.add(s);
                }
            }
        }
        
        return pkg;
    }

    private static String fixProcessName(String defProcessName, String processName) {
        return processName == null ? defProcessName :  processName;
    }

    private File getDataPathForPackage(String packageName) {
        return new File(PluginManager.getInstance().getPluginDataDir(), packageName);
    }
    
    public void removePackage(PackageParser.Package pkg) {
        // writer
        synchronized (mPackages) {
            mPackages.remove(pkg.applicationInfo.packageName);
            cleanPackageDataStructures(pkg);
        }
    }
    
    private void cleanPackageDataStructures(PackageParser.Package pkg) {
        for (PackageParser.Provider p : pkg.providers)
        {
            mProvidersByComponent.remove(new ComponentName(p.info.packageName,
                    p.info.name));
            if (p.info.authority == null)
            {

                /* There was another ContentProvider with this authority when
                 * this app was installed so this authority is null,
                 * Ignore it as we don't have to unregister the provider.
                 */
                continue;
            }

            String names[] = p.info.authority.split(";");
            for (String name : names)
            {
                if (mProviders.get(name) == p)
                {
                    mProviders.remove(name);
                }
            }
        }

        for (PackageParser.Service s : pkg.services)
        {
            mServices.removeService(s);
        }

        for (PackageParser.Activity a : pkg.receivers)
        {
            mReceivers.removeActivity(a, "receiver");
        }

        for (PackageParser.Activity a : pkg.activities)
        {
            mActivities.removeActivity(a, "activity");
        }
    }
    
    private final class ActivityIntentResolver
            extends IntentResolver<PackageParser.ActivityIntentInfo, ResolveInfo> {
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType,
                boolean defaultOnly, int userId) {
            mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags,
                int userId) {
            mFlags = flags;
            return super.queryIntent(intent, resolvedType,
                    (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0, userId);
        }

        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType,
                int flags, ArrayList<PackageParser.Activity> packageActivities, int userId) {
            if (packageActivities == null) {
                return null;
            }
            mFlags = flags;
            final boolean defaultOnly = (flags&PackageManager.MATCH_DEFAULT_ONLY) != 0;
            final int N = packageActivities.size();
            ArrayList<PackageParser.ActivityIntentInfo[]> listCut =
                new ArrayList<PackageParser.ActivityIntentInfo[]>(N);

            ArrayList<PackageParser.ActivityIntentInfo> intentFilters;
            for (int i = 0; i < N; ++i) {
                intentFilters = packageActivities.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    PackageParser.ActivityIntentInfo[] array =
                            new PackageParser.ActivityIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        public final void addActivity(PackageParser.Activity a, String type) {
            mActivities.put(a.getComponentName(), a);
            for (PackageParser.ActivityIntentInfo intent : a.intents)
            {
                if (intent.getPriority() > 0 && "activity".equals(type))
                {
                    intent.setPriority(0);
                }
                
                addFilter(intent);
            }
        }

        public final void removeActivity(PackageParser.Activity a, String type) {
            mActivities.remove(a.getComponentName());
            for (PackageParser.ActivityIntentInfo intent : a.intents)
            {
                removeFilter(intent);
            }
        }

        @Override
        protected boolean allowFilterResult(
                PackageParser.ActivityIntentInfo filter, List<ResolveInfo> dest) {
            ActivityInfo filterAi = filter.activity.info;
            for (int i=dest.size()-1; i>=0; i--) {
                ActivityInfo destAi = dest.get(i).activityInfo;
                if (destAi.name == filterAi.name
                        && destAi.packageName == filterAi.packageName) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected ActivityIntentInfo[] newArray(int size) {
            return new ActivityIntentInfo[size];
        }

        @Override
        protected boolean isFilterStopped(PackageParser.ActivityIntentInfo filter, int userId) {
            return false;
        }

        @Override
        protected String packageForFilter(PackageParser.ActivityIntentInfo info) {
            return info.activity.owner.packageName;
        }
        
        @Override
        protected ResolveInfo newResult(PackageParser.ActivityIntentInfo info,
                int match, int userId) {
            final PackageParser.Activity activity = info.activity;
            ActivityInfo ai = PackageParser.generateActivityInfo(activity, mFlags,
                    new PackageUserState(), userId);
            if (ai == null) {
                return null;
            }
            final ResolveInfo res = new ResolveInfo();
            res.activityInfo = ai;
            if ((mFlags&PackageManager.GET_RESOLVED_FILTER) != 0) {
                res.filter = info;
            }
            res.priority = info.getPriority();
            res.preferredOrder = activity.owner.mPreferredOrder;
            //System.out.println("Result: " + res.activityInfo.className +
            //                   " = " + res.priority);
            res.match = match;
            res.isDefault = info.hasDefault;
            res.labelRes = info.labelRes;
            res.nonLocalizedLabel = info.nonLocalizedLabel;
            res.icon = info.icon;
            return res;
        }

        @Override
        protected void sortResults(List<ResolveInfo> results) {
            Collections.sort(results, mResolvePrioritySorter);
        }

        @Override
        protected void dumpFilter(PrintWriter out, String prefix,
                PackageParser.ActivityIntentInfo filter) {
            out.print(prefix); out.print(
                    Integer.toHexString(System.identityHashCode(filter.activity)));
                    out.print(' ');
                    out.print(filter.activity.getComponentShortName());
                    out.print(" filter ");
                    out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

//        List<ResolveInfo> filterEnabled(List<ResolveInfo> resolveInfoList) {
//            final Iterator<ResolveInfo> i = resolveInfoList.iterator();
//            final List<ResolveInfo> retList = Lists.newArrayList();
//            while (i.hasNext()) {
//                final ResolveInfo resolveInfo = i.next();
//                if (isEnabledLP(resolveInfo.activityInfo)) {
//                    retList.add(resolveInfo);
//                }
//            }
//            return retList;
//        }

        // Keys are String (activity class name), values are Activity.
        private final HashMap<ComponentName, PackageParser.Activity> mActivities
                = new HashMap<ComponentName, PackageParser.Activity>();
        private int mFlags;
    }

    private final class ServiceIntentResolver
            extends IntentResolver<PackageParser.ServiceIntentInfo, ResolveInfo> {
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType,
                boolean defaultOnly, int userId) {
            mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags,
                int userId) {
            mFlags = flags;
            return super.queryIntent(intent, resolvedType,
                    (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0, userId);
        }

        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType,
                int flags, ArrayList<PackageParser.Service> packageServices, int userId) {
            if (packageServices == null) {
                return null;
            }
            mFlags = flags;
            final boolean defaultOnly = (flags&PackageManager.MATCH_DEFAULT_ONLY) != 0;
            final int N = packageServices.size();
            ArrayList<PackageParser.ServiceIntentInfo[]> listCut =
                new ArrayList<PackageParser.ServiceIntentInfo[]>(N);

            ArrayList<PackageParser.ServiceIntentInfo> intentFilters;
            for (int i = 0; i < N; ++i) {
                intentFilters = packageServices.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    PackageParser.ServiceIntentInfo[] array =
                            new PackageParser.ServiceIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        public final void addService(PackageParser.Service s) {
            mServices.put(s.getComponentName(), s);
            for (PackageParser.ServiceIntentInfo intent : s.intents)
            {
                addFilter(intent);
            }
        }

        public final void removeService(PackageParser.Service s) {
            mServices.remove(s.getComponentName());
            for (PackageParser.ServiceIntentInfo intent : s.intents)
            {
                removeFilter(intent);
            }
        }

        @Override
        protected boolean allowFilterResult(
                PackageParser.ServiceIntentInfo filter, List<ResolveInfo> dest) {
            ServiceInfo filterSi = filter.service.info;
            for (int i=dest.size()-1; i>=0; i--) {
                ServiceInfo destAi = dest.get(i).serviceInfo;
                if (destAi.name == filterSi.name
                        && destAi.packageName == filterSi.packageName) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected PackageParser.ServiceIntentInfo[] newArray(int size) {
            return new PackageParser.ServiceIntentInfo[size];
        }

        @Override
        protected boolean isFilterStopped(PackageParser.ServiceIntentInfo filter, int userId) {
            return false;
        }

        @Override
        protected String packageForFilter(PackageParser.ServiceIntentInfo info) {
            return info.service.owner.packageName;
        }
        
        @Override
        protected ResolveInfo newResult(PackageParser.ServiceIntentInfo filter,
                int match, int userId) {
            final PackageParser.ServiceIntentInfo info = (PackageParser.ServiceIntentInfo)filter;
            final PackageParser.Service service = info.service;
            ServiceInfo si = PackageParser.generateServiceInfo(service, mFlags,
                    new PackageUserState(), userId);
            if (si == null) {
                return null;
            }
            final ResolveInfo res = new ResolveInfo();
            res.serviceInfo = si;
            if ((mFlags&PackageManager.GET_RESOLVED_FILTER) != 0) {
                res.filter = filter;
            }
            res.priority = info.getPriority();
            res.preferredOrder = service.owner.mPreferredOrder;
            //System.out.println("Result: " + res.activityInfo.className +
            //                   " = " + res.priority);
            res.match = match;
            res.isDefault = info.hasDefault;
            res.labelRes = info.labelRes;
            res.nonLocalizedLabel = info.nonLocalizedLabel;
            res.icon = info.icon;
            return res;
        }

        @Override
        protected void sortResults(List<ResolveInfo> results) {
            Collections.sort(results, mResolvePrioritySorter);
        }

        @Override
        protected void dumpFilter(PrintWriter out, String prefix,
                PackageParser.ServiceIntentInfo filter) {
            out.print(prefix); out.print(
                    Integer.toHexString(System.identityHashCode(filter.service)));
                    out.print(' ');
                    out.print(filter.service.getComponentShortName());
                    out.print(" filter ");
                    out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

//        List<ResolveInfo> filterEnabled(List<ResolveInfo> resolveInfoList) {
//            final Iterator<ResolveInfo> i = resolveInfoList.iterator();
//            final List<ResolveInfo> retList = Lists.newArrayList();
//            while (i.hasNext()) {
//                final ResolveInfo resolveInfo = (ResolveInfo) i;
//                if (isEnabledLP(resolveInfo.serviceInfo)) {
//                    retList.add(resolveInfo);
//                }
//            }
//            return retList;
//        }

        // Keys are String (activity class name), values are Activity.
        private final HashMap<ComponentName, PackageParser.Service> mServices
                = new HashMap<ComponentName, PackageParser.Service>();
        private int mFlags;
    }

    private static final Comparator<ResolveInfo> mResolvePrioritySorter =
            new Comparator<ResolveInfo>() {
        public int compare(ResolveInfo r1, ResolveInfo r2) {
            int v1 = r1.priority;
            int v2 = r2.priority;
            //System.out.println("Comparing: q1=" + q1 + " q2=" + q2);
            if (v1 != v2) {
                return (v1 > v2) ? -1 : 1;
            }
            v1 = r1.preferredOrder;
            v2 = r2.preferredOrder;
            if (v1 != v2) {
                return (v1 > v2) ? -1 : 1;
            }
            if (r1.isDefault != r2.isDefault) {
                return r1.isDefault ? -1 : 1;
            }
            v1 = r1.match;
            v2 = r2.match;
            //System.out.println("Comparing: m1=" + m1 + " m2=" + m2);
            if (v1 != v2) {
                return (v1 > v2) ? -1 : 1;
            }
            if (r1.system != r2.system) {
                return r1.system ? -1 : 1;
            }
            return 0;
        }
    };

    public static final Comparator<ProviderInfo> mProviderInitOrderSorter =
            new Comparator<ProviderInfo>() {
        public int compare(ProviderInfo p1, ProviderInfo p2) {
            final int v1 = p1.initOrder;
            final int v2 = p2.initOrder;
            return (v1 > v2) ? -1 : ((v1 < v2) ? 1 : 0);
        }
    };
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String name = method.getName();
        PluginLog.debug("PackageManagerService." + name, Arrays.toString(args));
        if ("getPackageInfo".equals(name))
        {
            String packageName = (String) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            PackageInfo pi = getPackageInfo(packageName, flags, userId);
            if (pi != null)
            {
                return pi;
            }
        }
        else if ("getPackageUid".equals(name))
        {
            String packageName = (String) args[0];
            int userId = (Integer) args[1];
            
            int uid = getPackageUid(packageName, userId);
            if (uid >= 0)
            {
                return uid;
            }
        }
        else if ("getPermissionInfo".equals(name))
        {
            name = (String) args[0];
            int flags = (Integer) args[1];
            
            PermissionInfo pi = getPermissionInfo(name, flags);
            if (pi != null)
            {
                return pi;
            }
        }
        else if ("queryPermissionsByGroup".equals(name))
        {
            String group = (String) args[0];
            int flags = (Integer) args[1];
            
            List<PermissionInfo> pi = queryPermissionsByGroup(group, flags);
            if (pi != null)
            {
                return pi;
            }
        }
        else if ("getPermissionGroupInfo".equals(name))
        {
            name = (String) args[0];
            int flags = (Integer) args[1];

            PermissionGroupInfo pgi = getPermissionGroupInfo(name, flags);
            if (pgi != null)
            {
                return pgi;
            }
        }
        else if ("getAllPermissionGroups".equals(name))
        {
            int flags = (Integer) args[0];
            List<PermissionGroupInfo> list = getAllPermissionGroups(flags);
            
            try {
                list.addAll(thisObject.getAllPermissionGroups(flags));
            } catch (RemoteException e) {
                throw new RuntimeException("Package manager has died", e);
            }
        
            return list;
        }
        else if ("getApplicationInfo".equals(name))
        {
            String packageName = (String) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            ApplicationInfo ai = getApplicationInfo(packageName, flags, userId);
            if (ai != null)
            {
                return ai;
            }
        }
        else if ("getActivityInfo".equals(name))
        {
            ComponentName component = (ComponentName) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            ActivityInfo ai = getActivityInfo(component, flags, userId);
            if (ai != null)
            {
                return ai;
            }
        }
        else if ("getReceiverInfo".equals(name))
        {
            ComponentName component = (ComponentName) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            ActivityInfo ai = getReceiverInfo(component, flags, userId);
            if (ai != null)
            {
                return ai;
            }
        }
        else if ("getServiceInfo".equals(name))
        {
            ComponentName component = (ComponentName) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            ServiceInfo si = getServiceInfo(component, flags, userId);
            if (si != null)
            {
                return si;
            }
        }
        else if ("getProviderInfo".equals(name))
        {
            ComponentName component = (ComponentName) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            ProviderInfo pi = getProviderInfo(component, flags, userId);
            if (pi != null)
            {
                return pi;
            }
        }
        else if ("resolveIntent".equals(name))
        {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (Integer) args[2];
            int userId = (Integer) args[3];
            
            ResolveInfo ri = resolveIntent(intent, resolvedType, flags, userId);
            if (ri != null)
            {
                return ri;
            }
        }
        else if ("queryIntentActivities".equals(name))
        {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (Integer) args[2];
            int userId = (Integer) args[3];
            
            List<ResolveInfo> ris = queryIntentActivities(intent, resolvedType, flags, userId);
            if (ris != null && !ris.isEmpty())
            {
                return ris;
            }
        }
        else if ("queryIntentActivityOptions".equals(name))
        {
            ComponentName caller = (ComponentName) args[0];
            Intent[] specifics = (Intent[]) args[1];
            String[] specificTypes = (String[]) args[2];
            Intent intent = (Intent) args[3];
            String resolvedType = (String) args[4];
            int flags = (Integer) args[5];
            int userId = (Integer) args[6];
            
            List<ResolveInfo> list = queryIntentActivityOptions(caller, specifics, specificTypes, 
                    intent, resolvedType, flags, userId);
            if (list != null && !list.isEmpty())
            {
                return list;
            }
        }
        else if ("queryIntentReceivers".equals(name))
        {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (Integer) args[2];
            int userId = (Integer) args[3];
            
            List<ResolveInfo> ris = queryIntentReceivers(intent, resolvedType, flags, userId);
            if (ris != null && !ris.isEmpty())
            {
                return ris;
            }
        }
        else if ("resolveService".equals(name))
        {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (Integer) args[2];
            int userId = (Integer) args[3];
            
            ResolveInfo ri = resolveService(intent, resolvedType, flags, userId);
            if (ri != null)
            {
                return ri;
            }
        }
        else if ("queryIntentServices".equals(name))
        {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (Integer) args[2];
            int userId = (Integer) args[3];
            
            List<ResolveInfo> ris = queryIntentServices(intent, resolvedType, flags, userId);
            if (ris != null && !ris.isEmpty())
            {
                return ris;
            }
        }
        else if ("resolveContentProvider".equals(name))
        {
            name = (String) args[0];
            int flags = (Integer) args[1];
            int userId = (Integer) args[2];
            
            ProviderInfo pi = resolveContentProvider(name, flags, userId);
            if (pi != null)
            {
                return pi;
            }
        }
        else if ("getInstrumentationInfo".equals(name))
        {
            ComponentName className = (ComponentName) args[0];
            int flags = (Integer) args[1];
            
            InstrumentationInfo ii = getInstrumentationInfo(className, flags);
            if (ii != null)
            {
                return ii;
            }
        }
        else if ("queryInstrumentation".equals(name))
        {
            String targetPackage = (String) args[0];
            int flags = (Integer) args[1];
            
            List<InstrumentationInfo> list = queryInstrumentation(targetPackage, flags);
            if (list != null && !list.isEmpty())
            {
                return list;
            }
        }
        
        return super.invoke(proxy, method, args);
    }

    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        // reader
        synchronized (mPackages) {
            PackageParser.Package p = mPackages.get(packageName);
            if (p != null)
            {
                return generatePackageInfo(p, flags, userId);
            }
        }
        
        return null;
    }

    private PackageInfo generatePackageInfo(PackageParser.Package p, int flags, int userId) {
        return PackageParser.generatePackageInfo(p, null, flags, 0, 0, null, 
                Plugin.getUserState(), userId);
    }

    public int getPackageUid(String packageName, int userId) {
        // reader
        synchronized (mPackages) {
            PackageParser.Package p = mPackages.get(packageName);
            if (p != null)
            {
                return UserHandle.getUid(userId, p.applicationInfo.uid);
            }
        }
        
        return -1;
    }
    
    public PermissionInfo getPermissionInfo(String name, int flags) {
        // reader
        synchronized (mPackages) {
            PackageParser.Permission p = mPermissions.get(name);
            if (p != null)
            {
                return PackageParser.generatePermissionInfo(p, flags);
            }
        }
        
        return null;
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
        // reader
        synchronized (mPackages) {
            ArrayList<PermissionInfo> out = new ArrayList<PermissionInfo>(10);
            for (PackageParser.Permission p : mPermissions.values())
            {
                if (group == null)
                {
                    if (p.info.group == null)
                    {
                        out.add(PackageParser.generatePermissionInfo(p, flags));
                    }
                }
                else
                {
                    if (group.equals(p.info.group))
                    {
                        out.add(PackageParser.generatePermissionInfo(p, flags));
                    }
                }
            }

            if (out.size() > 0)
            {
                return out;
            }
            
            return mPermissionGroups.containsKey(group) ? out : null;
        }
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {
        // reader
        synchronized (mPackages) {
            return PackageParser.generatePermissionGroupInfo(
                    mPermissionGroups.get(name), flags);
        }
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        // reader
        synchronized (mPackages) {
            final int N = mPermissionGroups.size();
            ArrayList<PermissionGroupInfo> out = new ArrayList<PermissionGroupInfo>(N);
            for (PackageParser.PermissionGroup pg : mPermissionGroups.values())
                out.add(PackageParser.generatePermissionGroupInfo(pg, flags));
            return out;
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, 
            int flags, int userId) {
        // writer
        synchronized (mPackages) {
            PackageParser.Package p = mPackages.get(packageName);
            if (p != null)
            {
                return PackageParser.generateApplicationInfo(p, flags, 
                        Plugin.getUserState(), userId);
            }
            
            if ("android".equals(packageName) || "system".equals(packageName))
            {
                return mAndroidApplication;
            }
        }
        
        return null;
    }

    public ActivityInfo getActivityInfo(ComponentName component, 
            int flags, int userId) {
        synchronized (mPackages) {
            PackageParser.Activity a = mActivities.mActivities.get(component);
            
            if (a != null)
            {
                return PackageParser.generateActivityInfo(a, flags, 
                        Plugin.getUserState(), userId);
            }
    
            if (mResolveComponentName.equals(component))
            {
                return mResolveActivity;
            }
        }
        
        return null;
    }

    public ActivityInfo getReceiverInfo(ComponentName component, 
            int flags, int userId) {
        synchronized (mPackages) {
            PackageParser.Activity a = mReceivers.mActivities.get(component);
            
            if (a != null)
            {
                return PackageParser.generateActivityInfo(a, flags, 
                        Plugin.getUserState(), userId);
            }
        }
        
        return null;
    }

    public ServiceInfo getServiceInfo(ComponentName component, 
            int flags, int userId) {
        synchronized (mPackages) {
            PackageParser.Service s = mServices.mServices.get(component);
            
            if (s != null)
            {
                return PackageParser.generateServiceInfo(s, flags, 
                        Plugin.getUserState(), userId);
            }
        }
        
        return null;
    }

    public ProviderInfo getProviderInfo(ComponentName component, 
            int flags, int userId) {
        synchronized (mPackages) {
            PackageParser.Provider p = mProvidersByComponent.get(component);
            
            if (p != null)
            {
                return PackageParser.generateProviderInfo(p, flags, 
                        Plugin.getUserState(), userId);
            }
        }
        
        return null;
    }

    public ResolveInfo resolveIntent(Intent intent, String resolvedType,
            int flags, int userId) {
        List<ResolveInfo> query = queryIntentActivities(intent, resolvedType, flags, userId);
        return chooseBestActivity(intent, resolvedType, flags, query, userId);
    }

    private ResolveInfo chooseBestActivity(Intent intent, String resolvedType,
            int flags, List<ResolveInfo> query, int userId) {
        if (query != null)
        {
            final int N = query.size();
            if (N == 1)
            {
                return query.get(0);
            }
            else if (N > 1)
            {
                // If there is more than one activity with the same priority,
                // then let the user decide between them.
                ResolveInfo r0 = query.get(0);
                ResolveInfo r1 = query.get(1);
                // If the first activity has a higher priority, or a different
                // default, then it is always desireable to pick it.
                if (r0.priority != r1.priority
                ||  r0.preferredOrder != r1.preferredOrder
                ||  r0.isDefault != r1.isDefault)
                {
                    return r0;
                }
                
                if (userId != 0)
                {
                    ResolveInfo ri = new ResolveInfo(mResolveInfo);
                    ri.activityInfo = new ActivityInfo(ri.activityInfo);
                    ri.activityInfo.applicationInfo = new ApplicationInfo(
                            ri.activityInfo.applicationInfo);
                    ri.activityInfo.applicationInfo.uid = UserHandle.getUid(userId,
                            UserHandle.getAppId(ri.activityInfo.applicationInfo.uid));
                    return ri;
                }
                
                return mResolveInfo;
            }
        }
        
        return null;
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, 
            int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp == null)
        {
            if (intent.getSelector() != null)
            {
                intent = intent.getSelector(); 
                comp = intent.getComponent();
            }
        }

        if (comp != null)
        {
            final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
            final ActivityInfo ai = getActivityInfo(comp, flags, userId);
            if (ai != null)
            {
                final ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            
            return list;
        }

        // reader
        synchronized (mPackages) {
            final String pkgName = intent.getPackage();
            if (pkgName == null)
            {
                return mActivities.queryIntent(intent, resolvedType, flags, userId);
            }
            
            final PackageParser.Package pkg = mPackages.get(pkgName);
            if (pkg != null)
            {
                return mActivities.queryIntentForPackage(intent, resolvedType, flags,
                        pkg.activities, userId);
            }
            
            return new ArrayList<ResolveInfo>();
        }
    }

    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller,
            Intent[] specifics, String[] specificTypes, Intent intent,
            String resolvedType, int flags, int userId) {
        final String resultsAction = intent.getAction();

        List<ResolveInfo> results = queryIntentActivities(intent, resolvedType, flags
                | PackageManager.GET_RESOLVED_FILTER, userId);

        int specificsPos = 0;
        int N;

        // todo: note that the algorithm used here is O(N^2).  This
        // isn't a problem in our current environment, but if we start running
        // into situations where we have more than 5 or 10 matches then this
        // should probably be changed to something smarter...

        // First we go through and resolve each of the specific items
        // that were supplied, taking care of removing any corresponding
        // duplicate items in the generic resolve list.
        if (specifics != null)
        {
            for (int i = 0; i < specifics.length; i++)
            {
                final Intent sintent = specifics[i];
                if (sintent == null)
                {
                    continue;
                }
                
                String action = sintent.getAction();
                if (resultsAction != null && resultsAction.equals(action))
                {
                    // If this action was explicitly requested, then don't
                    // remove things that have it.
                    action = null;
                }
                
                ResolveInfo ri = null;
                ActivityInfo ai = null;
                ComponentName comp = sintent.getComponent();
                if (comp == null)
                {
                    ri = resolveIntent(sintent, 
                            specificTypes != null ? specificTypes[i] : null, flags, userId);
                    if (ri == null)
                    {
                        continue;
                    }
                    
                    if (ri == mResolveInfo)
                    {
                        // ACK!  Must do something better with this.
                    }
                    
                    ai = ri.activityInfo;
                    comp = new ComponentName(ai.applicationInfo.packageName, ai.name);
                }
                else
                {
                    ai = getActivityInfo(comp, flags, userId);
                    if (ai == null)
                    {
                        continue;
                    }
                }
                
                // Look for any generic query activities that are duplicates
                // of this specific one, and remove them from the results.
                N = results.size();
                int j;
                for (j = specificsPos; j < N; j++)
                {
                    ResolveInfo sri = results.get(j);
                    if (sri.activityInfo.name.equals(comp.getClassName())
                    &&  sri.activityInfo.applicationInfo.packageName.equals(comp.getPackageName())
                    || (action != null && sri.filter.matchAction(action)))
                    {
                        results.remove(j);
                        if (ri == null)
                        {
                            ri = sri;
                        }
                        
                        j--;
                        N--;
                    }
                }

                // Add this specific item to its proper place.
                if (ri == null)
                {
                    ri = new ResolveInfo();
                    ri.activityInfo = ai;
                }
                
                results.add(specificsPos, ri);
                ri.specificIndex = i;
                specificsPos++;
            }
        }

        // Now we go through the remaining generic results and remove any
        // duplicate actions that are found here.
        N = results.size();
        for (int i = specificsPos; i < N-1; i++)
        {
            final ResolveInfo rii = results.get(i);
            if (rii.filter == null)
            {
                continue;
            }

            // Iterate over all of the actions of this result's intent
            // filter...  typically this should be just one.
            final Iterator<String> it = rii.filter.actionsIterator();
            if (it == null)
            {
                continue;
            }
            
            while (it.hasNext())
            {
                final String action = it.next();
                if (resultsAction != null && resultsAction.equals(action))
                {
                    // If this action was explicitly requested, then don't
                    // remove things that have it.
                    continue;
                }
                
                for (int j = i + 1; j < N; j++)
                {
                    final ResolveInfo rij = results.get(j);
                    if (rij.filter != null && rij.filter.hasAction(action))
                    {
                        results.remove(j);
                        j--;
                        N--;
                    }
                }
            }

            // If the caller didn't request filter information, drop it now
            // so we don't have to marshall/unmarshall it.
            if ((flags & PackageManager.GET_RESOLVED_FILTER) == 0)
            {
                rii.filter = null;
            }
        }

        // Filter out the caller activity if so requested.
        if (caller != null)
        {
            N = results.size();
            for (int i = 0; i < N; i++)
            {
                ActivityInfo ainfo = results.get(i).activityInfo;
                if (caller.getPackageName().equals(ainfo.applicationInfo.packageName)
                &&  caller.getClassName().equals(ainfo.name)) {
                    results.remove(i);
                    break;
                }
            }
        }

        // If the caller didn't request filter information,
        // drop them now so we don't have to
        // marshall/unmarshall it.
        if ((flags & PackageManager.GET_RESOLVED_FILTER) == 0)
        {
            N = results.size();
            for (int i = 0; i < N; i++)
            {
                results.get(i).filter = null;
            }
        }

        return results;
    }

    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, 
            int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp == null)
        {
            if (intent.getSelector() != null)
            {
                intent = intent.getSelector(); 
                comp = intent.getComponent();
            }
        }

        if (comp != null)
        {
            final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
            final ActivityInfo ai = getReceiverInfo(comp, flags, userId);
            if (ai != null)
            {
                final ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            
            return list;
        }

        // reader
        synchronized (mPackages) {
            final String pkgName = intent.getPackage();
            if (pkgName == null)
            {
                return mReceivers.queryIntent(intent, resolvedType, flags, userId);
            }
            
            final PackageParser.Package pkg = mPackages.get(pkgName);
            if (pkg != null)
            {
                return mReceivers.queryIntentForPackage(intent, resolvedType, flags,
                        pkg.receivers, userId);
            }
            
            return null;
        }
    }

    public ResolveInfo resolveService(Intent intent, String resolvedType, 
            int flags, int userId) {
        List<ResolveInfo> query = queryIntentServices(intent, resolvedType, flags, userId);
        if (query != null)
        {
            if (query.size() >= 1)
            {
                // If there is more than one service with the same priority,
                // just arbitrarily pick the first one.
                return query.get(0);
            }
        }
        
        return null;
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, 
            int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp == null)
        {
            if (intent.getSelector() != null)
            {
                intent = intent.getSelector(); 
                comp = intent.getComponent();
            }
        }

        if (comp != null)
        {
            final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
            final ServiceInfo si = getServiceInfo(comp, flags, userId);
            if (si != null)
            {
                final ResolveInfo ri = new ResolveInfo();
                ri.serviceInfo = si;
                list.add(ri);
            }
            
            return list;
        }

        // reader
        synchronized (mPackages) {
            final String pkgName = intent.getPackage();
            if (pkgName == null)
            {
                return mServices.queryIntent(intent, resolvedType, flags, userId);
            }
            
            final PackageParser.Package pkg = mPackages.get(pkgName);
            if (pkg != null)
            {
                return mServices.queryIntentForPackage(intent, resolvedType, flags,
                        pkg.services, userId);
            }
            
            return null;
        }
    }

    public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
        // reader
        synchronized (mPackages) {
            final PackageParser.Provider provider = mProviders.get(name);
            
            if (provider != null)
            {
                return PackageParser.generateProviderInfo(provider, flags, 
                        Plugin.getUserState(), userId);
            }
            
            return null;
        }
    }

    public InstrumentationInfo getInstrumentationInfo(ComponentName name,
            int flags) {
        // reader
        synchronized (mPackages) {
            final PackageParser.Instrumentation i = mInstrumentation.get(name);
            return PackageParser.generateInstrumentationInfo(i, flags);
        }
    }

    public List<InstrumentationInfo> queryInstrumentation(String targetPackage,
            int flags) {
        ArrayList<InstrumentationInfo> finalList = new ArrayList<InstrumentationInfo>();

        // reader
        synchronized (mPackages) {
            final Iterator<PackageParser.Instrumentation> i = mInstrumentation.values().iterator();
            while (i.hasNext())
            {
                final PackageParser.Instrumentation p = i.next();
                if (targetPackage == null || targetPackage.equals(p.info.targetPackage)) {
                    InstrumentationInfo ii = PackageParser.generateInstrumentationInfo(p, flags);
                    if (ii != null)
                    {
                        finalList.add(ii);
                    }
                }
            }
        }

        return finalList;
    }
}