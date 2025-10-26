package com.android.launcher3.wallpaper;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileSearchHelper {
    private static final String TAG = "FileSearchHelper";
    
    /**
     * 直接读取已知的PAG文件
     */
    public static List<ImageItem> loadKnownPagFiles() {
        List<ImageItem> pagList = new ArrayList<>();
        
        // 已知的PAG文件列表
        String[] knownPagFiles = {
            "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
            "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
            "/storage/emulated/0/Download/LionWallpaper/test.pag",
            "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };
        
        Log.d(TAG, "开始加载已知PAG文件...");
        
        for (String filePath : knownPagFiles) {
            File file = new File(filePath);
            Log.d(TAG, "检查文件: " + filePath + 
                      ", 存在: " + file.exists() + 
                      ", 可读: " + file.canRead() +
                      ", 大小: " + file.length());
            
            if (file.exists() && file.canRead()) {
                ImageItem item = createImageItem(file);
                pagList.add(item);
                Log.d(TAG, "✅ 成功加载PAG文件: " + file.getName());
            } else {
                Log.w(TAG, "❌ 文件不可访问: " + filePath);
            }
        }
        
        Log.d(TAG, "已知PAG文件加载完成，找到 " + pagList.size() + " 个文件");
        return pagList;
    }
    
    /**
     * 扫描目录中的所有PAG文件
     */
    public static List<ImageItem> scanPagFiles() {
        List<ImageItem> pagList = new ArrayList<>();
        
        String[] directories = {
            "/storage/emulated/0/Download/LionWallpaper",
            "/sdcard/Download/LionWallpaper"
        };
        
        for (String dirPath : directories) {
            File directory = new File(dirPath);
            Log.d(TAG, "扫描目录: " + dirPath + 
                      ", 存在: " + directory.exists() + 
                      ", 是目录: " + directory.isDirectory());
            
            if (directory.exists() && directory.isDirectory()) {
                // 使用正确的文件过滤器
                File[] files = directory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        boolean isPag = file.isFile() && file.getName().toLowerCase().endsWith(".pag");
                        if (isPag) {
                            Log.d(TAG, "找到PAG文件: " + file.getName() + 
                                      ", 可读: " + file.canRead() +
                                      ", 大小: " + file.length());
                        }
                        return isPag;
                    }
                });
                
                if (files != null) {
                    Log.d(TAG, "目录中找到PAG文件数: " + files.length);
                    for (File file : files) {
                        ImageItem item = createImageItem(file);
                        pagList.add(item);
                        Log.d(TAG, "✅ 添加PAG文件: " + file.getName());
                    }
                } else {
                    Log.w(TAG, "目录列表返回null: " + dirPath);
                }
            }
        }
        
        return pagList;
    }
    
    private static ImageItem createImageItem(File file) {
        // 根据您实际的ImageItem构造方法调整
        return new ImageItem(
            file.getAbsolutePath(),
            android.net.Uri.fromFile(file),
            file.getName(),
            ImageItem.SOURCE_SDCARD
        );
    }

    public static List<ImageItem> loadPagFilesWithAllAccess() {
        List<ImageItem> pagList = new ArrayList<>();

        if (!AllFilesAccessHelper.hasAllFilesAccessPermission()) {
            Log.w(TAG, "没有所有文件访问权限，无法加载PAG文件");
            return pagList;
        }

        String[] knownPagFiles = {
                "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/test.pag",
                "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };

        for (String filePath : knownPagFiles) {
            if (AllFilesAccessHelper.canReadFileWithAllAccess(filePath)) {
                File file = new File(filePath);
                ImageItem item = createImageItem(file);
                pagList.add(item);
                Log.d(TAG, "✅ 使用所有文件权限加载PAG: " + file.getName());
            } else {
                Log.w(TAG, "❌ 即使有所有文件权限也无法读取: " + filePath);
            }
        }

        return pagList;
    }
}