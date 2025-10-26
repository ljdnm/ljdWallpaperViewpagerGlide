// WallpaperBroadcastReceiver.java
package com.android.launcher3.wallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WallpaperBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ljd WallpaperReceiver";
    
    // 广播Action定义
    public static final String ACTION_SWITCH_WALLPAPER = "com.android.launcher3.ACTION_SWITCH_WALLPAPER";
    public static final String ACTION_UPDATE_CONDITION = "com.android.launcher3.ACTION_UPDATE_CONDITION";
    public static final String ACTION_REFRESH_WALLPAPER = "com.android.launcher3.ACTION_REFRESH_WALLPAPER";
    
    // 额外参数
    public static final String EXTRA_WALLPAPER_TYPE = "wallpaper_type";
    public static final String EXTRA_IS_DAY = "is_day";
    public static final String EXTRA_TEMPERATURE = "temperature"; 
    public static final String EXTRA_WIND_LEVEL = "wind_level";
    
    private OnWallpaperChangeListener listener;
    
    public interface OnWallpaperChangeListener {
        void onWallpaperTypeChanged(int newType);
        void onConditionChanged(boolean isDay, int temperature, int windLevel);
        void onWallpaperListRefreshed();
    }
    
    public void setOnWallpaperChangeListener(OnWallpaperChangeListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);
        
        if (ACTION_SWITCH_WALLPAPER.equals(action)) {
            int wallpaperType = intent.getIntExtra(EXTRA_WALLPAPER_TYPE, WallpaperType.DEFAULT);
            Log.d(TAG, "切换壁纸类型: " + WallpaperType.getName(wallpaperType));
            
            if (listener != null) {
                listener.onWallpaperTypeChanged(wallpaperType);
            }
            
        } else if (ACTION_UPDATE_CONDITION.equals(action)) {
            boolean isDay = intent.getBooleanExtra(EXTRA_IS_DAY, true);
            int temperature = intent.getIntExtra(EXTRA_TEMPERATURE, 25);
            int windLevel = intent.getIntExtra(EXTRA_WIND_LEVEL, 2);
            
            Log.d(TAG, "环境条件更新: " + 
                      (isDay ? "白天" : "夜晚") + ", " +
                      temperature + "°C, 风量" + windLevel);
            
            if (listener != null) {
                listener.onConditionChanged(isDay, temperature, windLevel);
            }
            
        } else if (ACTION_REFRESH_WALLPAPER.equals(action)) {
            Log.d(TAG, "刷新壁纸列表");
            
            WallpaperManager.getInstance(context).refreshDefaultWallpapers();
            
            if (listener != null) {
                listener.onWallpaperListRefreshed();
            }
        }
    }
}