package com.android.launcher3.wallpaper;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSearchHelper {
    private static final String TAG = "ljd FileSearchHelper";




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

    /**
     * 只加载指定的PAG文件
     */
    public static List<ImageItem> loadKnownPagFiles() {
        List<ImageItem> pagList = new ArrayList<>();

        // 只加载指定的PAG文件列表
        String[] specifiedPagFiles = {
                "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/test.pag",
                "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };

        Log.d(TAG, "开始加载指定PAG文件...");

        for (String filePath : specifiedPagFiles) {
            File file = new File(filePath);
            Log.d(TAG, "检查指定文件: " + filePath +
                    ", 存在: " + file.exists() +
                    ", 可读: " + file.canRead() +
                    ", 大小: " + file.length());

            if (file.exists() && file.canRead()) {
                ImageItem item = createImageItem(file);
                pagList.add(item);
                Log.d(TAG, "✅ 成功加载指定PAG文件: " + file.getName());
            } else {
                Log.w(TAG, "❌ 指定文件不可访问: " + filePath);
            }
        }

        Log.d(TAG, "指定PAG文件加载完成，找到 " + pagList.size() + " 个文件");
        return pagList;
    }

    /**
     * 修改扫描方法，只检查指定文件
     */
    public static List<ImageItem> scanPagFiles() {
        // 不再扫描整个目录，直接返回指定文件
        return loadKnownPagFiles();
    }


    /**
     * 只加载指定的PAG文件
     */
    public static List<ImageItem> loadSpecifiedPagFiles() {
        List<ImageItem> pagList = new ArrayList<>();

        // 只加载指定的PAG文件列表
        String[] specifiedPagFiles = {
                "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/test.pag",
                "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };

        Log.d(TAG, "开始加载指定PAG文件...");

        for (String filePath : specifiedPagFiles) {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                ImageItem item = createImageItem(file);
                pagList.add(item);
                Log.d(TAG, "✅ 成功加载指定PAG文件: " + file.getName());
            } else {
                Log.w(TAG, "❌ 指定文件不可访问: " + filePath);
            }
        }

        return pagList;
    }

    private static ImageItem createImageItem(File file) {
        return new ImageItem(
                file.getAbsolutePath(),
                android.net.Uri.fromFile(file),
                file.getName(),
                ImageItem.SOURCE_SDCARD
        );
    }

}