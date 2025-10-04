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
    private final List<PhotoItem> data = new ArrayList<>();
    private final FavoritesViewModel favVM;
    public PhotosAdapter(FavoritesViewModel favVM) {
        this.favVM = favVM;
    }

    public void submitList(List<PhotoItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged(); // đủ dùng lúc này; sau có thể đổi sang DiffUtil
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
                .placeholder(R.drawable.bg_skeleton_rounded) // tạm
                .centerCrop()
                .into(h.img);

        boolean isFav = favVM.isFavorite(it.id);
        h.heart.setImageResource(isFav
                ? R.drawable.baseline_favorite_24
                : R.drawable.outline_favorite_24);

        h.heart.setOnClickListener(v -> {
            int adapterPos = h.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            PhotoItem cur = data.get(adapterPos);
            favVM.toggleFavorite(cur.id);
            notifyItemChanged(adapterPos);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageView heart;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
            heart = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
