// WallpaperManager.java
package com.android.launcher3.wallpaper;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class WallpaperManager {
    private static final String TAG = "ljd WallpaperManager";
    
    private static WallpaperManager instance;
    private List<ImageItem> currentWallpaperList;
    private int currentWallpaperType = WallpaperType.DEFAULT;
    private boolean isInteractiveMode = true; // 是否可滑动切换
    
    // 壁纸列表缓存
    private List<ImageItem> defaultWallpapers;
    private List<ImageItem> wuganWallpapers; 
    private List<ImageItem> fengyunWallpapers;
    
    private Context context;
    
    public static synchronized WallpaperManager getInstance(Context context) {
        if (instance == null) {
            instance = new WallpaperManager(context);
        }
        return instance;
    }
    
    private WallpaperManager(Context context) {
        this.context = context.getApplicationContext();
        this.currentWallpaperList = new ArrayList<>();
        loadAllWallpaperLists();
    }
    
    /**
     * 预加载所有壁纸列表
     */
    private void loadAllWallpaperLists() {
        // 默认壁纸：本地PNG + Assets PAG + SDCard文件
        defaultWallpapers = SpecifiedFileManager.createSpecifiedFileList(context);
        
        // 五感壁纸：固定的Assets PAG文件
        wuganWallpapers = loadWuganWallpapers();
        
        // 风云壁纸：固定的Assets PAG文件  
        fengyunWallpapers = loadFengyunWallpapers();
        
        Log.d(TAG, "壁纸列表加载完成 - " +
                  "默认: " + defaultWallpapers.size() + " | " +
                  "五感: " + wuganWallpapers.size() + " | " +
                  "风云: " + fengyunWallpapers.size());
    }
    
    /**
     * 加载五感壁纸列表
     */
    private List<ImageItem> loadWuganWallpapers() {
        List<ImageItem> list = new ArrayList<>();
        
        // 五感壁纸的固定Assets文件
        String[] wuganAssetsFiles = {
            "animations/wgbz_ssbz_1-day_cold_smallwind.pag",
            "animations/wgbz_ssbz_1-day_cold_mediumwind.pag", 
            "animations/wgbz_ssbz_1-day_cold_strongwind.pag",
            "animations/wgbz_ssbz_1-day_warm_smallwind.pag",
            "animations/wgbz_ssbz_1-day_warm_mediumwind.pag",
            "animations/wgbz_ssbz_1-day_warm_strongwind.pag",
            "animations/wgbz_ssbz_1-night_cold_smallwind.pag",
            "animations/wgbz_ssbz_1-night_cold_mediumwind.pag",
            "animations/wgbz_ssbz_1-night_cold_strongwind.pag",
            "animations/wgbz_ssbz_1-night_warm_smallwind.pag",
            "animations/wgbz_ssbz_1-night_warm_mediumwind.pag",
            "animations/wgbz_ssbz_1-night_warm_strongwind.pag"
        };
        
        String[] wuganDisplayNames = {
            "白天寒冷微风", "白天寒冷中风", "白天寒冷强风",
            "白天温暖微风", "白天温暖中风", "白天温暖强风", 
            "夜晚寒冷微风", "夜晚寒冷中风", "夜晚寒冷强风",
            "夜晚温暖微风", "夜晚温暖中风", "夜晚温暖强风"
        };
        
        for (int i = 0; i < wuganAssetsFiles.length; i++) {
            String assetPath = wuganAssetsFiles[i];
            String displayName = wuganDisplayNames[i];
            
            if (AssetsHelper.isAssetFileExists(context, assetPath)) {
                ImageItem item = new ImageItem(assetPath, displayName, true);
                list.add(item);
                Log.d(TAG, "✅ 添加五感壁纸: " + displayName);
            } else {
                Log.w(TAG, "❌ 五感壁纸文件不存在: " + assetPath);
            }
        }
        
        return list;
    }
    
    /**
     * 加载风云壁纸列表
     */
    private List<ImageItem> loadFengyunWallpapers() {
        List<ImageItem> list = new ArrayList<>();
        
        // 风云壁纸的固定Assets文件
        String[] fengyunAssetsFiles = {
            "animations/fybz_ssbz_1-day_cold_smallwind.pag",
            "animations/fybz_ssbz_1-day_cold_mediumwind.pag",
            "animations/fybz_ssbz_1-day_cold_strongwind.pag",
            "animations/fybz_ssbz_1-day_warm_smallwind.pag", 
            "animations/fybz_ssbz_1-day_warm_mediumwind.pag",
            "animations/fybz_ssbz_1-day_warm_strongwind.pag",
            "animations/fybz_ssbz_1-night_cold_smallwind.pag",
            "animations/fybz_ssbz_1-night_cold_mediumwind.pag",
            "animations/fybz_ssbz_1-night_cold_strongwind.pag",
            "animations/fybz_ssbz_1-night_warm_smallwind.pag",
            "animations/fybz_ssbz_1-night_warm_mediumwind.pag",
            "animations/fybz_ssbz_1-night_warm_strongwind.pag"
        };
        
        String[] fengyunDisplayNames = {
            "风云-白天寒冷微风", "风云-白天寒冷中风", "风云-白天寒冷强风",
            "风云-白天温暖微风", "风云-白天温暖中风", "风云-白天温暖强风",
            "风云-夜晚寒冷微风", "风云-夜晚寒冷中风", "风云-夜晚寒冷强风", 
            "风云-夜晚温暖微风", "风云-夜晚温暖中风", "风云-夜晚温暖强风"
        };
        
        for (int i = 0; i < fengyunAssetsFiles.length; i++) {
            String assetPath = fengyunAssetsFiles[i];
            String displayName = fengyunDisplayNames[i];
            
            if (AssetsHelper.isAssetFileExists(context, assetPath)) {
                ImageItem item = new ImageItem(assetPath, displayName, true);
                list.add(item);
                Log.d(TAG, "✅ 添加风云壁纸: " + displayName);
            } else {
                Log.w(TAG, "❌ 风云壁纸文件不存在: " + assetPath);
            }
        }
        
        return list;
    }
    
    /**
     * 切换壁纸类型
     */
    public void switchWallpaperType(int wallpaperType) {
        Log.d(TAG, "切换壁纸类型: " + WallpaperType.getName(wallpaperType));
        
        this.currentWallpaperType = wallpaperType;
        
        switch (wallpaperType) {
            case WallpaperType.DEFAULT:
                currentWallpaperList = new ArrayList<>(defaultWallpapers);
                isInteractiveMode = true; // 默认壁纸可滑动
                break;
                
            case WallpaperType.WUGAN:
                currentWallpaperList = new ArrayList<>(wuganWallpapers);
                isInteractiveMode = false; // 五感壁纸不可滑动
                break;
                
            case WallpaperType.FENGYUN:
                currentWallpaperList = new ArrayList<>(fengyunWallpapers);
                isInteractiveMode = false; // 风云壁纸不可滑动
                break;
        }
        
        Log.d(TAG, "壁纸列表已切换: " + currentWallpaperList.size() + " 个项目");
    }
    
    /**
     * 根据环境条件获取智能壁纸索引
     */
    public int getSmartWallpaperIndex(boolean isDay, int temperature, int windLevel) {
        if (currentWallpaperType == WallpaperType.DEFAULT) {
            return 0; // 默认壁纸返回第一个
        }
        
        // 五感/风云壁纸的智能选择逻辑
        String timeKey = isDay ? "day" : "night";
        String tempKey = temperature < 25 ? "cold" : "warm"; // 25度为冷暖分界
        String windKey;
        
        if (windLevel <= 2) {
            windKey = "smallwind";
        } else if (windLevel <= 4) {
            windKey = "mediumwind"; 
        } else {
            windKey = "strongwind";
        }
        
        // 构建匹配的文件名模式
        String pattern = timeKey + "_" + tempKey + "_" + windKey;
        
        // 在当前壁纸列表中查找匹配项
        for (int i = 0; i < currentWallpaperList.size(); i++) {
            ImageItem item = currentWallpaperList.get(i);
            if (item.isFromAssets() && item.getAssetsPath().contains(pattern)) {
                Log.d(TAG, "智能选择壁纸: " + item.getTitle() + " | 条件: " + pattern);
                return i;
            }
        }
        
        // 如果没有精确匹配，返回第一个
        Log.w(TAG, "未找到匹配壁纸，使用默认");
        return 0;
    }
    
    // Getter方法
    public List<ImageItem> getCurrentWallpaperList() {
        return currentWallpaperList;
    }
    
    public int getCurrentWallpaperType() {
        return currentWallpaperType;
    }
    
    public boolean isInteractiveMode() {
        return isInteractiveMode;
    }
    
    public String getCurrentWallpaperTypeName() {
        return WallpaperType.getName(currentWallpaperType);
    }
    
    /**
     * 刷新默认壁纸列表（当商城有新壁纸时调用）
     */
    public void refreshDefaultWallpapers() {
        defaultWallpapers = SpecifiedFileManager.createSpecifiedFileList(context);
        if (currentWallpaperType == WallpaperType.DEFAULT) {
            currentWallpaperList = new ArrayList<>(defaultWallpapers);
        }
        Log.d(TAG, "默认壁纸列表已刷新: " + defaultWallpapers.size() + " 个项目");
    }
}