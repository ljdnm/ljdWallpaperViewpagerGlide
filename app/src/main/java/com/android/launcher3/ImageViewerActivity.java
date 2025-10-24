package com.android.launcher3;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.launcher3.wallpaper.FileAccessHelper;
import com.android.launcher3.wallpaper.ImageItem;
import com.android.launcher3.wallpaper.ImagePagerAdapter;
import com.android.launcher3.wallpaper.PermissionChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String TAG = "ImageViewerActivity";
    
    private ViewPager2 viewPager;
    private ImagePagerAdapter adapter;
    private List<ImageItem> imageList;
    
    private TextView tvCurrentPosition;
    private TextView tvFileName;
    private TextView tvFileInfo;
    private Button btnDelete;
    private Button btnReload;
    
    private int currentPosition = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        
        initViews();
        checkPermissionsAndLoad();
        setupListeners();
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        tvFileName = findViewById(R.id.tvFileName);
        tvFileInfo = findViewById(R.id.tvFileInfo);
        btnDelete = findViewById(R.id.btnDelete);
        btnReload = findViewById(R.id.btnReload);
    }
    
    private void checkPermissionsAndLoad() {
        if (PermissionChecker.hasStoragePermission(this)) {
            loadAllData();
        } else {
            showPermissionDialog();
        }
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("需要存储权限")
            .setMessage("需要存储权限来访问SDCard中的图片和PAG文件")
            .setPositiveButton("授权", (dialog, which) -> {
                PermissionChecker.requestStoragePermission(this);
            })
            .setNegativeButton("仅使用内置资源", (dialog, which) -> {
                loadLocalResourcesOnly();
            })
            .setCancelable(false)
            .show();
    }
    
    private void loadAllData() {
        imageList = new ArrayList<>();
        
        // 添加本地资源
        addLocalResources();
        
        // 添加SDCard文件
        addSdcardFiles();
        
        setupViewPager();
        
        Log.d(TAG, "数据加载完成，总共: " + imageList.size() + " 个文件");
        Log.d(TAG, "SDCard文件: " + getSdcardCount() + " 个");
        Log.d(TAG, "本地资源: " + getLocalCount() + " 个");
        
        if (getSdcardCount() == 0) {
            Toast.makeText(this, "未找到SDCard文件，请检查权限和文件路径", Toast.LENGTH_LONG).show();
        }
    }
    
    private void addLocalResources() {
        // 添加本地PNG资源
        int[] pngResources = {
            R.drawable.jtbz_zrsg_alphelia,
            R.drawable.jtbz_zrsg_crimsono,
            R.drawable.jtbz_zrsg_equinoxis,
            R.drawable.jtbz_zrsg_harmonia,
            R.drawable.jtbz_zrsg_thalassa,
            R.drawable.jtbz_zrsg_zephyria
        };
        
        String[] pngNames = {
            "阿尔菲莉亚", "克里姆森", "伊奎诺克西斯", 
            "哈莫尼亚", "塔拉萨", "泽菲莉亚"
        };
        
        for (int i = 0; i < pngResources.length; i++) {
            ImageItem item = new ImageItem(pngResources[i], pngNames[i], false);
            imageList.add(item);
        }
    }
    
    private void addSdcardFiles() {
        // 测试多个可能的目录路径
        String[] possibleDirectories = {
            "/storage/emulated/0/LionWallpaper",
            "/sdcard/LionWallpaper",
            Environment.getExternalStorageDirectory() + "/LionWallpaper"
        };
        
        List<String> allSdcardFiles = new ArrayList<>();
        
        for (String directory : possibleDirectories) {
            Log.d(TAG, "扫描目录: " + directory);
            List<String> files = FileAccessHelper.scanDirectory(directory);
            allSdcardFiles.addAll(files);
        }
        
        // 如果没有扫描到文件，尝试直接添加已知文件路径
        if (allSdcardFiles.isEmpty()) {
            Log.d(TAG, "目录扫描未找到文件，尝试直接添加已知文件");
            addKnownSdcardFiles();
        } else {
            // 添加扫描到的文件
            for (String filePath : allSdcardFiles) {
                ImageItem item = new ImageItem(filePath, new File(filePath).getName());
                imageList.add(item);
                Log.d(TAG, "成功添加SDCard文件: " + filePath);
            }
        }
    }
    
    private void addKnownSdcardFiles() {
        // 直接添加已知的文件路径进行测试
        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] testFiles = {
                basePath + "/LionWallpaper/111.png",
                basePath + "/LionWallpaper/222.jpg",
                basePath + "/LionWallpaper/blue_bmp.pag",
                basePath + "/LionWallpaper/red_bmp.pag",
                basePath + "/LionWallpaper/white_bmp.pag",
                basePath + "/LionWallpaper/test.pag"
        };
//        String[] testFiles = {
//            "/storage/emulated/0/LionWallpaper/111.png",
//            "/storage/emulated/0/LionWallpaper/222.pag",
//            "/storage/emulated/0/LionWallpaper/333.png",
//            "/sdcard/LionWallpaper/111.png",
//            "/sdcard/LionWallpaper/222.pag"
//        };
        
        for (String filePath : testFiles) {
            if (FileAccessHelper.isFileAccessible(filePath)) {
                ImageItem item = new ImageItem(filePath, new File(filePath).getName());
                imageList.add(item);
                Log.d(TAG, "直接添加成功: " + filePath);
                
                // 显示文件信息用于调试
                String fileInfo = FileAccessHelper.getFileInfo(filePath);
                Log.d(TAG, "文件信息: " + fileInfo);
            } else {
                Log.w(TAG, "文件不可访问: " + filePath);
            }
        }
    }
    
    private void loadLocalResourcesOnly() {
        imageList = new ArrayList<>();
        addLocalResources();
        setupViewPager();
        Toast.makeText(this, "已加载 " + imageList.size() + " 个内置资源", Toast.LENGTH_SHORT).show();
    }
    
//    private void reloadSdcardFiles() {
//        if (!PermissionChecker.hasStoragePermission(this)) {
//            Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 移除现有的SDCard文件，保留本地资源
//        List<ImageItem> localItems = new ArrayList<>();
//        for (ImageItem item : imageList) {
//            if (item.isFromLocal()) {
//                localItems.add(item);
//            }
//        }
//
//        imageList = localItems;
//        addSdcardFiles();
//
//        if (adapter != null) {
//            adapter.updateData(imageList);
//            updatePositionInfo();
//            updateFileInfo();
//        }
//
//        Toast.makeText(this, "重新加载完成，SDCard文件: " + getSdcardCount() + " 个",
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
    
    private void setupListeners() {
        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmDialog(currentPosition);
        });
        
        btnReload.setOnClickListener(v -> {
            reloadSdcardFiles();
        });
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
        String source = currentItem.isFromSDCard() ? "SDCard" : "本地资源";
        
        if (currentItem.isFromSDCard() && currentItem.getFilePath() != null) {
            String fileInfo = FileAccessHelper.getFileInfo(currentItem.getFilePath());
            tvFileInfo.setText("类型: " + type + " | 来源: " + source + " | " + fileInfo);
        } else {
            tvFileInfo.setText("类型: " + type + " | 来源: " + source);
        }
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
                        (item.isFromSDCard() ? "SDCard" : "本地资源");
        
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage(message)
            .setPositiveButton("删除", (dialog, which) -> {
                deleteItem(position);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    private void reloadSdcardFiles() {
        if (!PermissionChecker.hasStoragePermission(this)) {
            Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show();
            return;
        }

        // 移除现有的SDCard文件，保留本地资源
        List<ImageItem> localItems = new ArrayList<>();
        for (ImageItem item : imageList) {
            if (item.isFromLocal()) {
                localItems.add(item);
            }
        }

        imageList = localItems;
        addSdcardFiles();

        if (adapter != null) {
            adapter.updateData(imageList); // 现在这个方法存在了

            // 重置到第一页
            if (!imageList.isEmpty()) {
                viewPager.setCurrentItem(0, false);
                currentPosition = 0;
            }

            updatePositionInfo();
            updateFileInfo();
        }

        Toast.makeText(this, "重新加载完成，SDCard文件: " + getSdcardCount() + " 个",
                Toast.LENGTH_SHORT).show();
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                loadAllData();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show();
                loadLocalResourcesOnly();
            }
        }
    }
}