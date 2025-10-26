package com.android.launcher3.wallpaper;

import android.content.Context;
import android.util.Log;
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

import java.io.File;
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
    
    public void updateData(List<ImageItem> newImageList) {
        this.imageList = newImageList;
        notifyDataSetChanged();
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

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemLongClick(getAdapterPosition());
                    return true;
                }
                return false;
            });
        }

        public void bind(ImageItem item, int position) {
            tvTitle.setText(item.getTitle());
            tvSource.setText(item.isFromSDCard() ? "媒体库" : "本地");

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
                // Glide会自动处理Content URI
                Glide.with(context)
                        .load(glideSource)
                        .apply(options)
                        .into(imageView);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        private void bindPagItem(ImageItem item) {
            tvType.setText("PAG动画");
            pagContainer.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            loadPagAnimation(item);
        }



        private void loadPagAnimation(ImageItem item) {
            if (pagView == null) {
                Log.e("ImagePagerAdapter", "PAGView为null");
                return;
            }

            try {
                String pagPath = item.getPagFilePath();
                Log.d("ImagePagerAdapter", "开始加载PAG文件，来源: " +
                        (item.isFromSDCard() ? "SDCard" : item.isFromAssets() ? "Assets" : "未知") +
                        ", 路径: " + pagPath);

                if (pagPath != null) {
                    // 停止之前的动画
                    pagView.stop();

                    // 设置PAG文件路径（PAG库会自动处理assets://前缀）
                    pagView.setPath(pagPath);

                    // 设置循环播放
                    pagView.setRepeatCount(-1); // 无限循环

                    // 开始播放
                    pagView.play();

                    Log.d("ImagePagerAdapter", "✅ PAG文件加载成功: " + pagPath);

                } else {
                    Log.e("ImagePagerAdapter", "❌ PAG文件路径为null");
                    tvType.setText("PAG文件路径错误");
                }
            } catch (Exception e) {
                Log.e("ImagePagerAdapter", "❌ PAG加载异常: " + e.getMessage());
                e.printStackTrace();
                tvType.setText("PAG加载失败: " + e.getMessage());
            }
        }
        }
}