package com.android.launcher3.wallpaper;

import android.net.Uri;
import java.io.File;

public class ImageItem {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_PAG = 1;
    
    public static final int SOURCE_SDCARD = 0;
    public static final int SOURCE_LOCAL = 1;
    public static final int SOURCE_ASSETS = 2; // 新增assets来源
    private String filePath;
    private Uri fileUri;
    private int type;
    private String title;
    private Integer resourceId;
    private int source;
    private String assetsPath; // 新增assets路径字段

    // SDCard文件构造方法
    public ImageItem(String filePath, String title) {
        this.filePath = filePath;
        this.title = title;
        this.type = getFileType(filePath);
        this.source = SOURCE_SDCARD;
        this.fileUri = null;
        this.resourceId = null;
        this.assetsPath = null;
    }

    // Assets文件构造方法
    public ImageItem(String assetsPath, String title, boolean isPag) {
        this.assetsPath = assetsPath;
        this.title = title;
        this.type = isPag ? TYPE_PAG : TYPE_IMAGE;
        this.source = SOURCE_ASSETS;
        this.filePath = null;
        this.fileUri = null;
        this.resourceId = null;
    }
    
    // 本地资源构造方法
    public ImageItem(int resourceId, String title, boolean isPag) {
        this.resourceId = resourceId;
        this.title = title;
        this.type = isPag ? TYPE_PAG : TYPE_IMAGE;
        this.source = SOURCE_LOCAL;
        this.filePath = null;
        this.fileUri = null;
    }



    // 或者使用现有的构造方法，但需要调整参数
    public ImageItem(String filePath, Uri fileUri, String title, int source) {
        this.filePath = filePath;
        this.fileUri = fileUri;
        this.title = title;
        this.type = getFileType(filePath);
        this.source = source;
        this.resourceId = null;
    }

    // 添加一个便捷方法用于创建SDCard项目
    public static ImageItem createSdcardItem(String filePath) {
        return new ImageItem(filePath, new File(filePath).getName());
    }

    // 或者使用URI创建
    public static ImageItem createSdcardItemWithUri(String filePath, Uri uri) {
        return new ImageItem(filePath, uri, new File(filePath).getName(), SOURCE_SDCARD);
    }
    public static int getFileType(String filePath) {
        if (filePath != null && filePath.toLowerCase().endsWith(".pag")) {
            return TYPE_PAG;
        } else {
            return TYPE_IMAGE;
        }
    }
    
    // Getters
    public String getFilePath() { return filePath; }
    public Uri getFileUri() { return fileUri; }
    public int getType() { return type; }
    public String getTitle() { return title; }
    public Integer getResourceId() { return resourceId; }
    public int getSource() { return source; }
    public File getFile() { return filePath != null ? new File(filePath) : null; }
    public boolean hasUri() { return fileUri != null; }
    public boolean isResource() { return resourceId != null; }
    public boolean isFromSDCard() { return source == SOURCE_SDCARD; }
    public boolean isFromLocal() { return source == SOURCE_LOCAL; }



    /**
     * 获取用于Glide加载的源
     */
    public Object getGlideSource() {
        if (isResource()) {
            return resourceId;
        } else if (hasUri()) {
            return fileUri;
        } else if (filePath != null) {
            return getFile();
        }
        // assets文件Glide无法直接加载，需要特殊处理
        return null;
    }


    /**
     * 获取Assets路径（如果是从Assets加载）
     */
    public String getAssetsPath() {
        return assetsPath;
    }

    /**
     * 获取用于PAG加载的完整路径
     */
    public String getPagFilePath() {
        if (isFromSDCard()) {
            return filePath;
        } else if (isFromAssets()) {
            return "assets://" + assetsPath; // PAG库需要的格式
        }
        return null;
    }

    /**
     * 判断是否是从Assets加载
     */
    public boolean isFromAssets() {
        return source == SOURCE_ASSETS;
    }

}