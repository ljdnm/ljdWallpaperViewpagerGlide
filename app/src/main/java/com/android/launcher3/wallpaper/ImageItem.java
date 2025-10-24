package com.android.launcher3.wallpaper;

import android.net.Uri;
import java.io.File;

public class ImageItem {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_PAG = 1;
    public static final int TYPE_RESOURCE = 2;
    
    public static final int SOURCE_SDCARD = 0;
    public static final int SOURCE_LOCAL = 1;
    
    private String filePath;
    private Uri fileUri;
    private int type;
    private String title;
    private Integer resourceId;
    private int source; // 0: SDCard, 1: Local
    
    // SDCard文件构造方法
    public ImageItem(String filePath, String title) {
        this.filePath = filePath;
        this.title = title;
        this.type = getFileType(filePath);
        this.source = SOURCE_SDCARD;
        this.fileUri = null;
        this.resourceId = null;
    }
    
    // SDCard文件带URI构造方法
    public ImageItem(String filePath, Uri fileUri, String title) {
        this.filePath = filePath;
        this.fileUri = fileUri;
        this.title = title;
        this.type = getFileType(filePath);
        this.source = SOURCE_SDCARD;
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
    
    public static int getFileType(String filePath) {
        if (filePath.toLowerCase().endsWith(".pag")) {
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
        return null;
    }
}