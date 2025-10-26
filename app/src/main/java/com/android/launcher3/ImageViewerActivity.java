package com.android.launcher3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.launcher3.wallpaper.AllFilesAccessHelper;
import com.android.launcher3.wallpaper.AssetsHelper;
import com.android.launcher3.wallpaper.FileSearchHelper;
import com.android.launcher3.wallpaper.ImageItem;
import com.android.launcher3.wallpaper.ImagePagerAdapter;
import com.android.launcher3.wallpaper.MediaStoreHelper;
import com.android.launcher3.wallpaper.PermissionHelper;
import com.android.launcher3.wallpaper.SpecifiedFileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String TAG = "ljd ImageViewerActivity";
    
    private ViewPager2 viewPager;
    private ImagePagerAdapter adapter;
    private List<ImageItem> imageList;
    
    private TextView tvCurrentPosition;
    private TextView tvFileName;
    private TextView tvFileInfo;
    private Button btnDelete;
    private Button btnReload;
    
    private int currentPosition = 0;
    


    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        tvFileName = findViewById(R.id.tvFileName);
        tvFileInfo = findViewById(R.id.tvFileInfo);
        btnDelete = findViewById(R.id.btnDelete);
        btnReload = findViewById(R.id.btnReload);
    }
    
    private void checkPermissionsAndLoad() {
        if (PermissionHelper.hasStoragePermission(this)) {
            loadAllData();
        } else {
            showPermissionDialog();
        }
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("需要存储权限")
            .setMessage("需要存储权限来访问媒体库中的图片和PAG文件\n\n" +
                       "建议将文件放在以下目录：\n" +
                       "• Pictures/ - 图片目录\n" +
                       "• Download/ - 下载目录")
            .setPositiveButton("授权", (dialog, which) -> {
                PermissionHelper.requestStoragePermission(this);
            })
            .setNegativeButton("仅使用内置资源", (dialog, which) -> {
                loadLocalResourcesOnly();
            })
            .setCancelable(false)
            .show();
    }
    

    
//    private void addLocalResources() {
//        // 添加本地PNG资源
//        int[] pngResources = {
//            R.drawable.jtbz_zrsg_alphelia,
//            R.drawable.jtbz_zrsg_crimsono,
//            R.drawable.jtbz_zrsg_equinoxis,
//            R.drawable.jtbz_zrsg_harmonia,
//            R.drawable.jtbz_zrsg_thalassa,
//            R.drawable.jtbz_zrsg_zephyria
//        };
//
//        String[] pngNames = {
//            "阿尔菲莉亚", "克里姆森", "伊奎诺克西斯",
//            "哈莫尼亚", "塔拉萨", "泽菲莉亚"
//        };
//
//        for (int i = 0; i < pngResources.length; i++) {
//            ImageItem item = new ImageItem(pngResources[i], pngNames[i], false);
//            imageList.add(item);
//        }
//    }


    
    private void loadLocalResourcesOnly() {
        imageList = new ArrayList<>();
        addLocalResources();
        setupViewPager();
        Toast.makeText(this, "已加载 " + imageList.size() + " 个内置资源", Toast.LENGTH_SHORT).show();
    }
    
//    private void reloadMediaFiles() {
//        if (!PermissionHelper.hasStoragePermission(this)) {
//            Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        updatePermissionStatus();
//        // 移除现有的媒体库文件，保留本地资源
//        List<ImageItem> localItems = new ArrayList<>();
//        for (ImageItem item : imageList) {
//            if (item.isFromLocal()) {
//                localItems.add(item);
//            }
//        }
//
//        imageList = localItems;
//        addMediaStoreFiles();
//
//        if (adapter != null) {
//            adapter.updateData(imageList);
//
//            // 重置到第一页
//            if (!imageList.isEmpty()) {
//                viewPager.setCurrentItem(0, false);
//                currentPosition = 0;
//            }
//
//            updatePositionInfo();
//            updateFileInfo();
//        }
//
//        Toast.makeText(this, "重新加载完成，媒体库文件: " + getSdcardCount() + " 个",
//                     Toast.LENGTH_SHORT).show();
//    }
    
    private int getSdcardCount() {
        int count = 0;
        for (ImageItem item : imageList) {
            if (item.isFromSDCard()) count++;
        }
        return count;
    }
    
    private int getLocalCount() {
        int count = 0;
        for (ImageItem item : imageList) {
            if (item.isFromLocal()) count++;
        }
        return count;
    }
    
    private void setupViewPager() {
        adapter = new ImagePagerAdapter(this, imageList);
        
        adapter.setOnItemClickListener(new ImagePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                toggleInfoVisibility();
            }
            
            @Override
            public void onItemLongClick(int position) {
                showDeleteConfirmDialog(position);
            }
        });
        
        viewPager.setAdapter(adapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updatePositionInfo();
                updateFileInfo();
            }
        });
        
        updatePositionInfo();
        updateFileInfo();
    }
    

    
    private void updatePositionInfo() {
        String positionText = (currentPosition + 1) + " / " + imageList.size();
        tvCurrentPosition.setText(positionText);
    }

    private void updateFileInfo() {
        if (imageList.isEmpty()) {
            tvFileName.setText("无文件");
            tvFileInfo.setText("");
            return;
        }

        ImageItem currentItem = imageList.get(currentPosition);
        tvFileName.setText(currentItem.getTitle());

        String type = currentItem.getType() == ImageItem.TYPE_PAG ? "PAG动画" : "图片";
        String source = "";

        if (currentItem.isFromSDCard()) {
            source = "SDCard";
        } else if (currentItem.isFromAssets()) {
            source = "Assets资源";
        } else if (currentItem.isFromLocal()) {
            source = "本地资源";
        }

        tvFileInfo.setText("类型: " + type + " | 来源: " + source);
    }
    
    private void toggleInfoVisibility() {
        View topInfo = findViewById(R.id.topInfoLayout);
        View bottomInfo = findViewById(R.id.bottomIndicator);
        
        if (topInfo.getVisibility() == View.VISIBLE) {
            topInfo.setVisibility(View.GONE);
            bottomInfo.setVisibility(View.GONE);
        } else {
            topInfo.setVisibility(View.VISIBLE);
            bottomInfo.setVisibility(View.VISIBLE);
        }
    }
    
    private void showDeleteConfirmDialog(int position) {
        if (position < 0 || position >= imageList.size()) return;
        
        ImageItem item = imageList.get(position);
        String message = "确定要删除 \"" + item.getTitle() + "\" 吗？\n来源: " + 
                        (item.isFromSDCard() ? "媒体库" : "本地资源");
        
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage(message)
            .setPositiveButton("删除", (dialog, which) -> {
                deleteItem(position);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void deleteItem(int position) {
        if (position < 0 || position >= imageList.size()) return;
        
        String deletedName = imageList.get(position).getTitle();
        adapter.removeItem(position);
        
        if (imageList.isEmpty()) {
            finish();
        } else {
            if (currentPosition >= imageList.size()) {
                currentPosition = imageList.size() - 1;
            }
            updatePositionInfo();
            updateFileInfo();
            
            Toast.makeText(this, "已删除: " + deletedName, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 1001) {
            if (PermissionHelper.isPermissionGranted(grantResults)) {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                loadAllData();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show();
                loadLocalResourcesOnly();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter = null;
        }
    }


//    private void runPagDebugTests() {
//        new Thread(() -> {
//            Log.d(TAG, "=== 增强PAG文件调试测试 ===");
//
//            // 全面搜索PAG文件
//            List<ImageItem> pagFiles = FileSearchHelper.searchPagFiles();
//
//            runOnUiThread(() -> {
//                String message = "PAG文件搜索完成\n" +
//                        "找到: " + pagFiles.size() + " 个PAG文件\n" +
//                        "查看Logcat获取文件位置";
//                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//
//                // 如果找到了PAG文件，立即加载
//                if (!pagFiles.isEmpty()) {
//                    imageList.addAll(pagFiles);
//                    if (adapter != null) {
//                        adapter.updateData(imageList);
//                        updatePositionInfo();
//                        updateFileInfo();
//                    }
//                }
//            });
//        }).start();
//    }
    private void runDetailedPagDebug() {
        new Thread(() -> {
            Log.d(TAG, "=== 详细PAG文件调试 ===");

            // 测试已知文件
            List<ImageItem> knownFiles = FileSearchHelper.loadKnownPagFiles();

            // 测试目录扫描
            List<ImageItem> scannedFiles = FileSearchHelper.scanPagFiles();

            // 测试文件权限
            String[] testFiles = {
                    "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
                    "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
                    "/storage/emulated/0/Download/LionWallpaper/test.pag",
                    "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
            };

            for (String filePath : testFiles) {
                File file = new File(filePath);
                Log.d(TAG, "文件权限测试: " + filePath);
                Log.d(TAG, "  存在: " + file.exists());
                Log.d(TAG, "  可读: " + file.canRead());
                Log.d(TAG, "  可写: " + file.canWrite());
                Log.d(TAG, "  可执行: " + file.canExecute());
                Log.d(TAG, "  大小: " + file.length());
                Log.d(TAG, "  路径: " + file.getAbsolutePath());
            }

            runOnUiThread(() -> {
                String message = "PAG调试完成\n" +
                        "已知文件: " + knownFiles.size() + " 个\n" +
                        "扫描文件: " + scannedFiles.size() + " 个\n" +
                        "查看Logcat获取详细信息";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // 立即加载找到的PAG文件
                if (!knownFiles.isEmpty()) {
                    imageList.addAll(knownFiles);
                    if (adapter != null) {
                        adapter.updateData(imageList);
                        updatePositionInfo();
                        updateFileInfo();
                        Toast.makeText(this, "已加载 " + knownFiles.size() + " 个PAG文件", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }).start();
    }
//    private void addMediaStoreFiles() {
//        // 方法1：尝试从特定目录加载图片
//        Log.d(TAG, "尝试从 LionWallpaper 目录加载图片文件...");
//        List<ImageItem> specificFiles = MediaStoreHelper.loadImagesFromSpecificDirectory(this, "LionWallpaper");
//
//        if (!specificFiles.isEmpty()) {
//            imageList.addAll(specificFiles);
//            Log.d(TAG, "从 LionWallpaper 目录成功加载 " + specificFiles.size() + " 个图片文件");
//        }
//
//        // 方法2：直接加载已知的PAG文件
//        Log.d(TAG, "直接加载已知PAG文件...");
//        List<ImageItem> knownPagFiles = FileSearchHelper.loadKnownPagFiles();
//
//        if (!knownPagFiles.isEmpty()) {
//            imageList.addAll(knownPagFiles);
//            Log.d(TAG, "成功加载 " + knownPagFiles.size() + " 个已知PAG文件");
//        } else {
//            Log.w(TAG, "已知PAG文件加载失败，尝试扫描...");
//
//            // 方法3：扫描目录
//            List<ImageItem> scannedPagFiles = FileSearchHelper.scanPagFiles();
//            if (!scannedPagFiles.isEmpty()) {
//                imageList.addAll(scannedPagFiles);
//                Log.d(TAG, "扫描找到 " + scannedPagFiles.size() + " 个PAG文件");
//            }
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        initViews();
        checkAllFilesAccessPermission();
        setupListeners();
        updatePermissionStatus();
    }

    private void checkAllFilesAccessPermission() {
        if (AllFilesAccessHelper.hasAllFilesAccessPermission()) {
            // 有所有文件权限，可以加载PAG文件
            loadAllData();
        } else {
            // 请求所有文件权限
            showAllFilesAccessDialog();
        }
        updatePermissionStatus(); // 添加这里
    }

//    private void showAllFilesAccessDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("需要所有文件访问权限")
//                .setMessage("为了加载PAG动画文件，需要授予应用\"所有文件访问\"权限。\n\n" +
//                        "请在接下来的设置页面中开启\"允许访问所有文件\"选项。\n\n" +
//                        "PNG/JPG文件可以正常显示，但PAG文件需要此额外权限。")
//                .setPositiveButton("去设置", (dialog, which) -> {
//                    AllFilesAccessHelper.requestAllFilesAccessPermission(this);
//                })
//                .setNegativeButton("仅加载图片", (dialog, which) -> {
//                    loadImagesOnly();
//                })
//                .setCancelable(false)
//                .show();
//    }

//    private void loadImagesOnly() {
//        imageList = new ArrayList<>();
//
//        // 添加本地资源
//        addLocalResources();
//
//        // 只加载图片文件（不需要所有文件权限）
//        addMediaStoreImages();
//
//        setupViewPager();
//
//        Toast.makeText(this, "已加载 " + imageList.size() + " 个图片文件", Toast.LENGTH_SHORT).show();
//    }

    private void addMediaStoreImages() {
        // 只加载通过MediaStore可访问的图片
        List<ImageItem> imageFiles = MediaStoreHelper.loadImagesFromSpecificDirectory(this, "LionWallpaper");
        imageList.addAll(imageFiles);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查用户是否从设置页面返回并授予了权限
        if (AllFilesAccessHelper.hasAllFilesAccessPermission()) {
            // 重新加载数据，包括PAG文件
            loadAllData();
            Toast.makeText(this, "所有文件访问权限已开启", Toast.LENGTH_SHORT).show();
        }
        updatePermissionStatus();
    }


    private void updatePermissionStatus() {
        TextView tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        if (tvPermissionStatus == null) return;

        if (AllFilesAccessHelper.hasAllFilesAccessPermission()) {
            tvPermissionStatus.setText("全权限");
            tvPermissionStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            tvPermissionStatus.setBackgroundColor(getColor(android.R.color.transparent));
        } else {
            tvPermissionStatus.setText("受限");
            tvPermissionStatus.setTextColor(getColor(android.R.color.holo_red_dark));
            tvPermissionStatus.setBackgroundColor(0x22FF0000); // 浅红色背景
        }

        Log.d(TAG, "权限状态更新: " +
                (AllFilesAccessHelper.hasAllFilesAccessPermission() ? "全权限" : "受限"));
    }
    private void showAllFilesAccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("需要所有文件访问权限")
                .setMessage("为了加载PAG动画文件，需要授予应用\"所有文件访问\"权限。\n\n" +
                        "请在接下来的设置页面中开启\"允许访问所有文件\"选项。\n\n" +
                        "PNG/JPG文件可以正常显示，但PAG文件需要此额外权限。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    AllFilesAccessHelper.requestAllFilesAccessPermission(this);
                    updatePermissionStatus(); // 添加这里
                })
                .setNegativeButton("仅加载图片", (dialog, which) -> {
                    loadImagesOnly();
                    updatePermissionStatus(); // 添加这里
                })
                .setCancelable(false)
                .show();
    }
    // 在权限请求后的回调中也调用
    private void onAllFilesPermissionGranted() {
        updatePermissionStatus();
        loadAllData();
    }


//    private void loadAllData() {
//        imageList = new ArrayList<>();
//
//        // 添加本地资源图片
//        addLocalResources();
//
//        // 添加assets PAG文件
//        addAssetsPagFiles();
//
//        // 添加SDCard文件
//        addMediaStoreFiles();
//
//        setupViewPager();
//
//        Log.d(TAG, "数据加载完成，总共: " + imageList.size() + " 个文件");
//        Log.d(TAG, "assets文件: " + getAssetsCount() + " 个");
//        Log.d(TAG, "SDCard文件: " + getSdcardCount() + " 个");
//        Log.d(TAG, "本地资源: " + getLocalCount() + " 个");
//    }
//
//    private void addAssetsPagFiles() {
//        Log.d(TAG, "开始加载assets PAG文件...");
//        List<ImageItem> assetsFiles = AssetsHelper.loadPagFilesFromAssets(this);
//
//        if (!assetsFiles.isEmpty()) {
//            imageList.addAll(assetsFiles);
//            Log.d(TAG, "成功加载 " + assetsFiles.size() + " 个assets PAG文件");
//        } else {
//            Log.w(TAG, "未找到assets PAG文件");
//            // 测试assets访问
//            AssetsHelper.testAssetsAccess(this);
//        }
//    }

    private int getAssetsCount() {
        int count = 0;
        for (ImageItem item : imageList) {
            if (item.isFromAssets()) count++;
        }
        return count;
    }




    /**
     * 修改原有的loadAllData方法，改为只加载指定文件
     */
    private void loadAllData() {
        loadSpecifiedFiles(); // 替换原来的扫描逻辑
    }

    /**
     * 修改addMediaStoreFiles方法，只加载指定文件
     */
    private void addMediaStoreFiles() {
        Log.d(TAG, "开始加载指定SDCard文件...");

        // 使用SpecifiedFileManager中的逻辑来添加SDCard文件
        String[] specifiedSdcardFiles = {
                "/storage/emulated/0/Download/LionWallpaper/blue_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/red_bmp.pag",
                "/storage/emulated/0/Download/LionWallpaper/test.pag",
                "/storage/emulated/0/Download/LionWallpaper/white_bmp.pag"
        };

        for (String filePath : specifiedSdcardFiles) {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                ImageItem item = new ImageItem(filePath, file.getName());
                imageList.add(item);
                Log.d(TAG, "✅ 加载指定SDCard文件: " + file.getName());
            } else {
                Log.w(TAG, "❌ 指定SDCard文件不可访问: " + filePath);
            }
        }

        Log.d(TAG, "SDCard指定文件加载完成: " + getSdcardCount() + " 个");
    }

    /**
     * 修改addAssetsPagFiles方法，只加载指定文件
     */
    private void addAssetsPagFiles() {
        Log.d(TAG, "开始加载指定Assets PAG文件...");

        // 使用SpecifiedFileManager中的逻辑来添加Assets文件
        String[] specifiedAssetsFiles = {
                "animations/wgbz_ssbz_1-day_cold_smallwind.pag",
                "animations/wgbz_ssbz_1-night_warm_strongwind.pag"
        };

        String[] displayNames = {
                "白天寒冷微风",
                "夜晚温暖强风"
        };

        for (int i = 0; i < specifiedAssetsFiles.length; i++) {
            String assetPath = specifiedAssetsFiles[i];
            String displayName = displayNames[i];

            try {
                getAssets().open(assetPath).close();
                ImageItem item = new ImageItem(assetPath, displayName, true);
                imageList.add(item);
                Log.d(TAG, "✅ 加载指定Assets文件: " + assetPath);
            } catch (Exception e) {
                Log.w(TAG, "❌ 指定Assets文件不存在: " + assetPath);
            }
        }

        Log.d(TAG, "Assets指定文件加载完成: " + getAssetsCount() + " 个");
    }

    /**
     * 修改addLocalResources方法
     */
    private void addLocalResources() {
        // 使用SpecifiedFileManager中的逻辑来添加本地资源
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
            imageList.add(item);
        }
    }

    /**
     * 修改reloadMediaFiles方法
     */
    private void reloadMediaFiles() {
        if (!PermissionHelper.hasStoragePermission(this)) {
            Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show();
            return;
        }

        // 重新加载指定文件
        loadSpecifiedFiles();
        Toast.makeText(this, "重新加载指定文件完成", Toast.LENGTH_SHORT).show();
    }

    /**
     * 修改loadImagesOnly方法
     */
    private void loadImagesOnly() {
        imageList = new ArrayList<>();

        // 只加载指定的本地PNG资源
        addLocalResources();

        setupViewPager();
        Toast.makeText(this, "已加载 " + imageList.size() + " 个指定图片文件", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        Button btnPagDebug = findViewById(R.id.btnPagDebug);
        btnPagDebug.setOnClickListener(v -> runDetailedPagDebug());

        // 添加Assets调试按钮
        Button btnAssetsDebug = findViewById(R.id.btnAssetsDebug);
        if (btnAssetsDebug != null) {
            btnAssetsDebug.setOnClickListener(v -> {
                runAssetsDebugTest();
            });
        }

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmDialog(currentPosition);
        });

        btnReload.setOnClickListener(v -> {
            reloadMediaFiles();
        });

        // 添加权限状态检查按钮
        Button btnCheckPermission = findViewById(R.id.btnCheckPermission);
        if (btnCheckPermission != null) {
            btnCheckPermission.setOnClickListener(v -> {
                updatePermissionStatus();
                Toast.makeText(this,
                        "权限状态: " + (AllFilesAccessHelper.hasAllFilesAccessPermission() ? "全权限" : "受限"),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Assets调试测试
     */
    private void runAssetsDebugTest() {
        new Thread(() -> {
            Log.d(TAG, "=== 开始Assets调试测试 ===");

            try {
                // 列出所有Assets根目录
                String[] rootAssets = getAssets().list("");
                Log.d(TAG, "Assets根目录文件数: " + (rootAssets != null ? rootAssets.length : "null"));
                if (rootAssets != null) {
                    for (String asset : rootAssets) {
                        Log.d(TAG, "Assets根目录: " + asset);
                    }
                }

                // 检查animations目录
                String[] animations = getAssets().list("animations");
                Log.d(TAG, "animations目录文件数: " + (animations != null ? animations.length : "null"));
                if (animations != null) {
                    for (String anim : animations) {
                        Log.d(TAG, "animations文件: " + anim);

                        // 尝试打开每个文件
                        String fullPath = "animations/" + anim;
                        try {
                            getAssets().open(fullPath).close();
                            Log.d(TAG, "✅ 可打开: " + fullPath);
                        } catch (IOException e) {
                            Log.d(TAG, "❌ 不可打开: " + fullPath + ", 错误: " + e.getMessage());
                        }
                    }
                }

                // 测试指定的文件
                String[] testFiles = {
                        "animations/wgbz_ssbz_1-day_cold_smallwind.pag",
                        "animations/wgbz_ssbz_1-night_warm_strongwind.pag"
                };

                for (String testFile : testFiles) {
                    try {
                        getAssets().open(testFile).close();
                        Log.d(TAG, "🎯 指定文件可访问: " + testFile);
                    } catch (IOException e) {
                        Log.d(TAG, "💥 指定文件不可访问: " + testFile + ", 错误: " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Assets调试异常: " + e.getMessage());
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Assets调试完成，查看Logcat", Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    /**
     * 加载指定的文件列表
     */
    private void loadSpecifiedFiles() {
        Log.d(TAG, "=== 开始加载指定文件 ===");
        imageList = SpecifiedFileManager.createSpecifiedFileList(this);
        setupViewPager();

        String stats = SpecifiedFileManager.getFileStatistics(imageList);
        Log.d(TAG, "指定文件加载完成，总共: " + imageList.size() + " 个文件");
        Log.d(TAG, "统计: " + stats);

        Toast.makeText(this,
                "已加载 " + imageList.size() + " 个指定文件\n" + stats,
                Toast.LENGTH_LONG).show();
    }
}