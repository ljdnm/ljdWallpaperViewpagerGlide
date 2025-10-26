package com.android.launcher3.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

public class AllFilesAccessHelper {
    private static final String TAG = "AllFilesAccessHelper";
    
    /**
     * 检查是否有所有文件访问权限
     */
    public static boolean hasAllFilesAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true; // Android 10及以下默认有权限
    }
    
    /**
     * 请求所有文件访问权限
     */
    public static void requestAllFilesAccessPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            } catch (Exception e) {
                // 备用方案
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivity(intent);
            }
        }
    }
    
    /**
     * 检查文件是否可读（使用所有文件权限）
     */
    public static boolean canReadFileWithAllAccess(String filePath) {
        if (!hasAllFilesAccessPermission()) {
            Log.d(TAG, "没有所有文件访问权限");
            return false;
        }
        
        try {
            File file = new File(filePath);
            boolean canRead = file.canRead();
            Log.d(TAG, "所有文件权限检查: " + filePath + " -> 可读: " + canRead);
            return canRead;
        } catch (Exception e) {
            Log.e(TAG, "文件权限检查失败: " + e.getMessage());
            return false;
        }
    }
}