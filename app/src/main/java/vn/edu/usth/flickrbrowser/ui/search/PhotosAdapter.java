package vn.edu.usth.flickrbrowser.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import android.widget.ProgressBar;

// PhotosAdapter.java
public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<PhotoItem> data = new ArrayList<>();

    // Đổi callback: trả cả item + vị trí
    public interface OnItemClick { void onClick(PhotoItem item, int position); }
    private final OnItemClick onItemClick;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_LOADING = 2;
    private boolean isLoading = false;

    public PhotosAdapter() { this(null); }
    public PhotosAdapter(OnItemClick cb) { this.onItemClick = cb; }

    public void submitList(List<PhotoItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public void addMore(List<PhotoItem> items) {
        if (items == null || items.isEmpty()) return;
        int start = data.size();
        data.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }
    // ====== Thêm và xóa loading footer ======
    public void addLoadingFooter() {
        if (!isLoading) {
            isLoading = true;
            data.add(null); // placeholder
            notifyItemInserted(data.size() - 1);
        }
    }
    
    @Override
    public int getItemCount() {
        return data.size();
    }

    public void removeLoadingFooter() {
        if (isLoading && data.size() > 0) {
            isLoading = false;
            int position = data.size() - 1;
            data.remove(position);
            notifyItemRemoved(position);
        }
    }
    /** Xóa toàn bộ dữ liệu và reset trạng thái loading **/
    public void clearData() { /*** added ***/
        isLoading = false;
        data.clear();
        notifyDataSetChanged();
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

    //  Cho Fragment lấy danh sách hiện tại để gửi sang DetailActivity
    public ArrayList<PhotoItem> getCurrentData() {
        return new ArrayList<>(data);
    }

    @NonNull @Override
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

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (getItemViewType(pos) == TYPE_LOADING) return;
        VH h = (VH) holder;
        PhotoItem it = data.get(pos);
        Glide.with(h.img.getContext())
                .load(it.getThumbUrl())
                .placeholder(R.drawable.bg_skeleton_rounded)
                .centerCrop()
                .into(h.img);
        if (onItemClick != null) {
            h.itemView.setOnClickListener(v -> {
                int p = h.getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION) {
                    onItemClick.onClick(data.get(p), p);
                }
            });
        }
    }
    
    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
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