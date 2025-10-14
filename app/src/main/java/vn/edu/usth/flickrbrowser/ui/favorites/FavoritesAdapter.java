package vn.edu.usth.flickrbrowser.ui.favorites;

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

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

    public interface OnFavoriteClick { void onClick(PhotoItem item); }
    public interface OnItemClick     { void onClick(PhotoItem item, int position); }

    private List<PhotoItem> items;
    private final OnFavoriteClick favCallback;
    private final OnItemClick itemClick; // mới thêm để mở Detail

    public FavoritesAdapter(List<PhotoItem> items,
                            OnFavoriteClick favCallback,
                            OnItemClick itemClick) {
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
        this.favCallback = favCallback;
        this.itemClick = itemClick;
    }

    public ArrayList<PhotoItem> getCurrentData() {
        return new ArrayList<>(items);
    }

    public void update(List<PhotoItem> newItems) {
        this.items = (newItems != null) ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PhotoItem item = items.get(position);

        // Hiển thị ảnh (nếu layout có imgPhoto)
        if (holder.img != null) {
            String url = item.getThumbUrl();
            if (url == null || url.isEmpty()) url = item.getFullUrl();

            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.bg_skeleton_rounded)
                    .centerCrop()
                    .into(holder.img);
        }

        if (holder.btnFavorite != null) {
            holder.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
            holder.btnFavorite.setContentDescription(
                    holder.itemView.getContext().getString(R.string.cd_unfavorite)
            );
            holder.btnFavorite.setOnClickListener(v -> {
                if (favCallback != null) favCallback.onClick(item);
            });
        }

        // Click item → mở DetailActivity (dùng chung layout Detail)
        holder.itemView.setOnClickListener(v -> {
            if (itemClick != null) itemClick.onClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageView btnFavorite;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
