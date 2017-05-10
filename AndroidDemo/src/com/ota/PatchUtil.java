package com.ota;

import android.content.Context;

import java.io.IOException;

public class PatchUtil {
    
    /**
     * 装载动态库"libpatch.so"
     */
    
    static
    {
        System.loadLibrary("patch");
    }
    
    private static native void applyPatchToOldApk(String oldApkPath, String newApkPath, 
            String patchPath);
    
    /**
     * 打补丁合成APP包
     * @param oldApkPath 旧版本Apk包路径
     * @param newApkPath 合成Apk包路径
     * @param patchPath 补丁文件路径
     */
    
    public static void applyPatch(String oldApkPath, String newApkPath, String patchPath)
            throws IOException {
        applyPatchToOldApk(oldApkPath, newApkPath, patchPath);
    }
    
    /**
     * 获取手机安装此应用的旧版apk包，与增量包合并生成新的apk包
     */
    
    public static void applyPatch(Context context, String newApkPath, String patchPath)
            throws IOException {
        String oldApkPath = context.getApplicationInfo().sourceDir;
        applyPatchToOldApk(oldApkPath, newApkPath, patchPath);
    }
}