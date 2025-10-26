package com.android.launcher3.wallpaper;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreHelper {
    private static final String TAG = "MediaStoreHelper";
    
    /**
     * 查询所有文件（包括PAG）
     */
    public static List<ImageItem> loadAllFiles(Context context) {
        List<ImageItem> imageList = new ArrayList<>();
        
        if (!PermissionHelper.hasStoragePermission((Activity) context)) {
            return imageList;
        }
        
        // 首先查询图片
        imageList.addAll(loadImagesFromSpecificDirectory(context, "LionWallpaper"));
        
        // 然后查询PAG文件（使用不同的查询方法）
        imageList.addAll(loadPagFiles(context));
        
        return imageList;
    }
    
    /**
     * 专门查询PAG文件
     */
    public static List<ImageItem> loadPagFiles(Context context) {
        List<ImageItem> pagList = new ArrayList<>();
        
        String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        };
        
        // 查询条件：查找PAG文件
        String selection = MediaStore.Files.FileColumns.DATA + " LIKE ? AND " +
                          MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%LionWallpaper%", "%.pag"};
        
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder)) {
            
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                
                Log.d(TAG, "开始查询PAG文件...");
                
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String filePath = cursor.getString(dataColumn);
                    
                    Log.d(TAG, "找到PAG文件: " + name + ", 路径: " + filePath);
                    
                    // 创建URI（使用Files表）
                    Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"), id);
                    
                    ImageItem item = new ImageItem(filePath, contentUri, name, ImageItem.SOURCE_SDCARD);
                    pagList.add(item);
                    
                    Log.d(TAG, "✅ 成功添加PAG文件: " + name + ", URI: " + contentUri);
                }
                
                Log.d(TAG, "PAG文件查询完成，找到 " + pagList.size() + " 个文件");
            }
        } catch (Exception e) {
            Log.e(TAG, "PAG文件查询失败: " + e.getMessage());
        }
        
        return pagList;
    }
    
    /**
     * 使用直接文件访问作为备用方案（需要所有文件权限）
     */

    /**
     * 通过MediaStore获取指定目录下的图片和PAG文件
     */
    public static List<ImageItem> loadImagesFromSpecificDirectory(Context context, String directoryName) {
        List<ImageItem> imageList = new ArrayList<>();

        if (!PermissionHelper.hasStoragePermission((Activity) context)) {
            Log.w(TAG, "没有存储权限");
            return imageList;
        }

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        // 修改查询条件：查找包含指定目录名的文件
        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + directoryName + "%"};

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder)) {

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH);

                Log.d(TAG, "开始查询目录: " + directoryName);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String filePath = cursor.getString(dataColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    String relativePath = cursor.getString(pathColumn);

                    Log.d(TAG, "找到文件: " + name + ", 路径: " + filePath + ", 相对路径: " + relativePath);

                    // 只处理图片和PAG文件
                    if (isSupportedFile(name, mimeType)) {
                        Uri contentUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        ImageItem item = new ImageItem(filePath, contentUri, name, ImageItem.SOURCE_SDCARD);
                        imageList.add(item);

                        Log.d(TAG, "✅ 成功添加文件: " + name + ", URI: " + contentUri);
                    }
                }

                Log.d(TAG, "从目录 " + directoryName + " 加载了 " + imageList.size() + " 个文件");
            } else {
                Log.e(TAG, "查询返回null cursor");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "权限不足: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "MediaStore查询失败: " + e.getMessage());
            e.printStackTrace();
        }

        return imageList;
    }
    /**
     * 检查是否支持的文件类型
     */
    private static boolean isSupportedFile(String fileName, String mimeType) {
        if (fileName == null) return false;

        String lowerName = fileName.toLowerCase();
        boolean isSupported = lowerName.endsWith(".png") ||
                lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".pag");

        Log.d(TAG, "文件类型检查: " + fileName + " -> " + isSupported);
        return isSupported;
    }
    public static List<ImageItem> loadPagFilesDirectly() {
        List<ImageItem> pagList = new ArrayList<>();

        try {
            File directory = new File("/storage/emulated/0/Download/LionWallpaper");
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pag"));

                if (files != null) {
                    for (File file : files) {
                        Log.d(TAG, "直接找到PAG文件: " + file.getName() + ", 可读: " + file.canRead());

                        if (file.canRead()) {
                            // 使用方法1：使用新的构造方法
                            ImageItem item = new ImageItem(file.getAbsolutePath(), file.getName());
                            pagList.add(item);

                            // 或者使用方法2：使用静态工厂方法
                            // ImageItem item = ImageItem.createSdcardItem(file.getAbsolutePath());

                            Log.d(TAG, "✅ 直接添加PAG文件: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "直接访问PAG文件失败: " + e.getMessage());
        }

        return pagList;
    }
}