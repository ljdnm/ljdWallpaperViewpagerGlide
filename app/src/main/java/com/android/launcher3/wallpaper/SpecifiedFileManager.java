// SpecifiedFileManager.java
package com.android.launcher3.wallpaper;

import android.content.Context;
import android.util.Log;
import com.android.launcher3.R;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpecifiedFileManager {
    private static final String TAG = "ljd SpecifiedFileManager";
    

    
    /**
     * 添加指定的本地PNG资源
     */
    private static void addLocalPngResources(List<ImageItem> list) {
        int[] localPngResources = {
            R.drawable.jtbz_zrsg_alphelia,
            R.drawable.jtbz_zrsg_crimsono,
            R.drawable.jtbz_zrsg_equinoxis,
            R.drawable.jtbz_zrsg_harmonia,
            R.drawable.jtbz_zrsg_thalassa,
            R.drawable.jtbz_zrsg_zephyria
        };
        
        String[] localPngNames = {
            "阿尔菲莉亚", "克里姆森", "伊奎诺克西斯", 
            "哈莫尼亚", "塔拉萨", "泽菲莉亚"
        };
        
        for (int i = 0; i < localPngResources.length; i++) {
            ImageItem item = new ImageItem(localPngResources[i], localPngNames[i], false);
            list.add(item);
            Log.d(TAG, "✅ 添加本地PNG资源: " + localPngNames[i]);
        }
    }
    

    /**
     * 添加指定的SDCard文件
     */
    private static void addSdcardFiles(List<ImageItem> list) {
        String[] sdcardFiles = {
            "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
            "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
            "/storage/emulated/0/Download/LionWallpaper/test.pag",
            "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };
        
        for (String filePath : sdcardFiles) {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                ImageItem item = new ImageItem(filePath, file.getName());
                list.add(item);
                Log.d(TAG, "✅ 添加SDCard文件: " + file.getName());
            } else {
                Log.w(TAG, "❌ SDCard文件不可访问: " + filePath);
            }
        }
    }
    
    /**
     * 检查是否是允许加载的指定文件
     */
    public static boolean isAllowedFile(String filePath) {
        if (filePath == null) return false;
        
        // 允许的SDCard文件列表
        String[] allowedSdcardFiles = {
            "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
            "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag", 
            "/storage/emulated/0/Download/LionWallpaper/test.pag",
            "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };
        
        for (String allowedFile : allowedSdcardFiles) {
            if (filePath.equals(allowedFile)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取指定文件统计信息
     */
    public static String getFileStatistics(List<ImageItem> fileList) {
        int localCount = 0;
        int assetsCount = 0;
        int sdcardCount = 0;
        
        for (ImageItem item : fileList) {
            if (item.isFromLocal()) {
                localCount++;
            } else if (item.isFromAssets()) {
                assetsCount++;
            } else if (item.isFromSDCard()) {
                sdcardCount++;
            }
        }
        
        return String.format("本地: %d | Assets: %d | SDCard: %d", 
                           localCount, assetsCount, sdcardCount);
    }


    /**
     * 创建指定文件列表
     */
    public static List<ImageItem> createSpecifiedFileList(Context context) {
        List<ImageItem> specifiedList = new ArrayList<>();

        // 添加本地PNG资源
        addLocalPngResources(specifiedList);

        // 添加Assets PAG文件
        addAssetsPagFiles(specifiedList, context);

        // 添加SDCard文件
        addSdcardFiles(specifiedList);

        Log.d(TAG, "指定文件列表创建完成，总共: " + specifiedList.size() + " 个文件");

        // 详细日志输出
        logDetailedStatistics(specifiedList);

        return specifiedList;
    }

    /**
     * 添加指定的Assets PAG文件 - 修复版本
     */
    private static void addAssetsPagFiles(List<ImageItem> list, Context context) {
        String[] assetsPagFiles = {
                "animations/wgbz_ssbz_1-day_cold_smallwind.pag",
                "animations/wgbz_ssbz_1-night_warm_strongwind.pag"
        };

        String[] assetsPagNames = {
                "白天寒冷微风", "夜晚温暖强风"
        };

        Log.d(TAG, "开始检查Assets文件...");

        for (int i = 0; i < assetsPagFiles.length; i++) {
            String assetPath = assetsPagFiles[i];
            String displayName = assetsPagNames[i];

            // 使用改进的检查方法
            if (isAssetFileExists(context, assetPath)) {
                ImageItem item = new ImageItem(assetPath, displayName, true);
                list.add(item);
                Log.d(TAG, "✅ 成功添加Assets PAG文件: " + assetPath);
            } else {
                Log.w(TAG, "❌ Assets文件不存在: " + assetPath);

                // 尝试列出animations目录来调试
                if (i == 0) { // 只在第一次时列出目录
                    listAssetsDirectory(context, "animations");
                }
            }
        }
    }

    /**
     * 改进的Assets文件存在性检查
     */
    private static boolean isAssetFileExists(Context context, String assetPath) {
        try {
            // 方法1: 尝试打开文件
            context.getAssets().open(assetPath).close();
            Log.d(TAG, "Assets文件可打开: " + assetPath);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Assets文件打开失败: " + assetPath + ", 错误: " + e.getMessage());

            // 方法2: 检查文件是否在目录列表中
            try {
                String directory = getDirectoryFromPath(assetPath);
                String fileName = getFileNameFromPath(assetPath);
                String[] files = context.getAssets().list(directory);

                if (files != null) {
                    for (String file : files) {
                        if (file.equals(fileName)) {
                            Log.d(TAG, "通过目录列表找到Assets文件: " + assetPath);
                            return true;
                        }
                    }
                }
                Log.w(TAG, "在目录列表中未找到文件: " + fileName);
            } catch (IOException ex) {
                Log.e(TAG, "列出Assets目录失败: " + ex.getMessage());
            }

            return false;
        }
    }

    /**
     * 列出Assets目录内容用于调试
     */
    private static void listAssetsDirectory(Context context, String directory) {
        try {
            Log.d(TAG, "=== 开始列出Assets目录: " + directory + " ===");
            String[] files = context.getAssets().list(directory);
            if (files != null && files.length > 0) {
                for (String file : files) {
                    Log.d(TAG, "Assets目录文件: " + directory + "/" + file);
                }
                Log.d(TAG, "=== Assets目录列出完成，共 " + files.length + " 个文件 ===");
            } else {
                Log.w(TAG, "Assets目录为空或不存在: " + directory);
            }
        } catch (IOException e) {
            Log.e(TAG, "列出Assets目录异常: " + e.getMessage());
        }
    }

    /**
     * 从路径中提取目录名
     */
    private static String getDirectoryFromPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash > 0 ? path.substring(0, lastSlash) : "";
    }

    /**
     * 从路径中提取文件名
     */
    private static String getFileNameFromPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash > 0 ? path.substring(lastSlash + 1) : path;
    }

    /**
     * 详细统计日志
     */
    private static void logDetailedStatistics(List<ImageItem> fileList) {
        int localCount = 0;
        int assetsCount = 0;
        int sdcardCount = 0;

        for (ImageItem item : fileList) {
            if (item.isFromLocal()) {
                localCount++;
                Log.d(TAG, "📱 本地资源: " + item.getTitle());
            } else if (item.isFromAssets()) {
                assetsCount++;
                Log.d(TAG, "📦 Assets文件: " + item.getTitle() + " (" + item.getAssetsPath() + ")");
            } else if (item.isFromSDCard()) {
                sdcardCount++;
                Log.d(TAG, "💾 SDCard文件: " + item.getTitle() + " (" + item.getFilePath() + ")");
            }
        }

        Log.d(TAG, "📊 详细统计 - 本地: " + localCount + " | Assets: " + assetsCount + " | SDCard: " + sdcardCount);
    }
}