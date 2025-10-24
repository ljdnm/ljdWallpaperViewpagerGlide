package com.android.launcher3.wallpaper;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileAccessHelper {
    private static final String TAG = "FileAccessHelper";
    
    /**
     * 检查文件是否存在且可读
     */
    public static boolean isFileAccessible(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            File file = new File(filePath);
            boolean exists = file.exists();
            boolean canRead = file.canRead();
            Log.d(TAG, "文件检查: " + filePath + 
                      ", 存在: " + exists + ", 可读: " + canRead);
            return exists && canRead;
        } catch (SecurityException e) {
            Log.e(TAG, "安全异常 - 无法访问文件: " + filePath + ", 错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "检查文件异常: " + filePath + ", 错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取文件信息
     */
    public static String getFileInfo(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return "大小: " + file.length() + " bytes, " +
                       "路径: " + file.getAbsolutePath();
            } else {
                return "文件不存在";
            }
        } catch (Exception e) {
            return "获取文件信息失败: " + e.getMessage();
        }
    }
    
    /**
     * 扫描目录下的图片和PAG文件
     */
    public static List<String> scanDirectory(String directoryPath) {
        List<String> fileList = new ArrayList<>();
        
        try {
            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                Log.e(TAG, "目录不存在或不是目录: " + directoryPath);
                return fileList;
            }
            
            File[] files = directory.listFiles();
            if (files == null) {
                Log.e(TAG, "无法读取目录内容: " + directoryPath);
                return fileList;
            }
            
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".png") || 
                        fileName.endsWith(".jpg") || 
                        fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".pag")) {
                        
                        if (isFileAccessible(file.getAbsolutePath())) {
                            fileList.add(file.getAbsolutePath());
                            Log.d(TAG, "找到文件: " + file.getAbsolutePath());
                        } else {
                            Log.w(TAG, "文件不可访问: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            
            Log.d(TAG, "扫描完成，找到 " + fileList.size() + " 个文件");
        } catch (SecurityException e) {
            Log.e(TAG, "权限不足，无法扫描目录: " + directoryPath + ", 错误: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "扫描目录异常: " + directoryPath + ", 错误: " + e.getMessage());
        }
        
        return fileList;
    }
}