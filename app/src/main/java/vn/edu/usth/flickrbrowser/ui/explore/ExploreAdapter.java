package vn.edu.usth.flickrbrowser.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

// Changed to handle multiple view types
public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    /** Callback click item để Fragment mở DetailActivity */
    public interface OnPhotoClickListener { void onPhotoClick(PhotoItem photo, int position); }
    private OnPhotoClickListener listener;

    public void setOnPhotoClickListener(OnPhotoClickListener l) { this.listener = l; }

    /** Dữ liệu */
    private final List<PhotoItem> data = new ArrayList<>();
    private boolean isLoadingAdded = false; // Flag to check if loading footer is added

    public List<PhotoItem> getCurrentData() { return new ArrayList<>(data); }

    /** Thay toàn bộ danh sách */
    public void setData(List<PhotoItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    /** Thêm trang mới (paging) */
    public void addPhotos(List<PhotoItem> newPhotos) {
        if (newPhotos == null || newPhotos.isEmpty()) return;
        int start = data.size();
        data.addAll(newPhotos);
        notifyItemRangeInserted(start, newPhotos.size());
    }

    // --- Helper methods for loading footer ---

    public void addLoadingFooter() {
        if (!isLoadingAdded) {
            isLoadingAdded = true;
            // Add a null item to represent the loading footer
            data.add(null);
            notifyItemInserted(data.size() - 1);
        }
    }

    public void removeLoadingFooter() {
        if (isLoadingAdded) {
            isLoadingAdded = false;
            int position = data.size() - 1;
            if (position >= 0) {
                // Ensure the item is indeed a loader (null) before removing
                if (data.get(position) == null) {
                    data.remove(position);
                    notifyItemRemoved(position);
                }
            }
        }
    }

    // --- Overridden methods for multiple view types ---

    @Override
    public int getItemViewType(int position) {
        // If the item at the last position is null, it's the loading footer
        return (data.get(position) == null) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo, parent, false);
            return new PhotoVH(v);
        } else { // VIEW_TYPE_LOADING
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);
            return new LoadingVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            PhotoVH h = (PhotoVH) holder;
            final PhotoItem p = data.get(pos);

            String url = (p.getThumbUrl() != null && !p.getThumbUrl().isEmpty())
                    ? p.getThumbUrl() : p.getFullUrl();

            try {
                Glide.with(h.img.getContext())
                        .load(url)
                        .placeholder(R.drawable.placeholder_grey)
                        .error(R.drawable.placeholder_grey)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(h.img);
            } catch (Throwable t) {
                h.img.setImageResource(R.drawable.placeholder_grey);
            }

            h.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPhotoClick(p, h.getBindingAdapterPosition());
            });
        }
        // No binding needed for LoadingVH, it's just a progress bar
    }

    @Override
    public int getItemCount() { return data.size(); }

    // --- ViewHolders ---

    // Renamed from VH for clarity
    static class PhotoVH extends RecyclerView.ViewHolder {
        final ImageView img;
        PhotoVH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgPhoto);
        }
    }

    // New ViewHolder for the loading item
    static class LoadingVH extends RecyclerView.ViewHolder {
        LoadingVH(@NonNull View v) {
            super(v);
            // We don't need a reference to the progress bar itself, just the view
        }
    }
}
