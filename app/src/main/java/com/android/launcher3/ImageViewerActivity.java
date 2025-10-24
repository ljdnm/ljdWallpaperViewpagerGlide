package com.android.launcher3;

import android.net.Uri;
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
    
    private int currentPosition = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        
        initViews();
        initData();
        setupViewPager();
        setupListeners();
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        tvFileName = findViewById(R.id.tvFileName);
        tvFileInfo = findViewById(R.id.tvFileInfo);
        btnDelete = findViewById(R.id.btnDelete);
    }
    
    private void initData() {
        imageList = new ArrayList<>();
        
        // 添加本地资源图片和PAG
        addLocalResources();
        
        // 添加SDCard文件
        addSdcardFiles();
        
        Log.d(TAG, "初始化数据完成，共 " + imageList.size() + " 个文件");
        Log.d(TAG, "其中SDCard: " + getSdcardCount() + " 个, 本地: " + getLocalCount() + " 个");
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
        
        // 添加本地PAG资源（如果有的话）
        // int[] pagResources = {R.raw.animation1, R.raw.animation2};
        // String[] pagNames = {"动画1", "动画2"};
        // for (int i = 0; i < pagResources.length; i++) {
        //     ImageItem item = new ImageItem(pagResources[i], pagNames[i], true);
        //     imageList.add(item);
        // }
    }
    
    private void addSdcardFiles() {
        // 这里添加您获取到的SDCard文件路径
        String[] filePaths = {
            "/storage/emulated/0/LionWallpaper/111.png",
            "/storage/emulated/0/LionWallpaper/222.pag",
            "/storage/emulated/0/LionWallpaper/333.png"
        };
        
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                ImageItem item = new ImageItem(filePath, file.getName());
                imageList.add(item);
                Log.d(TAG, "添加SDCard文件: " + filePath);
            } else {
                Log.w(TAG, "SDCard文件不存在: " + filePath);
            }
        }
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
                Log.d(TAG, "当前显示位置: " + position + ", 文件: " + imageList.get(position).getTitle());
            }
        });
        
        updatePositionInfo();
        updateFileInfo();
    }
    
    private void setupListeners() {
        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmDialog(currentPosition);
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
            Log.d(TAG, "删除文件: " + deletedName + ", 剩余: " + imageList.size() + " 个");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            // 确保释放PAG资源
            adapter = null;
        }
    }
}