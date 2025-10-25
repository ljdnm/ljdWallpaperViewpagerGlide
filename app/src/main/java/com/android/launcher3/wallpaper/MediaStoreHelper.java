package com.android.launcher3.wallpaper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreHelper {
    private static final String TAG = "ljd MediaStoreHelper";
    
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
     * 通过MediaStore获取所有图片和PAG文件（不限制目录）
     */
    public static List<ImageItem> loadAllImages(Context context) {
        List<ImageItem> imageList = new ArrayList<>();
        
        if (!PermissionHelper.hasStoragePermission((Activity) context)) {
            return imageList;
        }
        
        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE
        };
        
        // 不设置筛选条件，获取所有图片
        String selection = null;
        String[] selectionArgs = null;
        
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
                
                Log.d(TAG, "开始查询所有图片文件...");
                
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String filePath = cursor.getString(dataColumn);
                    
                    // 只处理图片和PAG文件
                    if (isSupportedFile(name, null)) {
                        Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        
                        ImageItem item = new ImageItem(filePath, contentUri, name, ImageItem.SOURCE_SDCARD);
                        imageList.add(item);
                        
                        Log.d(TAG, "找到文件: " + name + ", 路径: " + filePath);
                    }
                }
                
                Log.d(TAG, "总共加载了 " + imageList.size() + " 个图片/PAG文件");
            }
        } catch (Exception e) {
            Log.e(TAG, "查询所有图片失败: " + e.getMessage());
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
    
    /**
     * 测试MediaStore访问
     */
    public static void testMediaStoreAccess(Context context) {
        Log.d(TAG, "=== MediaStore访问测试 ===");
        
        String[] projection = {MediaStore.Images.Media.DATA};
        
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null)) {
            
            if (cursor != null) {
                Log.d(TAG, "MediaStore总文件数: " + cursor.getCount());
                int count = 0;
                while (cursor.moveToNext() && count < 10) { // 只显示前10个
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    Log.d(TAG, "MediaStore文件[" + count + "]: " + path);
                    count++;
                }
            } else {
                Log.e(TAG, "MediaStore查询返回null");
            }
        } catch (Exception e) {
            Log.e(TAG, "MediaStore测试失败: " + e.getMessage());
        }
    }
}