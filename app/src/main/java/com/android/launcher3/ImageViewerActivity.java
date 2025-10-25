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

import com.android.launcher3.wallpaper.ImageItem;
import com.android.launcher3.wallpaper.ImagePagerAdapter;
import com.android.launcher3.wallpaper.MediaStoreHelper;
import com.android.launcher3.wallpaper.PermissionHelper;

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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        
        initViews();
        checkPermissionsAndLoad();
        setupListeners();
    }
    private void setupListeners() {
        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmDialog(currentPosition);
        });

        btnReload.setOnClickListener(v -> {
            reloadMediaFiles();
        });

        // 添加调试按钮
        Button btnDebug = findViewById(R.id.btnDebug);
        btnDebug.setOnClickListener(v -> {
            runDebugTests();
        });
    }

    private void runDebugTests() {
        new Thread(() -> {
            Log.d(TAG, "=== 开始调试测试 ===");

            // 测试MediaStore访问
            MediaStoreHelper.testMediaStoreAccess(this);

            // 测试特定目录访问
            List<ImageItem> testFiles = MediaStoreHelper.loadImagesFromSpecificDirectory(this, "LionWallpaper");
            Log.d(TAG, "LionWallpaper目录文件数: " + testFiles.size());

            // 测试所有图片访问
            List<ImageItem> allFiles = MediaStoreHelper.loadAllImages(this);
            Log.d(TAG, "所有图片文件数: " + allFiles.size());

            runOnUiThread(() -> {
                Toast.makeText(this,
                        "调试完成，查看Logcat\nLionWallpaper: " + testFiles.size() +
                                "个\n总图片: " + allFiles.size() + "个",
                        Toast.LENGTH_LONG).show();
            });
        }).start();
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
    private void addMediaStoreFiles() {
        // 方法1：尝试从特定目录加载
        Log.d(TAG, "尝试从 LionWallpaper 目录加载文件...");
        List<ImageItem> specificFiles = MediaStoreHelper.loadImagesFromSpecificDirectory(this, "LionWallpaper");

        if (!specificFiles.isEmpty()) {
            imageList.addAll(specificFiles);
            Log.d(TAG, "从 LionWallpaper 目录成功加载 " + specificFiles.size() + " 个文件");
            return;
        }

        // 方法2：如果特定目录没找到，加载所有图片
        Log.d(TAG, "特定目录未找到文件，尝试加载所有图片...");
        List<ImageItem> allFiles = MediaStoreHelper.loadAllImages(this);
        imageList.addAll(allFiles);

        // 方法3：测试MediaStore访问
        MediaStoreHelper.testMediaStoreAccess(this);
    }

    // 在 loadAllData() 方法中添加调试信息
    private void loadAllData() {
        imageList = new ArrayList<>();

        // 添加本地资源
        addLocalResources();

        // 添加媒体库文件（使用MediaStore）
        addMediaStoreFiles();

        setupViewPager();

        Log.d(TAG, "=== 数据加载统计 ===");
        Log.d(TAG, "总共: " + imageList.size() + " 个文件");
        Log.d(TAG, "媒体库文件: " + getSdcardCount() + " 个");
        Log.d(TAG, "本地资源: " + getLocalCount() + " 个");

        // 显示详细的文件列表
        for (int i = 0; i < imageList.size(); i++) {
            ImageItem item = imageList.get(i);
            Log.d(TAG, "文件[" + i + "]: " + item.getTitle() +
                    " | 来源: " + (item.isFromSDCard() ? "媒体库" : "本地") +
                    " | 类型: " + (item.getType() == ImageItem.TYPE_PAG ? "PAG" : "图片"));
        }

        if (getSdcardCount() == 0) {
            Toast.makeText(this,
                    "未找到媒体库文件\n请检查文件是否在Download/LionWallpaper目录\n查看Logcat获取详细信息",
                    Toast.LENGTH_LONG).show();
        }
    }

    
    private void loadLocalResourcesOnly() {
        imageList = new ArrayList<>();
        addLocalResources();
        setupViewPager();
        Toast.makeText(this, "已加载 " + imageList.size() + " 个内置资源", Toast.LENGTH_SHORT).show();
    }
    
    private void reloadMediaFiles() {
        if (!PermissionHelper.hasStoragePermission(this)) {
            Toast.makeText(this, "没有存储权限", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 移除现有的媒体库文件，保留本地资源
        List<ImageItem> localItems = new ArrayList<>();
        for (ImageItem item : imageList) {
            if (item.isFromLocal()) {
                localItems.add(item);
            }
        }
        
        imageList = localItems;
        addMediaStoreFiles();
        
        if (adapter != null) {
            adapter.updateData(imageList);
            
            // 重置到第一页
            if (!imageList.isEmpty()) {
                viewPager.setCurrentItem(0, false);
                currentPosition = 0;
            }
            
            updatePositionInfo();
            updateFileInfo();
        }
        
        Toast.makeText(this, "重新加载完成，媒体库文件: " + getSdcardCount() + " 个", 
                     Toast.LENGTH_SHORT).show();
    }
    
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
        String source = currentItem.isFromSDCard() ? "媒体库" : "本地资源";
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
}