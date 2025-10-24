package com.android.launcher3.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        
        return permissions.toArray(new String[0]);
    }
    
    public static boolean hasMediaReadPermission(Context context) {
        String[] requiredPermissions = getRequiredPermissions();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    public static void requestMediaReadPermission(Activity activity) {
        String[] permissions = getRequiredPermissions();
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
    }
    
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}