package vn.edu.usth.flickrbrowser.ui.search;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH> {

    public interface OnItemClick { void onClick(@NonNull PhotoItem item, int position); }

    private final List<PhotoItem> data = new ArrayList<>();
    @Nullable private final OnItemClick onItemClick;

    // --- Khởi tạo
    public PhotosAdapter() { this(null); }
    public PhotosAdapter(@Nullable OnItemClick cb) {
        this.onItemClick = cb;
        setHasStableIds(true); // giúp RecyclerView ổn định hơn
    }

    // --- Cập nhật dữ liệu an toàn
    public void submitList(@Nullable List<PhotoItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged(); // đơn giản; có thể nâng cấp lên DiffUtil sau
    }

    // Tùy chọn: thêm cho paging
    public void append(@Nullable List<PhotoItem> more) {
        if (more == null || more.isEmpty()) return;
        int start = data.size();
        data.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @Override public long getItemId(int position) {
        // Stable id: ưu tiên id thật, fallback hash nếu rỗng
        try {
            String id = data.get(position).id;
            return id == null ? position : id.hashCode();
        } catch (Exception e) {
            return position;
        }
    }

    // --- Tạo ViewHolder
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    // --- Bind với các “hàng rào” chống crash
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        if (position < 0 || position >= data.size()) return; // guard
        final PhotoItem it = data.get(position);
        final String url = it != null ? it.getThumbUrl() : null;

        // Clear trước khi nạp mới để tránh “ảnh ma”
        Glide.with(h.img.getContext()).clear(h.img);

        Glide.with(h.img.getContext())
                .load(url)
                .placeholder(R.drawable.bg_skeleton_rounded)
                .error(R.drawable.placeholder_grey)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.w("PhotosAdapter", "Glide load failed: " + model, e);
                        return false; // vẫn để Glide hiển thị error drawable
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        return false;
                    }
                })
                .into(h.img);

        // Click an toàn (check vị trí lại vì có thể đã thay đổi)
        h.itemView.setOnClickListener(v -> {
            if (onItemClick == null) return;
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (pos < 0 || pos >= data.size()) return;
            try {
                onItemClick.onClick(data.get(pos), pos);
            } catch (Exception e) {
                Log.e("PhotosAdapter", "onItemClick error", e);
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        // Giải phóng request Glide khi item bị recycle
        Glide.with(holder.img.getContext()).clear(holder.img);
        super.onViewRecycled(holder);
    }

    // --- ViewHolder
    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
        }
    }
}
