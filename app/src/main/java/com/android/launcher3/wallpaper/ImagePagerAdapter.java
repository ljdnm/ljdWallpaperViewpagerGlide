package com.android.launcher3.wallpaper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import org.libpag.PAGView;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
    private Context context;
    private List<ImageItem> imageList;
    private OnItemClickListener onItemClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
    
    public ImagePagerAdapter(Context context, List<ImageItem> imageList) {
        this.context = context;
        this.imageList = imageList;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void removeItem(int position) {
        if (position >= 0 && position < imageList.size()) {
            imageList.remove(position);
            notifyItemRemoved(position);
            
            if (position < imageList.size()) {
                notifyItemRangeChanged(position, imageList.size() - position);
            }
        }
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_pager, parent, false);
        return new ViewHolder(view);
    }
    /**
     * 更新数据的方法
     */
    public void updateData(List<ImageItem> newImageList) {
        this.imageList = newImageList;
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageItem item = imageList.get(position);
        holder.bind(item, position);
    }
    
    @Override
    public int getItemCount() {
        return imageList.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvTitle;
        TextView tvType;
        TextView tvSource;
        View pagContainer;
        PAGView pagView;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvSource = itemView.findViewById(R.id.tvSource);
            pagContainer = itemView.findViewById(R.id.pagContainer);
            pagView = itemView.findViewById(R.id.pagView);
        }
        
        public void bind(ImageItem item, int position) {
            tvTitle.setText(item.getTitle());
            tvSource.setText(item.isFromSDCard() ? "SDCard" : "本地");
            
            if (item.getType() == ImageItem.TYPE_PAG) {
                bindPagItem(item);
            } else {
                bindImageItem(item);
            }
        }
        
        private void bindImageItem(ImageItem item) {
            tvType.setText("图片");
            pagContainer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            
            if (pagView != null) {
                pagView.stop();
                pagView.freeCache();
            }
            
            RequestOptions options = new RequestOptions()
                    .override(1080, 1920)
                    .centerInside();
            
            Object glideSource = item.getGlideSource();
            if (glideSource != null) {
                Glide.with(context)
                        .load(glideSource)
                        .apply(options)
                        .into(imageView);
            }
        }
        
        private void bindPagItem(ImageItem item) {
            tvType.setText("PAG动画");
            pagContainer.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            
            // 加载PAG动画
            loadPagAnimation(item);
        }
        
        private void loadPagAnimation(ImageItem item) {
            if (pagView == null) {
                return;
            }
            
            try {
                String filePath = null;
                
                if (item.isFromSDCard()) {
                    // SDCard的PAG文件
                    filePath = item.getFilePath();
                } else if (item.isFromLocal()) {
                    // 本地资源的PAG文件（需要从assets或raw读取）
                    // 这里需要根据您的资源存放位置调整
                    filePath = "assets://" + item.getTitle(); // 示例，实际需要具体实现
                }
                
                if (filePath != null) {
                    // 设置PAG文件并播放
                    pagView.setPath(filePath);
                    pagView.setRepeatCount(-1); // 无限循环
                    pagView.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
                tvType.setText("PAG加载失败");
            }
        }
        
        public void releasePag() {
            if (pagView != null) {
                pagView.stop();
                pagView.freeCache();
            }
        }
        
        @Override
        protected void finalize() throws Throwable {
            releasePag();
            super.finalize();
        }
    }
    
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.releasePag();
    }
}