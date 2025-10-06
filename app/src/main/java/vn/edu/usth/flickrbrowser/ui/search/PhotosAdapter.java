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
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH> {

    // Data
    private final List<PhotoItem> data = new ArrayList<>();

    // Optional behaviors
    public interface OnItemClick { void onClick(PhotoItem item); }
    private final OnItemClick onItemClick;          // optional
    private final FavoritesViewModel favVM;         // optional

    // ---- Constructors (choose what you need) ----
    public PhotosAdapter() { this(null, null); }
    public PhotosAdapter(OnItemClick cb) { this(null, cb); }
    public PhotosAdapter(FavoritesViewModel favVM) { this(favVM, null); }
    public PhotosAdapter(FavoritesViewModel favVM, OnItemClick cb) {
        this.favVM = favVM;
        this.onItemClick = cb;
    }

    // Submit/replace list
    public void submitList(List<PhotoItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged(); // đủ dùng; sau có thể thay bằng DiffUtil
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PhotoItem it = data.get(pos);

        // Load image
        Glide.with(h.img.getContext())
                .load(it.getThumbUrl())
                .placeholder(R.drawable.bg_skeleton_rounded) // optional
                .centerCrop()
                .into(h.img);

        // Root click → callback (nếu có)
        if (onItemClick != null) {
            h.itemView.setOnClickListener(v -> {
                int adapterPos = h.getBindingAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                onItemClick.onClick(data.get(adapterPos));
            });
        } else {
            h.itemView.setOnClickListener(null);
        }

        // Heart behavior (toggle favorites) — chỉ khi có favVM
        if (h.heart != null) {
            if (favVM != null && it != null && it.id != null && !it.id.isEmpty()) {
                boolean isFav = favVM.isFavorite(it.id);
                h.heart.setVisibility(View.VISIBLE);
                h.heart.setImageResource(isFav
                        ? R.drawable.baseline_favorite_24
                        : R.drawable.outline_favorite_24);

                h.heart.setOnClickListener(v -> {
                    int adapterPos = h.getBindingAdapterPosition();
                    if (adapterPos == RecyclerView.NO_POSITION) return;
                    PhotoItem cur = data.get(adapterPos);
                    // toggle trong ViewModel
                    favVM.toggleFavorite(cur.id);
                    // update icon tại chỗ
                    notifyItemChanged(adapterPos);
                });
            } else {
                // Không có ViewModel → ẩn nút ♥ để tránh bấm nhầm
                h.heart.setVisibility(View.GONE);
                h.heart.setOnClickListener(null);
            }
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageView heart; // @id/btnFavorite (có thể null nếu layout không có)
        VH(@NonNull View itemView) {
            super(itemView);
            img   = itemView.findViewById(R.id.imgPhoto);
            heart = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
