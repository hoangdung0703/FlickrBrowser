package vn.edu.usth.flickrbrowser.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Callback click item để Fragment mở DetailActivity */
    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoItem photo, int position);
    }

    private OnPhotoClickListener listener;

    public void setOnPhotoClickListener(OnPhotoClickListener l) {
        this.listener = l;
    }

    /** Danh sách dữ liệu */
    private final List<PhotoItem> data = new ArrayList<>();
    // ====== Các hằng và biến mới để xử lý loading footer ======
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_LOADING = 2;
    private boolean isLoading = false;

    /** Lấy toàn bộ dữ liệu hiện tại (dùng cho DetailActivity) */
    public ArrayList<PhotoItem> getCurrentData() {
        return new ArrayList<>(data);
    }

    /** Thay toàn bộ danh sách ảnh (dành cho load đầu hoặc refresh) */
    public void setData(List<PhotoItem> list) {
        data.clear();
        if (list != null && !list.isEmpty()) data.addAll(list);
        isLoading = false;
        notifyDataSetChanged();
    }

    /** Thêm dữ liệu cho phân trang (loadMore) */
    public void addMore(List<PhotoItem> more) {
        if (more == null || more.isEmpty()) return;
        int start = data.size();
        data.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }
    // ====== Thêm và xóa loading footer ======
    public void addLoadingFooter() {
        if (!isLoading) {
            isLoading = true;
            data.add(null); // placeholder
            notifyItemInserted(data.size() - 1);
        }
    }

    public void removeLoadingFooter() {
        if (isLoading && data.size() > 0) {
            isLoading = false;
            int position = data.size() - 1;
            data.remove(position);
            notifyItemRemoved(position);
        }
    }
    // ====== Phân biệt loại view ======
    @Override
    public int getItemViewType(int position) {
        if (isLoading && position == data.size() - 1) {
            return TYPE_LOADING;
        } else {
            return TYPE_PHOTO;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view tùy theo loại
        if (viewType == TYPE_PHOTO) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo, parent, false);
            return new VH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);
            return new LoadingVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (getItemViewType(pos) == TYPE_LOADING) return;
        VH h = (VH) holder;
        final PhotoItem p = data.get(pos);

        // Ưu tiên ảnh nhỏ (thumbUrl) để tải nhanh
        String url = (p.getThumbUrl() != null && !p.getThumbUrl().isEmpty())
                ? p.getThumbUrl()
                : p.getFullUrl();

        try {
            Glide.with(h.img.getContext())
                    .load(url)
                    .placeholder(R.drawable.placeholder_grey)
                    .error(R.drawable.placeholder_grey)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(h.img);
        } catch (Throwable t) {
            // tránh crash khi context đã bị destroy
            h.img.setImageResource(R.drawable.placeholder_grey);
        }

        // Xử lý click vào ảnh
        h.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onPhotoClick(p, h.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;

        VH(@NonNull View v) {
            super(v);
            // item_photo.xml phải có ImageView id = imgPhoto
            img = v.findViewById(R.id.imgPhoto);
        }
    }
    // ViewHolder cho footer loading
    static class LoadingVH extends RecyclerView.ViewHolder {
        final ProgressBar progressBar;

        LoadingVH(@NonNull View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }
}
