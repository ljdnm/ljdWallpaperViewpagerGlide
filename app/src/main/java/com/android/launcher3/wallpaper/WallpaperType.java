// WallpaperType.java
package com.android.launcher3.wallpaper;

public class WallpaperType {
    public static final int DEFAULT = 0;    // 默认壁纸
    public static final int WUGAN = 1;      // 五感壁纸  
    public static final int FENGYUN = 2;    // 风云壁纸
    
    public static String getName(int type) {
        switch (type) {
            case DEFAULT: return "默认壁纸";
            case WUGAN: return "五感壁纸";
            case FENGYUN: return "风云壁纸";
            default: return "未知";
        }
    }
}