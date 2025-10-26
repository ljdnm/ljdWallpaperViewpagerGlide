package com.android.launcher3.wallpaper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssetsHelper {
    private static final String TAG = "AssetsHelper";
    
    /**
     * 从assets目录加载PAG文件列表
     */
    public static List<ImageItem> loadPagFilesFromAssets(Context context) {
        List<ImageItem> pagList = new ArrayList<>();
        
        try {
            AssetManager assetManager = context.getAssets();
            
            // 直接添加已知的PAG文件
            String[] knownPagFiles = {
                "animations/wgbz_ssbz_1-day_cold_smallwind.pag",
                "animations/wgbz_ssbz_1-night_warm_strongwind.pag"
            };
            
            for (String assetPath : knownPagFiles) {
                // 检查文件是否存在
                try {
                    assetManager.open(assetPath).close(); // 尝试打开并立即关闭
                    ImageItem item = new ImageItem(assetPath, getDisplayName(assetPath), true);
                    pagList.add(item);
                    Log.d(TAG, "✅ 找到assets PAG文件: " + assetPath);
                } catch (IOException e) {
                    Log.w(TAG, "❌ assets文件不存在: " + assetPath);
                }
            }
            
            // 可选：扫描animations目录下的所有PAG文件
            scanAssetsDirectory(assetManager, "animations", pagList);
            
        } catch (Exception e) {
            Log.e(TAG, "加载assets文件失败: " + e.getMessage());
        }
        
        Log.d(TAG, "从assets加载了 " + pagList.size() + " 个PAG文件");
        return pagList;
    }
    
    /**
     * 扫描assets目录
     */
    private static void scanAssetsDirectory(AssetManager assetManager, String directory, List<ImageItem> result) {
        try {
            String[] files = assetManager.list(directory);
            if (files != null) {
                for (String file : files) {
                    String fullPath = directory + "/" + file;
                    if (file.toLowerCase().endsWith(".pag")) {
                        ImageItem item = new ImageItem(fullPath, getDisplayName(file), true);
                        result.add(item);
                        Log.d(TAG, "✅ 扫描到assets PAG文件: " + fullPath);
                    } else {
                        // 如果是目录，递归扫描（注意：assets不支持直接列出子目录文件）
                        try {
                            String[] subFiles = assetManager.list(fullPath);
                            if (subFiles != null && subFiles.length > 0) {
                                // 这可能是目录，但我们不深入扫描以避免复杂逻辑
                                Log.d(TAG, "发现assets子目录: " + fullPath);
                            }
                        } catch (Exception e) {
                            // 忽略错误
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "扫描assets目录失败: " + directory);
        }
    }
    
    /**
     * 从文件路径获取显示名称
     */
    private static String getDisplayName(String assetPath) {
        // 从路径中提取文件名
        String[] parts = assetPath.split("/");
        String fileName = parts[parts.length - 1];
        
        // 移除扩展名并美化显示
        String nameWithoutExt = fileName.replace(".pag", "");
        
        // 替换下划线和横杠为空格
        String displayName = nameWithoutExt
            .replace("_", " ")
            .replace("-", " ")
            .trim();
        
        return displayName.isEmpty() ? fileName : displayName;
    }
    
    /**
     * 测试assets文件访问
     */
    public static void testAssetsAccess(Context context) {
        Log.d(TAG, "=== Assets访问测试 ===");
        
        try {
            AssetManager assetManager = context.getAssets();
            
            // 测试已知文件
            String[] testFiles = {
                "animations/wgbz_ssbz_1-day_cold_smallwind.pag",
                "animations/wgbz_ssbz_1-night_warm_strongwind.pag"
            };
            
            for (String filePath : testFiles) {
                try {
                    assetManager.open(filePath).close();
                    Log.d(TAG, "✅ assets文件可访问: " + filePath);
                } catch (IOException e) {
                    Log.e(TAG, "❌ assets文件不可访问: " + filePath);
                }
            }
            
            // 列出animations目录内容
            try {
                String[] animationsFiles = assetManager.list("animations");
                Log.d(TAG, "animations目录文件数: " + (animationsFiles != null ? animationsFiles.length : "null"));
                if (animationsFiles != null) {
                    for (String file : animationsFiles) {
                        Log.d(TAG, "animations文件: " + file);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "无法列出animations目录");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "assets测试失败: " + e.getMessage());
        }
    }
}