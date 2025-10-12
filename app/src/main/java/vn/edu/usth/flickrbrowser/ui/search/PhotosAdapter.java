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

// PhotosAdapter.java
public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH> {
    private final List<PhotoItem> data = new ArrayList<>();

    // Đổi callback: trả cả item + vị trí
    public interface OnItemClick { void onClick(PhotoItem item, int position); }
    private final OnItemClick onItemClick;

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

    //  Cho Fragment lấy danh sách hiện tại để gửi sang DetailActivity
    public ArrayList<PhotoItem> getCurrentData() {
        return new ArrayList<>(data);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
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

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
        }
    }
}